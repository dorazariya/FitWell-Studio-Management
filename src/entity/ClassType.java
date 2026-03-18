package entity;

public class ClassType {
	//Fields
	private String typeName;
	private String description;
	
	//Constructor
	public ClassType(String typeName, String description) {
		super();
		this.typeName = typeName;
		this.description = description;
	}

	//Getters
	public String getTypeName() {
		return typeName;
	}

	public String getDescription() {
		return description;
	}
	
	//Setters
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return typeName;
	}
}