package entity;

import java.util.Objects;

public class ClassRegistrations {
	//Fields
	private int classID;
	private int traineeID;
	//Constructor
	public ClassRegistrations(int classID, int traineeID) {
		this.classID = classID;
		this.traineeID = traineeID;
	}
	//Getters
	public int getClassID() {
		return classID;
	}

	public int getTraineeID() {
		return traineeID;
	}
	//Setters
	public void setClassID(int classID) {
		this.classID = classID;
	}

	public void setTraineeID(int traineeID) {
		this.traineeID = traineeID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(classID, traineeID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassRegistrations other = (ClassRegistrations) obj;
		return classID == other.classID && traineeID == other.traineeID;
	}

	@Override
	public String toString() {
		return "ClassRegistrations [classID=" + classID + ", traineeID=" + traineeID + "]";
	}
}