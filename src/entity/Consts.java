package entity;
import java.io.File;
public class Consts {
	// Path
	private static final String DB_RELATIVE_PATH = "database/Fitwell.accdb";

	// Reports
	public static final String UNREGISTERED_REPORT_JASPER = "/boundary/UnregisteredClassReport.jasper";
	public static final String INVENTORY_REPORT_JASPER = "/boundary/RptInventory.jasper";
	public static final String SQL_INVENTORY_FOR_XML =
		"SELECT t.category, t.typeName, " +
		"(SELECT COUNT(*) FROM TblClassEquipmentRequirements r " +
	    " INNER JOIN TblTrainingClass c ON r.classID = c.classID " +
	    " WHERE r.typeName = t.typeName " +
	    " AND Year(c.startTime) = Year(Now()) " +
	    " AND c.status = 'COMPLETED') AS usageCount " +
	    "FROM TblEquipmentType t " +
	    "ORDER BY t.category, t.typeName";

	// JDBC
	private static String getDbAbsolutePath() {
		return new File(DB_RELATIVE_PATH).getAbsolutePath();
	}
	public static final String CONN_STR = "jdbc:ucanaccess://" + getDbAbsolutePath() + ";COLUMNORDER=DISPLAY";
	public static String getDbJdbcUrl() {
		return CONN_STR;
	}
	
	// Equipment Type Queries
	public static final String SQL_SEL_FLAGGED_TYPES = "SELECT * FROM TblEquipmentType WHERE flaggedForReview = true";
	public static final String SQL_INS_NEW_TYPE = "INSERT INTO TblEquipmentType (typeName, description, category, totalQuantity, extractionConfidence, flaggedForReview) VALUES (?, ?, ?, ?, ?, ?)";
	public static final String SQL_UPD_FULL_TYPE = "UPDATE TblEquipmentType SET description=?, category=?, totalQuantity=?, extractionConfidence=?, flaggedForReview=? WHERE typeName=?";
	public static final String SQL_UPD_APPROVE_TYPE = "UPDATE TblEquipmentType SET typeName = ?, description = ?, category = ?, extractionConfidence = 100, flaggedForReview = false WHERE typeName = ?";
	public static final String SQL_EXISTS_TYPE = "SELECT COUNT(*) FROM TblEquipmentType WHERE typeName = ?";
	public static final String SQL_SEL_TYPE_NAME = "SELECT typeName FROM TblEquipmentType WHERE typeName = ?";
	public static final String SQL_DEC_TYPE_QTY = "UPDATE TblEquipmentType SET totalQuantity = totalQuantity - 1 WHERE typeName = ? AND totalQuantity > 0";
	
	// Equipment Item Queries
	public static final String SQL_SEL_ITEM_BY_SN = "SELECT * FROM TblEquipmentItem WHERE serialNumber = ?";
	public static final String SQL_SEL_TYPE_BY_SN = "SELECT typeName FROM TblEquipmentItem WHERE serialNumber = ?";
	public static final String SQL_UPD_DISABLE_ITEM = "UPDATE TblEquipmentItem SET status = 'OUTOFSERVICE' WHERE serialNumber = ?";
	public static final String SQL_UPD_ENABLE_ITEM = "UPDATE TblEquipmentItem SET status = 'AVAILABLE' WHERE serialNumber = ?";
	public static final String SQL_REM_ITEM = "UPDATE TblEquipmentItem SET status = 'REMOVED' WHERE serialNumber = ?";
	public static final String SQL_GET_FULL_ITEM_DETAILS =
		"SELECT i.serialNumber, i.typeName, i.status, t.category " +
		"FROM TblEquipmentItem i " +
	    "INNER JOIN TblEquipmentType t ON i.typeName = t.typeName " +
	    "WHERE i.serialNumber = ?";
	
	// Training Class Queries
	public static final String SQL_INS_CLASS = "INSERT INTO TblTrainingClass (name, startTime, endTime, typeName, maxParticipants, planID, consultantID, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String SQL_INS_TIP = "INSERT INTO TblTip (content, URL, classID) VALUES (?, ?, ?)";
	public static final String SQL_UPD_CLASS_TIME = "UPDATE TblTrainingClass SET startTime=?, endTime=? WHERE classID=?";
	public static final String SQL_CANCEL_CLASS = "UPDATE TblTrainingClass SET status = 'CANCELLED' WHERE classID = ?";
	
	// Registration Queries
	public static final String SQL_INS_REGISTRATION = "INSERT INTO TblClassRegistrations (classID, traineeID) VALUES (?, ?)";
	public static final String SQL_DEL_REGISTRATION = "DELETE FROM TblClassRegistrations WHERE classID=? AND traineeID=?";
	
	// EMERGENCY LOGIC
	// Emergency Logic: Suspend all SCHEDULED and ACTIVE classes
	public static final String SQL_ACTIVATE_EMERGENCY = "UPDATE TblTrainingClass SET status = 'SUSPENDED' " + "WHERE status IN ('SCHEDULED', 'ACTIVE')";
	
	// Release: restore SUSPENDED classes based on their times
	public static final String SQL_RELEASE_EMERGENCY_FUTURE = "UPDATE TblTrainingClass SET status = 'SCHEDULED' WHERE status = 'SUSPENDED' AND startTime > Now()";
	public static final String SQL_RELEASE_EMERGENCY_ONGOING = "UPDATE TblTrainingClass SET status = 'ACTIVE' WHERE status = 'SUSPENDED' AND startTime <= Now() AND endTime > Now()";
	public static final String SQL_RELEASE_EMERGENCY_FINISHED = "UPDATE TblTrainingClass SET status = 'COMPLETED' WHERE status = 'SUSPENDED' AND endTime <= Now()";
	public static final String SQL_GET_CLASS_INFO = "SELECT name, typeName, consultantID, startTime, endTime, maxParticipants, status, planID FROM TblTrainingClass WHERE classID=?";
	public static final String SQL_GET_PLAN_ID_BY_CLASS = "SELECT planID FROM TblTrainingClass WHERE classID = ?";
	public static final String SQL_GET_PLAN_STATUS = "SELECT status FROM TblFitnessPlan WHERE planID=?";
	public static final String SQL_CHECK_DUPLICATE_REG = "SELECT traineeID FROM TblClassRegistrations WHERE classID=? AND traineeID=?";
	public static final String SQL_COUNT_PARTICIPANTS = "SELECT COUNT(*) AS total FROM TblClassRegistrations WHERE classID=?";
	public static final String SQL_CHECK_TRAINEE_ASSIGNED_TO_PLAN = "SELECT 1 FROM TblTraineePlanAssignments WHERE traineeID=? AND planID=?";
	public static final String SQL_SEL_TRAINEES_BY_PLAN = "SELECT t.* FROM TblTrainee AS t INNER JOIN TblTraineePlanAssignments AS a ON t.ID = a.traineeID WHERE a.planID = ? AND NOT EXISTS (SELECT 1 FROM TblClassRegistrations AS r WHERE r.classID = ? AND r.traineeID = t.ID) AND NOT EXISTS (SELECT 1 FROM TblClassRegistrations AS r2 INNER JOIN TblTrainingClass AS c2 ON r2.classID = c2.classID WHERE r2.traineeID = t.ID AND c2.classID <> ? AND c2.startTime < ? AND c2.endTime > ?)";
	public static final String SQL_CHECK_TRAINEE_OVERLAP = "SELECT 1 FROM TblClassRegistrations AS r INNER JOIN TblTrainingClass AS c ON r.classID = c.classID WHERE r.traineeID = ? AND c.classID <> ? AND c.startTime < ? AND c.endTime > ?";
	
	// Auto-status updates (called on every read to keep statuses fresh)
	public static final String SQL_AUTO_TO_ACTIVE = "UPDATE TblTrainingClass SET status = 'ACTIVE' WHERE status = 'SCHEDULED' AND startTime <= Now() AND endTime > Now()";
	
	// ACTIVE to COMPLETED when class end time has passed
	public static final String SQL_AUTO_TO_COMPLETED = "UPDATE TblTrainingClass SET status = 'COMPLETED' WHERE status = 'ACTIVE' AND endTime <= Now()";
	public static final String SQL_SEL_CLASSES = "SELECT classID, name, startTime, endTime, typeName, maxParticipants, planID, consultantID, status FROM TblTrainingClass";
	public static final String SQL_SEL_TRAINEES = "SELECT * FROM TblTrainee";
	public static final String SQL_SEL_TYPES = "SELECT typeName, description FROM TblClassType";
	public static final String SQL_SEL_CONSULTANTS = "SELECT ID, firstName, lastName FROM TblFitnessConsultant";
	public static final String SQL_SEL_PLANS = "SELECT * FROM TblFitnessPlan WHERE status='Active'";
	public static final String SQL_SEL_TRAINEES_IN_CLASS = "SELECT TblTrainee.* FROM TblTrainee INNER JOIN TblClassRegistrations ON TblTrainee.ID = TblClassRegistrations.traineeID WHERE TblClassRegistrations.classID = ?";
	
	// Group Plan
	public static final String SQL_SEL_CLASS_TYPES = "SELECT typeName FROM TblClassType";
	public static final String SQL_GET_ACTIVE_GROUP_PLANS =
	    "SELECT p.planID, p.startDate, p.duration, p.status, p.consultantID, " +
	    "g.minAge, g.maxAge, g.guidelines " +
	    "FROM TblFitnessPlan p INNER JOIN TblGroupPlan g ON p.planID = g.planID " +
	    "WHERE p.status = 'Active'";
	
	// INSERTS
	public static final String SQL_INS_FITNESS_PLAN = "INSERT INTO TblFitnessPlan (startDate, duration, consultantID, status) VALUES (?, ?, ?, ?)";
	public static final String SQL_INS_PERSONAL_PLAN = "INSERT INTO TblPersonalPlan (planID, goals, dietitianID) VALUES (?, ?, NULL)";
	public static final String SQL_INS_TRAINEE_ASSIGNMENT = "INSERT INTO TblTraineePlanAssignments (planID, traineeID) VALUES (?, ?)";
	public static final String SQL_INS_GROUP_PLAN = "INSERT INTO TblGroupPlan (planID, minAge, maxAge, guidelines) VALUES (?, ?, ?, ?)";
	public static final String SQL_INS_GROUP_PREF = "INSERT INTO TblGroupPreferredTypes (planID, typeName) VALUES (?, ?)";
	
	// UPDATES
	public static final String SQL_UPD_FITNESS_PLAN = "UPDATE TblFitnessPlan SET startDate=?, duration=?, status=? WHERE planID=?";
	public static final String SQL_UPD_PERSONAL_PLAN = "UPDATE TblPersonalPlan SET goals=? WHERE planID=?";
	public static final String SQL_UPD_GROUP_PLAN = "UPDATE TblGroupPlan SET minAge=?, maxAge=?, guidelines=? WHERE planID=?";
	
	// DELETES
	public static final String SQL_DEL_TRAINEE_ASSIGNMENT = "DELETE FROM TblTraineePlanAssignments WHERE planID=?";
	public static final String SQL_DEL_GROUP_PREFS = "DELETE FROM TblGroupPreferredTypes WHERE planID=?";
	
	// Plan Queries
	public static final String SQL_SEL_ALL_PLANS_WITH_TYPE =
	    "SELECT p.planID, p.startDate, p.duration, p.status, " +
	    "CASE WHEN pp.planID IS NOT NULL THEN 'Personal' ELSE 'Group' END AS planType " +
	    "FROM TblFitnessPlan p " +
	    "LEFT JOIN TblPersonalPlan pp ON p.planID = pp.planID " +
	    "ORDER BY p.startDate DESC";
	public static final String SQL_GET_TRAINEE_ID_BY_PLAN = "SELECT traineeID FROM TblTraineePlanAssignments WHERE planID = ?";
	
	// Details Queries
	public static final String SQL_GET_PERSONAL_DETAILS = "SELECT goals FROM TblPersonalPlan WHERE planID = ?";
	public static final String SQL_GET_GROUP_DETAILS = "SELECT minAge, maxAge, guidelines FROM TblGroupPlan WHERE planID = ?";
	public static final String SQL_GET_PLAN_TYPES = "SELECT typeName FROM TblGroupPreferredTypes WHERE planID = ?";
	
	// Dietitian Queries
	public static final String SQL_SEL_DIETITIAN_DATA =
	    "SELECT t.ID, t.firstName, t.lastName, a.planID, p.dietaryRestrictions " +
	    "FROM (TblTrainee AS t " +
	    "INNER JOIN TblTraineePlanAssignments AS a ON t.ID = a.traineeID) " +
	    "INNER JOIN TblPersonalPlan AS p ON a.planID = p.planID";
	public static final String SQL_UPD_DIETITIAN_RESTRICTIONS = "UPDATE TblPersonalPlan SET dietaryRestrictions = ?, dietitianID = ? WHERE planID = ?";
	
	// Login
	public static final String SQL_SEL_TRAINEE_BY_ID = "SELECT * FROM TblTrainee WHERE ID = ?";
	public static final String SQL_SEL_CONSULTANT_BY_ID = "SELECT * FROM TblFitnessConsultant WHERE ID = ?";
	public static final String SQL_SEL_DIETITIAN_BY_ID = "SELECT * FROM TblDietitian WHERE ID = ?";
	public static final String SQL_SEL_MANAGER_BY_ID =
	    "SELECT c.ID, c.firstName, c.lastName " +
	    "FROM TblFitnessConsultant c " +
	    "INNER JOIN TblStudioManager m ON c.ID = m.ID " +
	    "WHERE m.ID = ?";
	
	// Status taken from DB, not computed
	public static final String SQL_SEL_AVAILABLE_CLASSES_FOR_TRAINEE =
	    "SELECT c.classID, c.name, c.startTime, c.endTime, c.typeName, c.maxParticipants, c.planID, c.consultantID, c.status AS status " +
	    "FROM TblTrainingClass c " +
	    "INNER JOIN TblTraineePlanAssignments a ON c.planID = a.planID " +
	    "WHERE a.traineeID = ? " +
	    "AND c.startTime > Now() " +
	    "AND c.status = 'SCHEDULED' " +
	    "AND NOT EXISTS (SELECT 1 FROM TblClassRegistrations r WHERE r.classID = c.classID AND r.traineeID = ?) " +
	    "ORDER BY c.startTime ASC";
	
	// Status taken from DB, not computed
	public static final String SQL_SEL_TRAINEE_SCHEDULE_HISTORY =
	    "SELECT c.classID, c.name, c.startTime, c.endTime, c.typeName, c.maxParticipants, c.planID, c.consultantID, c.status AS status " +
	    "FROM TblTrainingClass c " +
	    "INNER JOIN TblClassRegistrations r ON c.classID = r.classID " +
	    "WHERE r.traineeID = ? " +
	    "ORDER BY c.startTime DESC";
	public static final String SQL_UPD_TRAINEE_PROFILE = "UPDATE TblTrainee SET firstName=?, lastName=?, dateOfBirth=?, email=?, phoneNumber=?, updateMethod=? WHERE ID=?";
	public static final String SQL_INS_TRAINEE = "INSERT INTO TblTrainee (firstName, lastName, dateOfBirth, email, phoneNumber, updateMethod) VALUES (?, ?, ?, ?, ?, ?)";
	
	// Equipment Requirements Queries
	// Insert a single equipment requirement row for a class
	public static final String SQL_INS_EQUIPMENT_REQ = "INSERT INTO TblClassEquipmentRequirements (classID, typeName, requestQuantity) VALUES (?, ?, ?)";
	
	// Fetch all equipment requirements for a given class
	public static final String SQL_SEL_EQUIPMENT_BY_CLASS =
	    "SELECT classID, typeName, requestQuantity " +
	    "FROM TblClassEquipmentRequirements " +
	    "WHERE classID = ?";
	
	// Delete all equipment requirements for a given class (used when editing)
	public static final String SQL_DEL_EQUIPMENT_REQ_BY_CLASS = "DELETE FROM TblClassEquipmentRequirements WHERE classID = ?";
	
	// Availability check:
	// For each equipment type required by classID,
	// sum up ALL reservations from overlapping SCHEDULED/ACTIVE classes (excluding classID itself),
	// then check if (our request + others) exceeds totalQuantity.
	// Returns rows only for types that have a conflict.
	public static final String SQL_CHECK_EQUIPMENT_AVAILABILITY =
			"SELECT r.typeName, " +
			"r.requestQuantity AS ourRequest, " +
			"t.totalQuantity AS totalAvailable, " +
			"(SELECT SUM(r2.requestQuantity) " +
			"FROM TblClassEquipmentRequirements r2 " +
			"INNER JOIN TblTrainingClass c2 ON r2.classID = c2.classID " +
			"WHERE r2.typeName = r.typeName " +
			"AND r2.classID <> ? " +
			"AND c2.status IN ('SCHEDULED', 'ACTIVE') " +
			"AND c2.startTime < ? " +
			"AND c2.endTime > ?) AS othersReserved " +
			"FROM TblClassEquipmentRequirements r " +
			"INNER JOIN TblEquipmentType t ON r.typeName = t.typeName " +
			"WHERE r.classID = ?";
	public static final String SQL_GET_ALL_EQUIPMENT_TYPES =
	    "SELECT typeName, description, category, totalQuantity, extractionConfidence, flaggedForReview " +
	    "FROM TblEquipmentType " +
	    "ORDER BY typeName";
	
	// Equipment stock summary: total, available, out of service per type
	public static final String SQL_GET_EQUIPMENT_STOCK_SUMMARY =
	    "SELECT t.typeName, t.category, t.totalQuantity, " +
	    "(SELECT COUNT(*) FROM TblEquipmentItem i WHERE i.typeName = t.typeName AND i.status = 'AVAILABLE') AS availableCount, " +
	    "(SELECT COUNT(*) FROM TblEquipmentItem i WHERE i.typeName = t.typeName AND i.status = 'OUTOFSERVICE') AS outOfServiceCount " +
	    "FROM TblEquipmentType t " +
	    "ORDER BY t.typeName";
	
	// Auto-Generate Serials from JSON
	public static final String SQL_COUNT_ITEMS_BY_TYPE = "SELECT COUNT(*) FROM TblEquipmentItem WHERE typeName = ? AND status <> 'REMOVED'";
	public static final String SQL_UPDATE_TYPE_EXACT_QTY = "UPDATE TblEquipmentType SET totalQuantity = ? WHERE typeName = ?";
	public static final String SQL_GET_MAX_SERIAL = "SELECT MAX(serialNumber) FROM TblEquipmentItem";
	public static final String SQL_INSERT_AUTO_ITEM =
	    "INSERT INTO TblEquipmentItem (serialNumber, typeName, locationX, locationY, shelfNumber, status) " +
	    "VALUES (?, ?, 0, 0, 1, 'AVAILABLE')";
	public static final String SQL_SYNC_TYPE_TOTAL = "UPDATE TblEquipmentType SET totalQuantity = ? WHERE typeName = ?";
}