package control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import entity.ClassEquipmentRequirement;
import entity.ClassStatus;
import entity.ClassType;
import entity.Consts;
import entity.Tip;
import entity.TrainingClass;

public class TrainingClassControl {

    private static TrainingClassControl instance;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> autoReleaseTask;

    private TrainingClassControl() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static TrainingClassControl getInstance() {
        if (instance == null) instance = new TrainingClassControl();
        return instance;
    }

    private static class ClassInfo {
        String name, typeName;
        int consultantID, maxParticipants, planID;
        LocalDateTime startTime, endTime;
        ClassStatus status;

        ClassInfo(String name, String typeName, int consultantID,
                  LocalDateTime startTime, LocalDateTime endTime,
                  int maxParticipants, ClassStatus status, int planID) {
            this.name = name;
            this.typeName = typeName;
            this.consultantID = consultantID;
            this.startTime = startTime;
            this.endTime = endTime;
            this.maxParticipants = maxParticipants;
            this.status = status;
            this.planID = planID;
        }
    }
    
    public static class TipInput {
        private final String content;
        private final String url;

        public TipInput(String content, String url) {
            this.content = content;
            this.url = url;
        }
        public String getContent() { return content; }
        public String getUrl() { return url; }
    }

    public static class EquipmentReqInput {
        private final String typeName;
        private final int quantity;

        public EquipmentReqInput(String typeName, int quantity) {
            this.typeName = typeName;
            this.quantity = quantity;
        }
        public String getTypeName() { return typeName; }
        public int getQuantity() { return quantity; }
    }
    
    public boolean createClassFromUI(
            String name,
            LocalDateTime start,
            LocalDateTime end,
            String typeName,
            int maxParticipants,
            int planId,
            int consultantId,
            ArrayList<TipInput> tipsInput,
            ArrayList<EquipmentReqInput> reqInput
    ) {
        // convert tips
        ArrayList<Tip> tips = new ArrayList<>();
        if (tipsInput != null) {
            for (TipInput ti : tipsInput) {
                tips.add(new Tip(ti.getContent(), ti.getUrl()));
            }
        }

        // convert equipment requirements
        ArrayList<ClassEquipmentRequirement> reqs = new ArrayList<>();
        if (reqInput != null) {
            for (EquipmentReqInput r : reqInput) {
                reqs.add(new ClassEquipmentRequirement(0, r.getTypeName(), r.getQuantity()));
            }
        }

        // create entity TrainingClass
        TrainingClass tc = new TrainingClass(
                name,
                start,
                end,
                typeName,
                maxParticipants,
                planId,
                consultantId,
                ClassStatus.SCHEDULED
        );

        return addClass(tc, tips, reqs);
    }

    public boolean addClass(TrainingClass tc, ArrayList<Tip> tips) {
        return addClass(tc, tips, null);
    }

    public boolean addClass(TrainingClass tc, ArrayList<Tip> tips, ArrayList<ClassEquipmentRequirement> requirements) {
        if (!tc.getEndTime().isAfter(tc.getStartTime()))
            throw new IllegalArgumentException("End time must be after start time.");

        LocalDateTime now = LocalDateTime.now();
        if (tc.getStartTime().isBefore(now) || tc.getEndTime().isBefore(now))
            throw new IllegalArgumentException("Class times cannot be in the past.");

        if (tips != null && tips.size() > 5)
            throw new IllegalArgumentException("Maximum 5 tips per class.");

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
            conn.setAutoCommit(false);
            autoUpdateStatuses(conn);

            int newClassID;
            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_CLASS, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, tc.getName());
                stmt.setTimestamp(2, Timestamp.valueOf(tc.getStartTime()));
                stmt.setTimestamp(3, Timestamp.valueOf(tc.getEndTime()));
                stmt.setString(4, tc.getTypeName());
                stmt.setInt(5, tc.getMaxParticipants());
                stmt.setInt(6, tc.getPlanID());
                stmt.setInt(7, tc.getConsultantID());
                stmt.setString(8, ClassStatus.SCHEDULED.name());

                if (stmt.executeUpdate() == 0) throw new SQLException("Insert failed.");

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) newClassID = keys.getInt(1);
                    else throw new SQLException("No ID obtained.");
                }
            }

            if (tips != null && !tips.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_TIP)) {
                    for (Tip tip : tips) {
                        if (tip.getContent() != null && !tip.getContent().trim().isEmpty()) {
                            stmt.setString(1, tip.getContent().trim());
                            stmt.setString(2, tip.getURL());
                            stmt.setInt(3, newClassID);
                            stmt.executeUpdate();
                        }
                    }
                }
            }

            if (requirements != null && !requirements.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_EQUIPMENT_REQ)) {
                    for (ClassEquipmentRequirement req : requirements) {
                        stmt.setInt(1, newClassID);
                        stmt.setString(2, req.getTypeName());
                        stmt.setInt(3, req.getRequiredQuantity());
                        stmt.executeUpdate();
                    }
                }
            }

            if (requirements != null && !requirements.isEmpty()) {
                ArrayList<String> conflicts = checkEquipmentAvailability(conn, newClassID, tc.getStartTime(), tc.getEndTime());
                if (!conflicts.isEmpty()) {
                    conn.rollback(); 
                    throw new EquipmentConflictException(newClassID, conflicts);
                }
            }

            conn.commit(); 
            return true;

        } catch (EquipmentConflictException e) {
            throw e; 
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public ArrayList<String> checkEquipmentAvailability(int classID, LocalDateTime startTime, LocalDateTime endTime) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            return checkEquipmentAvailability(conn, classID, startTime, endTime);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<String> checkEquipmentAvailability(Connection conn, int classID, LocalDateTime startTime, LocalDateTime endTime) {
        ArrayList<String> conflicts = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_CHECK_EQUIPMENT_AVAILABILITY)) {
            stmt.setInt(1, classID);
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));
            stmt.setTimestamp(3, Timestamp.valueOf(startTime));
            stmt.setInt(4, classID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("typeName");
                    int ourRequest = rs.getInt("ourRequest");
                    int totalAvail = rs.getInt("totalAvailable");
                    int othersReserved = rs.getInt("othersReserved");
                    int totalNeeded = ourRequest + othersReserved;

                    if (totalNeeded > totalAvail) {
                        conflicts.add(String.format("%s: need %d, only %d available (%d reserved by other classes)",
                        		type, ourRequest, totalAvail - othersReserved, othersReserved));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conflicts;
    }

    public ArrayList<ClassEquipmentRequirement> getEquipmentForClass(int classID) {
        ArrayList<ClassEquipmentRequirement> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_EQUIPMENT_BY_CLASS)) {
            stmt.setInt(1, classID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(new ClassEquipmentRequirement(
                        rs.getInt("classID"),
                        rs.getString("typeName"),
                        rs.getInt("requestQuantity")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public ArrayList<EquipmentReqInput> getEquipmentInputsForClass(int classID) {
        ArrayList<EquipmentReqInput> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_EQUIPMENT_BY_CLASS)) {

            stmt.setInt(1, classID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new EquipmentReqInput(
                        rs.getString("typeName"),
                        rs.getInt("requestQuantity")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateEquipmentRequirements(int classID,
            ArrayList<ClassEquipmentRequirement> requirements,
            LocalDateTime startTime, LocalDateTime endTime) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
            conn.setAutoCommit(false);
            autoUpdateStatuses(conn);

            try (PreparedStatement del = conn.prepareStatement(Consts.SQL_DEL_EQUIPMENT_REQ_BY_CLASS)) {
                del.setInt(1, classID);
                del.executeUpdate();
            }

            if (requirements != null && !requirements.isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement(Consts.SQL_INS_EQUIPMENT_REQ)) {
                    for (ClassEquipmentRequirement req : requirements) {
                        ins.setInt(1, classID);
                        ins.setString(2, req.getTypeName());
                        ins.setInt(3, req.getRequiredQuantity());
                        ins.executeUpdate();
                    }
                }
            }

            if (requirements != null && !requirements.isEmpty()) {
                ArrayList<String> conflicts = checkEquipmentAvailability(conn, classID, startTime, endTime);
                if (!conflicts.isEmpty()) {
                    conn.rollback(); 
                    throw new EquipmentConflictException(classID, conflicts);
                }
            }

            conn.commit(); 
            return true;

        } catch (EquipmentConflictException e) {
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    public boolean updateEquipmentRequirementsFromUI(int classID,ArrayList<EquipmentReqInput> reqInput,LocalDateTime startTime,LocalDateTime endTime) {
        ArrayList<ClassEquipmentRequirement> reqs = new ArrayList<>();
        if (reqInput != null) {
            for (EquipmentReqInput r : reqInput) {
                reqs.add(new ClassEquipmentRequirement(classID, r.getTypeName(), r.getQuantity()));
            }
        }
        return updateEquipmentRequirements(classID, reqs, startTime, endTime);
    }

    public static class EquipmentConflictException extends RuntimeException {
        private final int classID;
        private final ArrayList<String> conflicts;

        public EquipmentConflictException(int classID, ArrayList<String> conflicts) {
            super("Equipment availability conflict for class " + classID);
            this.classID = classID;
            this.conflicts = conflicts;
        }

        public int getClassID() { 
        	return classID; 
        }
        public ArrayList<String> getConflicts() { 
        	return conflicts;
        }
    }

    public boolean rescheduleClass(int classID, LocalDateTime newStart, LocalDateTime newEnd) {
        if (!newEnd.isAfter(newStart))
            throw new IllegalArgumentException("End time must be after start time.");

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
            conn.setAutoCommit(false);

            autoUpdateStatuses(conn);

            ClassInfo info = getClassInfo(conn, classID);
            if (info.status != ClassStatus.SCHEDULED)
                throw new IllegalArgumentException("Only SCHEDULED classes can be moved.");

            // check equipment conflicts for the NEW time window
            ArrayList<String> conflicts = checkEquipmentAvailability(conn, classID, newStart, newEnd);
            if (!conflicts.isEmpty()) {
                conn.rollback();
                throw new EquipmentConflictException(classID, conflicts);
            }

            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_UPD_CLASS_TIME)) {
                stmt.setTimestamp(1, Timestamp.valueOf(newStart));
                stmt.setTimestamp(2, Timestamp.valueOf(newEnd));
                stmt.setInt(3, classID);

                boolean ok = stmt.executeUpdate() > 0;
                conn.commit();
                return ok;
            }

        } catch (EquipmentConflictException e) {
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) 
            	try {
            		conn.rollback();
            	} catch (SQLException ex) { 
            		ex.printStackTrace();
            	}
            return false;
        } finally {
            if (conn != null) 
            	try {
            	conn.setAutoCommit(true);
            	conn.close();
            	} catch (SQLException e) {
            		e.printStackTrace();
            	}
        }
    }

    public boolean cancelClass(int classID) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            ClassInfo info = getClassInfo(conn, classID);

            if (info.status == ClassStatus.CANCELLED)
                throw new IllegalArgumentException("Class is already cancelled.");
            if (info.status == ClassStatus.COMPLETED)
                throw new IllegalArgumentException("Cannot cancel a class that has already completed.");

            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_CANCEL_CLASS)) {
                stmt.setInt(1, classID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public TrainingClass getClassByID(int classID) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            ClassInfo info = getClassInfo(conn, classID);
            return new TrainingClass(
                classID, info.name, info.startTime, info.endTime,
                info.typeName, info.maxParticipants, info.planID,
                info.consultantID, info.status
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<TrainingClass> getAllClasses() {
        ArrayList<TrainingClass> classes = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            autoUpdateStatuses(conn);
            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_CLASSES);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    classes.add(buildTrainingClassFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public ArrayList<TrainingClass> getAvailableClassesForTrainee(int traineeID) {
        ArrayList<TrainingClass> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_AVAILABLE_CLASSES_FOR_TRAINEE)) {
        	autoUpdateStatuses(conn);
            stmt.setInt(1, traineeID);
            stmt.setInt(2, traineeID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(buildTrainingClassFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<TrainingClass> getTraineeSchedule(int traineeID) {
        ArrayList<TrainingClass> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_TRAINEE_SCHEDULE_HISTORY)) {
        	autoUpdateStatuses(conn);
            stmt.setInt(1, traineeID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(buildTrainingClassFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<ClassType> getAllClassTypes() {
        ArrayList<ClassType> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_TYPES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next())
                list.add(new ClassType(rs.getString("typeName"), rs.getString("description")));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public ArrayList<String> getAllClassTypeNames() {
        ArrayList<String> typeNames = new ArrayList<>();
        ArrayList<ClassType> types = getAllClassTypes();       
        for (ClassType type : types) {
            typeNames.add(type.getTypeName());
        }
        return typeNames;
    }
    
    public ArrayList<String> getAllStatusNames() {
        ArrayList<String> statusNames = new ArrayList<>();
        for (ClassStatus status : ClassStatus.values()) {
            statusNames.add(status.name());
        }
        return statusNames;
    }

    public boolean activateEmergencyMode() {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            autoUpdateStatuses(conn);
            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_ACTIVATE_EMERGENCY)) {
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0)
                    startEmergencyTimer(30);
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean releaseEmergencyMode() {
        if (autoReleaseTask != null)
            autoReleaseTask.cancel(false);

        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            conn.setAutoCommit(false);
            try {
                int updated = 0;
                try (PreparedStatement st1 = conn.prepareStatement(Consts.SQL_RELEASE_EMERGENCY_FUTURE))    { updated += st1.executeUpdate(); }
                try (PreparedStatement st2 = conn.prepareStatement(Consts.SQL_RELEASE_EMERGENCY_ONGOING))  { updated += st2.executeUpdate(); }
                try (PreparedStatement st3 = conn.prepareStatement(Consts.SQL_RELEASE_EMERGENCY_FINISHED)) { updated += st3.executeUpdate(); }
                conn.commit();
                return updated > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startEmergencyTimer(int minutes) {
        if (autoReleaseTask != null && !autoReleaseTask.isDone())
            autoReleaseTask.cancel(false);

        autoReleaseTask = scheduler.schedule(
            this::releaseEmergencyMode, minutes, TimeUnit.MINUTES
        );
    }

    private void autoUpdateStatuses(Connection conn) throws SQLException {
        try (PreparedStatement st1 = conn.prepareStatement(Consts.SQL_AUTO_TO_ACTIVE))     { st1.executeUpdate(); }
        try (PreparedStatement st2 = conn.prepareStatement(Consts.SQL_AUTO_TO_COMPLETED))  { st2.executeUpdate(); }
    }

    private ClassInfo getClassInfo(Connection conn, int classID) throws SQLException {
        autoUpdateStatuses(conn);
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_GET_CLASS_INFO)) {
            stmt.setInt(1, classID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Class not found.");
                return new ClassInfo(
                    rs.getString("name"),
                    rs.getString("typeName"),
                    rs.getInt("consultantID"),
                    rs.getTimestamp("startTime").toLocalDateTime(),
                    rs.getTimestamp("endTime").toLocalDateTime(),
                    rs.getInt("maxParticipants"),
                    parseClassStatus(rs.getString("status")),
                    rs.getInt("planID")
                );
            }
        }
    }

    private TrainingClass buildTrainingClassFromResultSet(ResultSet rs) throws SQLException {
        return new TrainingClass(
            rs.getInt("classID"),
            rs.getString("name"),
            rs.getTimestamp("startTime").toLocalDateTime(),
            rs.getTimestamp("endTime").toLocalDateTime(),
            rs.getString("typeName"),
            rs.getInt("maxParticipants"),
            rs.getInt("planID"),
            rs.getInt("consultantID"),
            parseClassStatus(rs.getString("status"))
        );
    }

    private static ClassStatus parseClassStatus(String s) {
        if (s == null) return ClassStatus.SCHEDULED;
        try { return ClassStatus.valueOf(s.trim().toUpperCase()); }
        catch (Exception e) { return ClassStatus.SCHEDULED; }
    }
}