package entity;

public class Dietitian {
    private int id;
    private String firstName;
    private String lastName;

    public Dietitian(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters
	public int getId() {
		return id;
	}

	public String getFirstName() {
	    return (firstName != null && !firstName.trim().isEmpty()) ? firstName : "Dietitian";
	}

	public String getLastName() {
	    return (lastName != null && !lastName.trim().isEmpty()) ? lastName : "";
	}

	// Setters
	public void setId(int id) {
		this.id = id;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return "Dietitian [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + "]";
	}
}