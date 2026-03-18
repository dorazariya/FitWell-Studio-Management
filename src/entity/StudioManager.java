package entity;

public class StudioManager extends FitnessConsultant {
    public StudioManager(int id, String firstName, String lastName) {
        super(id, firstName, lastName);
    }
    @Override
    public String toString() {
        return "Manager" + getFirstName() + " " + getLastName();
    }
}