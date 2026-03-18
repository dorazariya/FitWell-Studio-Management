package control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import entity.Consts;
import entity.FitnessConsultant;
import entity.Trainee;
import entity.Dietitian; 
import entity.StudioManager; 
import entity.UpdateMethod;

/**
 * Control class responsible ONLY for authentication and session management.
 * Login / Logout for all user types
 * Holding the currently logged-in user's session
 * Retrieving consultant list (used in class/plan creation screens)
 */
public class UsersControl {
    private static UsersControl instance;
    private Object loggedInUser = null;
    private int loggedInUserId = -1;
    private String loggedInUserName = "";

    private UsersControl() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static UsersControl getInstance() {
        if (instance == null) instance = new UsersControl();
        return instance;
    }

    // Authentication
    public boolean loginTrainee(int id) {
        Trainee t = TraineeRegisterControl.getInstance().getTraineeById(id);
        if (t != null) {
            this.loggedInUser = t;
            this.loggedInUserId = id;
            this.loggedInUserName = t.getFirstName() + " " + t.getLastName(); 
            return true;
        }
        return false;
    }

    public boolean loginConsultant(int id) {
        return authenticate(id, Consts.SQL_SEL_CONSULTANT_BY_ID, "Consultant");
    }

    public boolean loginDietitian(int id) {
        return authenticate(id, Consts.SQL_SEL_DIETITIAN_BY_ID, "Dietitian");
    }

    public boolean loginManager(int id) {
        return authenticate(id, Consts.SQL_SEL_MANAGER_BY_ID, "Manager");
    }

    private boolean authenticate(int id, String sql, String role) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("firstName");
                    String lastName  = rs.getString("lastName");
                    this.loggedInUserName = firstName + " " + lastName;
                    this.loggedInUserId = rs.getInt("ID");

                    switch (role) {
                        case "Consultant":
                            this.loggedInUser = new FitnessConsultant(rs.getInt("ID"), firstName, lastName);
                            break;

                        case "Dietitian":
                            this.loggedInUser = new Dietitian(rs.getInt("ID"), firstName, lastName);
                            break;

                        case "Manager":
                            this.loggedInUser = new StudioManager(rs.getInt("ID"), firstName, lastName);
                            break;
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Object getLoggedInUser() {
        return loggedInUser;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    public Trainee getCurrentTrainee() {
        if (loggedInUser instanceof Trainee) {
            return (Trainee) loggedInUser;
        }
        return null;
    }

    public FitnessConsultant getCurrentConsultant() {
        if (loggedInUser instanceof FitnessConsultant) {
            return (FitnessConsultant) loggedInUser;
        }
        return null;
    }

    public Dietitian getCurrentDietitian() {
        if (loggedInUser instanceof Dietitian) {
            return (Dietitian) loggedInUser;
        }
        return null;
    }

    public StudioManager getCurrentManager() {
        if (loggedInUser instanceof StudioManager) {
            return (StudioManager) loggedInUser;
        }
        return null;
    }

    public String getLoggedInUserName() {
        return loggedInUserName;
    }

    // Update Trainee session data inside Control
    public void updateSessionTrainee(int id, String fName, String lName, Date dob, String email, String phone, UpdateMethod method) {
        if (loggedInUser instanceof Trainee && this.loggedInUserId == id) {
            Trainee t = (Trainee) loggedInUser;
            t.setFirstName(fName);
            t.setLastName(lName);
            t.setBirthDate(dob);
            t.setEmail(email);
            t.setPhone(phone);
            t.setUpdateMethod(method);

            // keep session name consistent too
            this.loggedInUserName = fName + " " + lName;
        }
    }

    public void logout() {
        this.loggedInUser = null;
        this.loggedInUserId = -1;
        this.loggedInUserName = "";  
    }

    public ArrayList<FitnessConsultant> getAllConsultants() {
        ArrayList<FitnessConsultant> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_CONSULTANTS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new FitnessConsultant(
                    rs.getInt("ID"),
                    rs.getString("firstName"),
                    rs.getString("lastName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}