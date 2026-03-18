package control;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import entity.Consts;
import entity.FitnessPlan;
import entity.GroupPlan;
import entity.PlanStatus;

public class FitnessPlanControl {
    private static FitnessPlanControl instance;

    private FitnessPlanControl() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static FitnessPlanControl getInstance() {
        if (instance == null) instance = new FitnessPlanControl();
        return instance;
    }

    public boolean isPlanActive(int planID) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            return isPlanActiveInternal(conn, planID);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isPlanActiveInternal(Connection conn, int planID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_GET_PLAN_STATUS)) {
            stmt.setInt(1, planID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return false;
                String s = rs.getString("status");
                return s != null && "ACTIVE".equalsIgnoreCase(s.trim());
            }
        }
    }

    public boolean updateTrainingPlan(int planID, Date startDate, int duration, String status, boolean isIndividual, 
            String goals, int traineeID, int minAge, int maxAge, String guidelines, ArrayList<String> preferredClasses) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtMain = conn.prepareStatement(Consts.SQL_UPD_FITNESS_PLAN)) {
                stmtMain.setDate(1, new java.sql.Date(startDate.getTime()));
                stmtMain.setInt(2, duration);
                stmtMain.setString(3, status);
                stmtMain.setInt(4, planID);
                stmtMain.executeUpdate();
            }
            
            if (isIndividual) {
                try (PreparedStatement stmtPersonal = conn.prepareStatement(Consts.SQL_UPD_PERSONAL_PLAN)) {
                    stmtPersonal.setString(1, goals);
                    stmtPersonal.setInt(2, planID);
                    stmtPersonal.executeUpdate();
                }
            
                try (PreparedStatement stmtDel = conn.prepareStatement(Consts.SQL_DEL_TRAINEE_ASSIGNMENT)) {
                    stmtDel.setInt(1, planID);
                    stmtDel.executeUpdate();
                }
                assignTraineeToPlan(conn, planID, traineeID);
            
            } else {
                try (PreparedStatement stmtGroup = conn.prepareStatement(Consts.SQL_UPD_GROUP_PLAN)) {
                    stmtGroup.setInt(1, minAge);
                    stmtGroup.setInt(2, maxAge);
                    stmtGroup.setString(3, guidelines);
                    stmtGroup.setInt(4, planID);
                    stmtGroup.executeUpdate();
                }
            
                try (PreparedStatement stmtDelTypes = conn.prepareStatement(Consts.SQL_DEL_GROUP_PREFS)) {
                    stmtDelTypes.setInt(1, planID);
                    stmtDelTypes.executeUpdate();
                }
            
                if (preferredClasses != null && !preferredClasses.isEmpty()) {
                    try (PreparedStatement stmtInsTypes = conn.prepareStatement(Consts.SQL_INS_GROUP_PREF)) {
                        for (String type : preferredClasses) {
                            stmtInsTypes.setInt(1, planID);
                            stmtInsTypes.setString(2, type);
                            stmtInsTypes.executeUpdate();
                        }
                    }
                }
            }
        
            conn.commit();
            return true;
        
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean addNewTrainingPlan(Date startDate, int duration, boolean isIndividual, String goals,int minAge,
    		int maxAge, String guidelines, ArrayList<String> preferredClasses, int traineeID, int consultantID) {

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
            conn.setAutoCommit(false);

            try (PreparedStatement stmtMain = conn.prepareStatement(Consts.SQL_INS_FITNESS_PLAN, Statement.RETURN_GENERATED_KEYS)) {
                stmtMain.setDate(1, new java.sql.Date(startDate.getTime()));
                stmtMain.setInt(2, duration);
                stmtMain.setInt(3, consultantID);
                stmtMain.setString(4, "Active");

                if (stmtMain.executeUpdate() == 0) throw new SQLException("Main insert failed.");

                try (ResultSet keys = stmtMain.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("ID generation failed.");
                    int newPlanID = keys.getInt(1);

                    if (isIndividual) {
                        insertPersonalDetails(conn, newPlanID, goals);
                        assignTraineeToPlan(conn, newPlanID, traineeID);
                    } else {
                        insertGroupDetails(conn, newPlanID, minAge, maxAge, guidelines, preferredClasses);
                    }
                }
            }
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public ArrayList<FitnessPlan> getAllFitnessPlans() {
        ArrayList<FitnessPlan> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_PLANS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new FitnessPlan(
                        rs.getInt("planID"),
                        rs.getTimestamp("startDate").toLocalDateTime(),
                        rs.getInt("duration"),
                        parsePlanStatus(rs.getString("status")),
                        rs.getInt("consultantID")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private void insertPersonalDetails(Connection conn, int planID, String goals) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_PERSONAL_PLAN)) {
            stmt.setInt(1, planID);
            stmt.setString(2, goals);
            stmt.executeUpdate();
        }
    }

    private void insertGroupDetails(Connection conn, int planID, int min, int max, String guidelines, ArrayList<String> prefs) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_GROUP_PLAN)) {
            stmt.setInt(1, planID);
            stmt.setInt(2, min);
            stmt.setInt(3, max);
            stmt.setString(4, guidelines);
            stmt.executeUpdate();
        }
        if (prefs != null) {
            try (PreparedStatement stmtPref = conn.prepareStatement(Consts.SQL_INS_GROUP_PREF)) {
                for (String type : prefs) {
                    stmtPref.setInt(1, planID);
                    stmtPref.setString(2, type);
                    stmtPref.executeUpdate();
                }
            }
        }
    }
    
    private void assignTraineeToPlan(Connection conn, int planID, int traineeID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_TRAINEE_ASSIGNMENT)) {
            stmt.setInt(1, planID);
            stmt.setInt(2, traineeID);
            stmt.executeUpdate();
        }
    }

    private PlanStatus parsePlanStatus(String s) {
        try { return PlanStatus.valueOf(s.trim()); }
        catch (Exception e) { return PlanStatus.Active; }
    }

    public boolean updateRestrictions(String planID, String newRestrictions, int dietitianID) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl()); 
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_UPD_DIETITIAN_RESTRICTIONS)) {
            
            stmt.setString(1, newRestrictions); 
            stmt.setInt(2, dietitianID);        
            stmt.setString(3, planID);          
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> getPlanDetails(String planID, String type) {
        Map<String, Object> details = new HashMap<>();
        String sql = type.equalsIgnoreCase("Personal") ? Consts.SQL_GET_PERSONAL_DETAILS : Consts.SQL_GET_GROUP_DETAILS;

        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (type.equalsIgnoreCase("Personal")) {
                        details.put("goals", rs.getString("goals"));
                        
                        try (PreparedStatement stmtTr = conn.prepareStatement(Consts.SQL_GET_TRAINEE_ID_BY_PLAN)) {
                            stmtTr.setInt(1, Integer.parseInt(planID));
                            try (ResultSet rsTr = stmtTr.executeQuery()) {
                                if (rsTr.next()) {
                                    details.put("traineeID", rsTr.getInt("traineeID"));
                                }
                            }
                        }

                    } else {
                        details.put("minAge", rs.getInt("minAge"));
                        details.put("maxAge", rs.getInt("maxAge"));
                        details.put("guidelines", rs.getString("guidelines"));

                        ArrayList<String> types = new ArrayList<>();
                        try (PreparedStatement stmtTypes = conn.prepareStatement(Consts.SQL_GET_PLAN_TYPES)) {
                            stmtTypes.setString(1, planID);
                            try (ResultSet rsTypes = stmtTypes.executeQuery()) {
                                while (rsTypes.next()) {
                                    types.add(rsTypes.getString("typeName"));
                                }
                            }
                        }
                        details.put("preferredClasses", types);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public ArrayList<Object[]> getAllPlansWithType() {
        ArrayList<Object[]> plans = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_ALL_PLANS_WITH_TYPE);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                plans.add(new Object[]{
                    false,
                    rs.getString("planID"),
                    rs.getString("planType"),
                    rs.getDate("startDate"),
                    rs.getInt("duration"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    public ArrayList<String[]> getTraineesForDietitian() {
        ArrayList<String[]> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl()); 
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_DIETITIAN_DATA);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String planId = rs.getString("planID");
                String restrictions = rs.getString("dietaryRestrictions");
                if (planId == null) planId = "";
                if (restrictions == null) restrictions = "";
                results.add(new String[]{
                    rs.getString("firstName") + " " + rs.getString("lastName"),
                    rs.getString("ID"),
                    planId,
                    restrictions
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    
    public ArrayList<GroupPlan> getActiveGroupPlans() {
        ArrayList<GroupPlan> list = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_GET_ACTIVE_GROUP_PLANS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new GroupPlan(
                    rs.getInt("planID"),
                    rs.getTimestamp("startDate").toLocalDateTime(),
                    rs.getInt("duration"),
                    PlanStatus.Active,
                    rs.getInt("consultantID"),
                    rs.getInt("minAge"),
                    rs.getInt("maxAge"),
                    rs.getString("guidelines")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public boolean assignTraineesToPlan(int planID, ArrayList<Integer> traineeIDs) {
        if (traineeIDs == null || traineeIDs.isEmpty()) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
            conn.setAutoCommit(false); 

            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_TRAINEE_ASSIGNMENT)) {
                for (int traineeID : traineeIDs) {
                    stmt.setInt(1, planID);     
                    stmt.setInt(2, traineeID);
                    stmt.executeUpdate();     
                }
            }
            
            conn.commit(); 
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); 
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public String validatePlanInput(int duration, boolean isIndividual, String goals, int minAge, int maxAge, String guidelines, ArrayList<String> preferredClasses) {
        if (duration <= 0)
            return "Duration must be a positive number.";
        if (isIndividual) {
            if (goals == null || goals.trim().isEmpty())
                return "Please enter goals for the trainee.";
            if (!goals.matches(".*[a-zA-Z\u0590-\u05FF].*"))
                return "Goals must contain text, not only numbers.";
        } else {
            if (minAge <= 0 || maxAge <= 0)
                return "Age must be a positive number.";
            if (minAge > maxAge)
                return "Min Age cannot be greater than Max Age.";
            if (preferredClasses == null || preferredClasses.isEmpty())
                return "Please select at least one preferred class type.";
            if (guidelines == null || guidelines.trim().isEmpty())
                return "Please enter guidelines for the group plan.";
        }
        return null;
    }
}