package entity;

import java.time.LocalDateTime;

public class GroupPlan extends FitnessPlan {   
	// Fields
    private int minAge;
    private int maxAge;
    private String guidelines;
    
    // Constructor
    public GroupPlan(int planID, LocalDateTime startDate, int duration, PlanStatus status,int consultantID, int minAge, int maxAge, String guidelines) {      
        super(planID, startDate, duration, status, consultantID);     
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.guidelines = guidelines;
    }

    // Getters
    public int getMinAge() {
    	return minAge;
    }
    public int getMaxAge() {
    	return maxAge;
    }
    public String getGuidelines() {
    	return guidelines; 
    }

    // Setters
    public void setMinAge(int minAge) {
    	this.minAge = minAge;
    }
    public void setMaxAge(int maxAge) { 
    	this.maxAge = maxAge; 
    }
    public void setGuidelines(String guidelines) {
    	this.guidelines = guidelines; 
    }
    
    public String getAgeRangeString() {
        return minAge + "-" + maxAge;
    }
}