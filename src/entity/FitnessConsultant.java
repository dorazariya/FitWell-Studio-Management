package entity;

public class FitnessConsultant {
	//Fields
	private int ID;
	private String firstName;
	private String lastName;
	
	//Constructor
	public FitnessConsultant(int ID, String firstName, String lastName) {
		super();
		this.ID = ID;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	//Getters
	public int getID() {
		return ID;
	}

	public String getFirstName() {
		return (firstName != null && !firstName.trim().isEmpty()) ? firstName : "Staff";
	}

	public String getLastName() {
		return (lastName != null && !lastName.trim().isEmpty()) ? lastName : "";
	}

	//Setters
	public void setID(int iD) {
		ID = iD;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return String.valueOf(ID);
	}
}