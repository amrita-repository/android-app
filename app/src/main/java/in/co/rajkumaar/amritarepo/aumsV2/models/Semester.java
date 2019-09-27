package in.co.rajkumaar.amritarepo.aumsV2.models;

public class Semester {
    private int id;
    private String semester;
    private String period;

    public Semester(int id, String semester, String period) {

        this.id = id;
        this.semester = semester;
        this.period = period;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
