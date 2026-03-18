package entity;

import java.util.Objects;

/* A class that represents a single physical piece of equipment in a system
Each item is identified by a serial number. */
public class EquipmentItem {	
	// Fields
	private int serialNumber; 
	private int locationX;
	private int locationY;
	private int shelfNumber;
	private ItemStatus status; 
	private String typeName;
    
	// Constructor
	public EquipmentItem(int serialNumber, int locationX, int locationY, int shelfNumber, String statusStr,
			String typeName) {
		this.serialNumber = serialNumber;
		this.locationX = locationX;
		this.locationY = locationY;
		this.shelfNumber = shelfNumber;
		this.typeName = typeName;
		
		if (statusStr != null && !statusStr.isEmpty()) {
			try {
				this.status = ItemStatus.valueOf(statusStr.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				this.status = ItemStatus.AVAILABLE;
			}
		} else {
			this.status = ItemStatus.AVAILABLE;
		}
	}
	
    // Getters
	public int getSerialNumber() {
		return serialNumber;
	}

	public int getLocationX() {
		return locationX;
	}

	public int getLocationY() {
		return locationY;
	}

	public int getShelfNumber() {
		return shelfNumber;
	}

	public ItemStatus getStatus() {
		return status;
	}

	public String getTypeName() {
		return typeName;
	}
	
    // Setters
	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	public void setLocationX(int locationX) {
		this.locationX = locationX;
	}

	public void setLocationY(int locationY) {
		this.locationY = locationY;
	}

	public void setShelfNumber(int shelfNumber) {
		this.shelfNumber = shelfNumber;
	}

	public void setStatus(ItemStatus status) {
		this.status = status;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	// Object methods
	@Override
	public int hashCode() {
		return Objects.hash(serialNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EquipmentItem other = (EquipmentItem) obj;
		return serialNumber == other.serialNumber;
	}

	@Override
	public String toString() {
		return "EquipmentItem [serialNumber=" + serialNumber + ", locationX=" + locationX + ", locationY=" + locationY
				+ ", shelfNumber=" + shelfNumber + ", status=" + status + ", typeName=" + typeName + "]";
	}
}