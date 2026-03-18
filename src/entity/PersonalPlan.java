package entity;

import java.time.LocalDateTime;

public class PersonalPlan extends FitnessPlan {
    // Fields
    private String goals;
    private String dietaryRest;
    private int dietitianID;
    
    // Constructor
    public PersonalPlan(int planID, LocalDateTime startDate, int duration, PlanStatus status, int consultantID, String goals, String dietaryRest, int dietitianID) {
        super(planID, startDate, duration, status, consultantID);
        this.goals = goals;
        this.dietaryRest = dietaryRest;
        this.dietitianID = dietitianID;
    }

    // Getters
	public String getGoals() {
		return goals;
	}

	public String getDietaryRest() {
		return dietaryRest;
	}

	public int getDietitianID() {
		return dietitianID;
	}

	// Setters
	public void setGoals(String goals) {
		this.goals = goals;
	}

	public void setDietaryRest(String dietaryRest) {
		this.dietaryRest = dietaryRest;
	}

	public void setDietitianID(int dietitianID) {
		this.dietitianID = dietitianID;
	}
}