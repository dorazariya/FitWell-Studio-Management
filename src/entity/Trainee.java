package entity;

import java.util.Date;
import java.util.Objects;

public class Trainee {
	// Fields
	private int ID;
	private String firstName;
	private String lastName;
	private Date birthDate;
	private String email;
	private String phone;
	private UpdateMethod updateMethod;

	// Constructor
	public Trainee(int ID, String firstName, String lastName, Date birthDate, String email, String phone, UpdateMethod updateMethod) {
		this.ID = ID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.email = email;
		this.phone = phone;
		this.updateMethod = updateMethod;
	}

	// Getters
	public int getId() {
		return ID;
	}

	public String getFirstName() {
	    return (firstName != null && !firstName.trim().isEmpty()) ? firstName : "Trainee";
	}

	public String getLastName() {
	    return (lastName != null && !lastName.trim().isEmpty()) ? lastName : "";
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public UpdateMethod getUpdateMethod() {
		return updateMethod;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	// Setters
	public void setId(int ID) {
		this.ID = ID;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setUpdateMethod(UpdateMethod updateMethod) {
		this.updateMethod = updateMethod;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trainee other = (Trainee) obj;
		return ID == other.ID;
	}

	@Override
    public String toString() {
        return ID + " - " + firstName + " " + lastName;
    }
}