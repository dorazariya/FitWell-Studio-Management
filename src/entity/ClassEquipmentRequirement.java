package entity;

public class ClassEquipmentRequirement {
	// Fields
    private int classID;
    private String typeName;
    private int requiredQuantity;

    // Empty constructor
    public ClassEquipmentRequirement() {
    }

    // Full constructor
    public ClassEquipmentRequirement(int classID, String typeName, int requiredQuantity) {
        this.classID = classID;
        this.typeName = typeName;
        this.requiredQuantity = requiredQuantity;
    }

    // Getters
    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public String getTypeName() {
        return typeName;
    }

    // Setters
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    @Override
    public String toString() {
        return "ClassEquipmentRequirement{" + " classID= " + classID + ", typeName='" + typeName + '\'' + ", requiredQuantity=" + requiredQuantity + '}';
    }
}