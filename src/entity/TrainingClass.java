package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class TrainingClass {
	//Fields
    private Integer classID;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String typeName;
    private int maxParticipants;
    private int planID;
    private int consultantID;
    private ClassStatus status;

    //Full Constructor
    public TrainingClass(Integer classID, String name, LocalDateTime startTime, LocalDateTime endTime, String typeName,
                         int maxParticipants, int planID, int consultantID, ClassStatus status) {
        this.classID = classID;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.typeName = typeName;
        this.maxParticipants = maxParticipants;
        this.planID = planID;
        this.consultantID = consultantID;
        this.status = status;
    }
    
    //Constructor for creating a new class
    public TrainingClass(String name, LocalDateTime startTime, LocalDateTime endTime, String typeName,
			int maxParticipants, int planID, int consultantID, ClassStatus status) {
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.typeName = typeName;
		this.maxParticipants = maxParticipants;
		this.planID = planID;
		this.consultantID = consultantID;
		this.status = status;
	}

    //Getters
	public Integer getClassID() {
        return classID;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public int getPlanID() {
        return planID;
    }

    public int getConsultantID() {
        return consultantID;
    }

    public ClassStatus getStatus() {
        return status;
    }

    //Setters
    public void setClassID(Integer classID) {
        this.classID = classID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setPlanID(int planID) {
        this.planID = planID;
    }

    public void setConsultantID(int consultantID) {
        this.consultantID = consultantID;
    }

    public void setStatus(ClassStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(classID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TrainingClass other = (TrainingClass) obj;

        if (this.classID == null || other.classID == null) return false;

        return Objects.equals(this.classID, other.classID);
    }

    @Override
    public String toString() {
        return "TrainingClass [classID=" + classID + ", name=" + name + ", startTime=" + startTime + ", endTime="
                + endTime + ", typeName=" + typeName + ", maxParticipants=" + maxParticipants + ", planID=" + planID
                + ", consultantID=" + consultantID + ", status=" + status + "]";
    }
}
