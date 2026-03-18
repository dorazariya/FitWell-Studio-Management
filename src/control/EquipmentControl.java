package control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import entity.Consts;
import entity.EquipmentItem;
import entity.EquipmentType;
import entity.ItemStatus;

public class EquipmentControl {
    private static EquipmentControl instance;

    private EquipmentControl() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static EquipmentControl getInstance() {
        if (instance == null) instance = new EquipmentControl();
        return instance;
    }

    //  EQUIPMENT TYPE OPERATIONS
    public ArrayList<EquipmentType> getAllEquipmentTypes() {
        ArrayList<EquipmentType> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_GET_ALL_EQUIPMENT_TYPES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new EquipmentType(
                    rs.getString("typeName"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("totalQuantity"),
                    rs.getInt("extractionConfidence"),
                    rs.getBoolean("flaggedForReview")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<EquipmentType> getFlaggedEquipmentTypes() {
        ArrayList<EquipmentType> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_FLAGGED_TYPES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(new EquipmentType(
                    rs.getString("typeName"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("totalQuantity"),
                    rs.getInt("extractionConfidence"),
                    rs.getBoolean("flaggedForReview")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public boolean approveEquipmentType(String originalTypeName, String newTypeName, String newDesc, String newCat) {
        if (newTypeName == null || newTypeName.trim().isEmpty())
            throw new RuntimeException("Type Name cannot be empty.");
        if (newDesc == null || newDesc.trim().isEmpty())
            throw new RuntimeException("Description cannot be empty.");
        if (newCat == null || newCat.trim().isEmpty())
            throw new RuntimeException("Category cannot be empty.");

        newTypeName = newTypeName.trim();
        newDesc = newDesc.trim();
        newCat = newCat.trim();

        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            if (!newTypeName.equalsIgnoreCase(originalTypeName)) {
                try (PreparedStatement check = conn.prepareStatement(Consts.SQL_EXISTS_TYPE)) {
                    check.setString(1, newTypeName);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0)
                            throw new RuntimeException("Type Name already exists. Please choose a different name.");
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_UPD_APPROVE_TYPE)) {
                stmt.setString(1, newTypeName);
                stmt.setString(2, newDesc);
                stmt.setString(3, newCat);
                stmt.setString(4, originalTypeName);
                return stmt.executeUpdate() > 0;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("System error: Database operation failed");
        }
    }

    public boolean insertOrUpdateType(String name, String desc, String cat, int qty, int conf, boolean flag) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement checkStmt = conn.prepareStatement(Consts.SQL_SEL_TYPE_NAME)) {

            checkStmt.setString(1, name);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_UPD_FULL_TYPE)) {
                        stmt.setString(1, desc);
                        stmt.setString(2, cat);
                        stmt.setInt(3, qty);
                        stmt.setInt(4, conf);
                        stmt.setBoolean(5, flag);
                        stmt.setString(6, name);
                        return stmt.executeUpdate() > 0;
                    }
                } else {
                    try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_NEW_TYPE)) {
                        stmt.setString(1, name);
                        stmt.setString(2, desc);
                        stmt.setString(3, cat);
                        stmt.setInt(4, qty);
                        stmt.setInt(5, conf);
                        stmt.setBoolean(6, flag);
                        return stmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //  EQUIPMENT ITEM OPERATIONS
    public EquipmentItem findItemBySn(int sn) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_ITEM_BY_SN)) {

            stmt.setInt(1, sn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new EquipmentItem(
                        rs.getInt("serialNumber"),
                        rs.getInt("locationX"),
                        rs.getInt("locationY"),
                        rs.getInt("shelfNumber"),
                        rs.getString("status"),
                        rs.getString("typeName")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String[] getItemDisplayDetails(int sn) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_GET_FULL_ITEM_DETAILS)) {

            stmt.setInt(1, sn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[] {
                        rs.getString("typeName"), 
                        rs.getString("category"),  
                        rs.getString("status")     
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; 
    }

    public boolean manageItemStatus(int sn, ItemStatus targetStatus) {
        EquipmentItem item = findItemBySn(sn);
        if (item == null)
            throw new RuntimeException("Item SN " + sn + " does not exist in the system.");

        if (targetStatus == ItemStatus.REMOVED) {
            if (item.getStatus() != ItemStatus.OUTOFSERVICE)
                throw new RuntimeException("Business Rule Violation: Only items in 'Out Of Service' status can be removed.");
            
            return removeItemAndDecreaseTypeQuantity(sn, item.getTypeName());
        }
       
        if (targetStatus == ItemStatus.OUTOFSERVICE) {
            return runUpdateQuery(Consts.SQL_UPD_DISABLE_ITEM, sn);
        }

        if (targetStatus == ItemStatus.AVAILABLE) {
            return runUpdateQuery(Consts.SQL_UPD_ENABLE_ITEM, sn);
        }
        
        return false;
    }

    //  INVENTORY VIEW
    public ArrayList<Object[]> getEquipmentStockSummary() {
        ArrayList<Object[]> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(Consts.SQL_GET_EQUIPMENT_STOCK_SUMMARY);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("typeName"),
                    rs.getString("category"),
                    rs.getInt("totalQuantity"),
                    rs.getInt("availableCount"),
                    rs.getInt("outOfServiceCount")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //  PRIVATE HELPERS
    private boolean runUpdateQuery(String sql, int sn) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sn);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean removeItemAndDecreaseTypeQuantity(int sn, String typeName) {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            conn.setAutoCommit(false);           
            try {
                try (PreparedStatement stmtRemove = conn.prepareStatement(Consts.SQL_REM_ITEM)) {
                    stmtRemove.setInt(1, sn);
                    int rows = stmtRemove.executeUpdate();
                    if (rows == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                int realCount = 0;
                try (PreparedStatement stmtCount = conn.prepareStatement(Consts.SQL_COUNT_ITEMS_BY_TYPE)) {
                    stmtCount.setString(1, typeName);
                    try (ResultSet rs = stmtCount.executeQuery()) {
                        if (rs.next()) {
                            realCount = rs.getInt(1);
                        }
                    }
                }

                try (PreparedStatement stmtUpdateQty = conn.prepareStatement(Consts.SQL_UPDATE_TYPE_EXACT_QTY)) {
                    stmtUpdateQty.setInt(1, realCount);
                    stmtUpdateQty.setString(2, typeName);
                    stmtUpdateQty.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception ex) {
                conn.rollback(); 
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}