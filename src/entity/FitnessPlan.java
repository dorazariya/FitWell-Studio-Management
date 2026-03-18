package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class FitnessPlan {
	//Field
	private int planID;
	private LocalDateTime startDate;
	private int duration;
	private PlanStatus status;
	private int consultantID;
	
	//Constructor
	public FitnessPlan(int planID, LocalDateTime startDate, int duration, PlanStatus status, int consultantID) {
		super();
		this.planID = planID;
		this.startDate = startDate;
		this.duration = duration;
		this.status = status;
		this.consultantID = consultantID;
	}

	//Getters
	public int getPlanID() {
		return planID;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public int getDuration() {
		return duration;
	}

	public PlanStatus getStatus() {
		return status;
	}

	public int getConsultantID() {
		return consultantID;
	}

	//Setters
	public void setPlanID(int planID) {
		this.planID = planID;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setStatus(PlanStatus status) {
		this.status = status;
	}

	public void setConsultantID(int consultantID) {
		this.consultantID = consultantID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(planID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FitnessPlan other = (FitnessPlan) obj;
		return planID == other.planID;
	}

	@Override
	public String toString() {
		return String.valueOf(planID);
	}
}