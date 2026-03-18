package control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;

import entity.ClassStatus;
import entity.Consts;
import entity.Trainee;
import entity.TrainingClass;
import entity.UpdateMethod;

/**
 * Control class responsible for ALL trainee-related operations:
 * - Trainee profile management (add, update, retrieve)
 * - Class registration and cancellation
 * - Trainee queries (who is in a class, who is eligible)
 * - Input validation
 */
public class TraineeRegisterControl {

    private static TraineeRegisterControl instance;

    private TraineeRegisterControl() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static TraineeRegisterControl getInstance() {
        if (instance == null) instance = new TraineeRegisterControl();
        return instance;
    }

    // Returns a single trainee by ID, or null if not found.
    public Trainee getTraineeById(int id) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_TRAINEE_BY_ID)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return buildTraineeFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Returns all trainees in the system.
    public ArrayList<Trainee> getAllTrainees() {
        ArrayList<Trainee> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_TRAINEES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next())
                list.add(buildTraineeFromResultSet(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

 // Adds a new trainee to the system.
    public boolean addTrainee(String fName, String lName, Date dob, String email, String phone, UpdateMethod method) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_TRAINEE)) {

            stmt.setString(1, fName);
            stmt.setString(2, lName);

            if (dob != null)
                stmt.setDate(3, new java.sql.Date(dob.getTime()));
            else
                stmt.setNull(3, java.sql.Types.DATE);

            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setString(6, method.name());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

 // Updates an existing trainee's profile
    public boolean updateTraineeProfile(int id, String fName, String lName, Date dob, String email, String phone, UpdateMethod method) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_UPD_TRAINEE_PROFILE)) {

            stmt.setString(1, fName);
            stmt.setString(2, lName);

            if (dob != null)
                stmt.setDate(3, new java.sql.Date(dob.getTime()));
            else
                stmt.setNull(3, java.sql.Types.DATE);

            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setString(6, method.name());
            stmt.setInt(7, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Registers a trainee to a training class.
    public boolean registerTraineeToClass(int classID, int traineeID) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {

            if (isAlreadyRegistered(conn, classID, traineeID))
                throw new IllegalArgumentException("This trainee is already registered for this class.");

            TrainingClass tc = TrainingClassControl.getInstance().getClassByID(classID);
            if (tc == null)
                throw new IllegalArgumentException("Class not found.");

            if (hasTraineeOverlappingClass(conn, traineeID, classID, tc.getStartTime(), tc.getEndTime()))
                throw new IllegalArgumentException("Trainee is registered to another class at this time.");

            if (tc.getStatus() != ClassStatus.SCHEDULED)
                throw new IllegalArgumentException("Registration is only allowed for SCHEDULED classes.");

            if (tc.getPlanID() <= 0)
                throw new IllegalArgumentException("No plan assigned to class.");

            if (!FitnessPlanControl.getInstance().isPlanActive(tc.getPlanID()))
                throw new IllegalArgumentException("The fitness plan is not active.");

            if (!isTraineeAssignedToPlan(conn, traineeID, tc.getPlanID()))
                throw new IllegalArgumentException("Trainee is not assigned to this class's fitness plan.");

            if (LocalDateTime.now().isAfter(tc.getStartTime().minusHours(24)))
                throw new IllegalArgumentException("Registration closes 24 hours before class starts.");

            if (getCurrentParticipantCount(conn, classID) >= tc.getMaxParticipants())
                throw new IllegalArgumentException("Class is full.");

            return executeRegistration(conn, classID, traineeID);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancels a trainee's registration from a class.
     * Only allowed for SCHEDULED classes.
     */
    public boolean cancelTraineeRegistration(int classID, int traineeID) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {

            if (!isAlreadyRegistered(conn, classID, traineeID))
                throw new IllegalArgumentException("This trainee is not registered for this class.");

            TrainingClass tc = TrainingClassControl.getInstance().getClassByID(classID);
            if (tc == null)
                throw new IllegalArgumentException("Class not found.");

            if (tc.getStatus() != ClassStatus.SCHEDULED)
                throw new IllegalArgumentException("Cancellations are only allowed for SCHEDULED classes.");

            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_DEL_REGISTRATION)) {
                stmt.setInt(1, classID);
                stmt.setInt(2, traineeID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Returns all trainees currently registered to a given class.
    public ArrayList<Trainee> getTraineesInClass(int classID) {
        ArrayList<Trainee> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_TRAINEES_IN_CLASS)) {

            stmt.setInt(1, classID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(buildTraineeFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns trainees eligible to register for a given class:
     * - Assigned to the class's plan
     * - Not already registered
     * - No time overlap with other classes
     */
    public ArrayList<Trainee> getEligibleTraineesForClass(int classID) {
        ArrayList<Trainee> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {

            TrainingClass tc = TrainingClassControl.getInstance().getClassByID(classID);
            if (tc == null || !FitnessPlanControl.getInstance().isPlanActive(tc.getPlanID()))
                return list;

            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_TRAINEES_BY_PLAN)) {
                stmt.setInt(1, tc.getPlanID());
                stmt.setInt(2, classID);
                stmt.setInt(3, classID);
                stmt.setTimestamp(4, Timestamp.valueOf(tc.getEndTime()));
                stmt.setTimestamp(5, Timestamp.valueOf(tc.getStartTime()));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next())
                        list.add(buildTraineeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Returns the current number of participants in a class.
    public int getParticipantCount(int classID) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            return getCurrentParticipantCount(conn, classID);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Validates trainee profile fields.
    public String validateTraineeDetails(String fName, String lName, String email, String phone, java.util.Date dob) {
        if (fName == null || fName.trim().isEmpty() ||
            lName == null || lName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            dob == null)
            return "All fields are required. Please fill in all details.";

        if (fName.length() > 50 || lName.length() > 50)
            return "Name cannot exceed 50 characters.";

        if (email.length() > 100)
            return "Email cannot exceed 100 characters.";

        if (!fName.matches("^[a-zA-Z\\s\\-]+$") || !lName.matches("^[a-zA-Z\\s\\-]+$"))
            return "First Name and Last Name must contain only English letters.";

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            return "Please enter a valid email address with a domain (e.g., name@domain.com).";

        if (!phone.matches("^05\\d{8}$"))
            return "Phone number must be exactly 10 digits and start with '05'.";

        if (dob.after(new java.util.Date()))
            return "Birth date cannot be in the future.";

        return null;
    }

    // Helper functions
    private boolean isAlreadyRegistered(Connection conn, int classID, int traineeID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_CHECK_DUPLICATE_REG)) {
            stmt.setInt(1, classID);
            stmt.setInt(2, traineeID);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    private int getCurrentParticipantCount(Connection conn, int classID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_COUNT_PARTICIPANTS)) {
            stmt.setInt(1, classID);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private boolean isTraineeAssignedToPlan(Connection conn, int traineeID, int planID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_CHECK_TRAINEE_ASSIGNED_TO_PLAN)) {
            stmt.setInt(1, traineeID);
            stmt.setInt(2, planID);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    private boolean hasTraineeOverlappingClass(Connection conn, int traineeID, int classID,
                                                LocalDateTime start, LocalDateTime end) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_CHECK_TRAINEE_OVERLAP)) {
            stmt.setInt(1, traineeID);
            stmt.setInt(2, classID);
            stmt.setTimestamp(3, Timestamp.valueOf(end));
            stmt.setTimestamp(4, Timestamp.valueOf(start));
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    private boolean executeRegistration(Connection conn, int classID, int traineeID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_REGISTRATION)) {
            stmt.setInt(1, classID);
            stmt.setInt(2, traineeID);
            return stmt.executeUpdate() > 0;
        }
    }


    // Builds a Trainee object from a ResultSet row.
    private Trainee buildTraineeFromResultSet(ResultSet rs) throws SQLException {
        return new Trainee(
            rs.getInt("ID"),
            rs.getString("firstName"),
            rs.getString("lastName"),
            rs.getDate("dateOfBirth"),
            rs.getString("email"),
            rs.getString("phoneNumber"),
            parseUpdateMethod(rs.getString("updateMethod"))
        );
    }

    private static UpdateMethod parseUpdateMethod(String s) {
        try { return UpdateMethod.valueOf(s.trim()); }
        catch (Exception e) { return UpdateMethod.Email; }
    }
    
    public ArrayList<Trainee> getEligibleTraineesForGroupPlan(int planID, int minAge, int maxAge) {
        ArrayList<Trainee> allTrainees = getAllTrainees();
        ArrayList<Trainee> eligible = new ArrayList<>();
        LocalDate today = LocalDate.now();

        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            for (Trainee t : allTrainees) {
                if (isTraineeAssignedToPlan(conn, t.getId(), planID)) {
                    continue; 
                }
                
                // Check age
                if (t.getBirthDate() != null) {
                    LocalDate dob = new java.sql.Date(t.getBirthDate().getTime()).toLocalDate();
                    int age = Period.between(dob, today).getYears();
                    
                    if (age >= minAge && age <= maxAge) {
                        eligible.add(t); 
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eligible;
    }
}