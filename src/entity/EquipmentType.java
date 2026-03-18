package entity;

import java.util.Objects;
// This class contains the general information common to all items of the same type.
public class EquipmentType {
	//Fields
	private String typeName;	
	private String description;
	private String category;
	private int totalQuantity;
	private int extractionConfidence;
	private boolean flaggedForReview;
	
	// Constructor
	public EquipmentType(String typeName, String description, String category, int totalQuantity,
			int extractionConfidence, boolean flaggedForReview) {
		this.typeName = typeName;
		this.description = description;
		this.category = category;
		this.totalQuantity = totalQuantity;
		this.extractionConfidence = extractionConfidence;
		this.flaggedForReview = flaggedForReview;
	}
    // Getters
	public String getTypeName() {
		return typeName;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}

	public int getExtractionConfidence() {
		return extractionConfidence;
	}

	public boolean isFlaggedForReview() {
		return flaggedForReview;
	}
    // Setters
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public void setExtractionConfidence(int extractionConfidence) {
		this.extractionConfidence = extractionConfidence;
	}
	
	public void setFlaggedForReview(boolean flaggedForReview) {
		this.flaggedForReview = flaggedForReview;
	}
	
	//Object methods
	@Override
	public int hashCode() {
		return Objects.hash(typeName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EquipmentType other = (EquipmentType) obj;
		return Objects.equals(typeName, other.typeName);
	}

	public String toString() {
		return "EquipmentType [typeName=" + typeName + ", description=" + description + ", category=" + category
				+ ", totalQuantity=" + totalQuantity + ", extractionConfidence=" + extractionConfidence
				+ ", flaggedForReview=" + flaggedForReview + "]";
	}
}
