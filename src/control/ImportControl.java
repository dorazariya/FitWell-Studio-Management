package control;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import entity.Consts;

/**
 * Controller for importing equipment data from JSON updates.
 * Implements "Gap Filling" logic: automatically generating serial numbers
 * for new equipment quantities reported in the JSON.
 */
public class ImportControl {
    private static ImportControl instance;

    private ImportControl() { }

    public static ImportControl getInstance() {
        if (instance == null)
            instance = new ImportControl();
        return instance;
    }

    // Entry point for JSON import
    public int importDefaultJson() {
        String jsonPath = System.getProperty("user.dir") + File.separator + "update.json";
        File file = new File(jsonPath);
        
        // Fallback for running inside IDE (look in src folder)
        if (!file.exists()) {
             jsonPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "update.json";
             file = new File(jsonPath);
        }
        
        if (!file.exists())
            throw new RuntimeException("Import file 'update.json' not found in project root.");
            
        return importDataFromJSON(jsonPath);
    }

    /**
     * Reads the JSON file and synchronizes the database.
     * Parse JSON object.
     * CREATE/UPDATE Type
     * CALCULATE GAP & INSERT 
     */
    public int importDataFromJSON(String jsonFilePath) {
        int itemsGeneratedCount = 0;

        try (FileReader reader = new FileReader(jsonFilePath)) {
            JsonArray objects = (JsonArray) Jsoner.deserialize(reader);

            for (Object item : objects) {
                if (!(item instanceof JsonObject)) continue;
                JsonObject obj = (JsonObject) item;

                // Extract Basic Fields
                String typeName = (String) obj.get("typeName");
                if (typeName == null || typeName.trim().isEmpty()) continue;
                typeName = typeName.trim();

                String description = (obj.get("description") != null) ? obj.get("description").toString().trim() : "";
                String category    = (obj.get("category")    != null) ? obj.get("category").toString().trim()    : "General";

                // Extract Quantity
                int targetQuantity = 0;
                try {
                    Object qtyObj = obj.get("totalQuantity");
                    if (qtyObj != null) targetQuantity = Integer.parseInt(qtyObj.toString().trim());
                } catch (NumberFormatException e) { continue; }

                // Image Logic & Confidence Scoring
                int confidence;
                boolean flagged;
                String imageUrl = (obj.get("imageUrl") != null) ? obj.get("imageUrl").toString().trim() : null;

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    boolean imageDownloaded = downloadImage(imageUrl, typeName);
                    if (!imageDownloaded) confidence = 20;
                    else if (description.isEmpty() || category.equals("General")) confidence = 40;
                    else confidence = 60;
                    flagged = true;
                } else {
                    // Manual/Text-only update -> 100% confidence
                    confidence = 100;
                    flagged = false;
                }

                //  CREATE/UPDATE PARENT TYPE
                //  We must ensure the 'typeName' exists in TblEquipmentType 
                //  before we can insert items into TblEquipmentItem.
                EquipmentControl.getInstance().insertOrUpdateType(typeName, description, category, targetQuantity, confidence, flagged);

                if (targetQuantity > 0) {
                    itemsGeneratedCount += syncItemQuantity(typeName, targetQuantity);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Import failed: " + e.getMessage());
        }

        return itemsGeneratedCount;
    }

    /**
     * Synchronizes the physical inventory count with the target quantity from JSON.
     * If (Target > Actual), it auto-generates new items with sequential Serial Numbers.
     */
    private int syncItemQuantity(String typeName, int targetQuantity) {
        int created = 0;
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl())) {
            
            // Check how many items we actually have in DB (Active items only)
            int currentCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_COUNT_ITEMS_BY_TYPE)) {
                stmt.setString(1, typeName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) currentCount = rs.getInt(1);
                }
            }

            // If JSON says we need more than we have -> Create the missing items
            if (targetQuantity > currentCount) {
                int gap = targetQuantity - currentCount;
                
                // Find the highest existing Serial Number to start from
                int nextSerial = 1000; 
                try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_GET_MAX_SERIAL);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        nextSerial = rs.getInt(1) + 1;
                    }
                }

                // Batch Insert the new items
                try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INSERT_AUTO_ITEM)) {
                    for (int i = 0; i < gap; i++) {
                        stmt.setInt(1, nextSerial + i);
                        stmt.setString(2, typeName);
                        stmt.executeUpdate();
                        created++;
                    }
                }
                
                // Ensure the Type table reflects the exact total quantity
                try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SYNC_TYPE_TOTAL)) {
                    stmt.setInt(1, targetQuantity);
                    stmt.setString(2, typeName);
                    stmt.executeUpdate();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return created;
    }

    // Helper method to download an image from a URL and save it locally.
    private boolean downloadImage(String imageUrl, String typeName) {
        try {
            String imagesDir = System.getProperty("user.dir") + File.separator + "images";
            Files.createDirectories(Paths.get(imagesDir));
            
            String safeFileName = typeName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String savePath = imagesDir + File.separator + safeFileName + ".jpg";

            URL url = new URL(imageUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, Paths.get(savePath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed download: " + e.getMessage());
            return false;
        }
    }
}