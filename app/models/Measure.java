package models;

import com.avaje.ebean.Model;
import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="_measures", catalog="gis", schema="public")

public class Measure extends Model {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="_measures_id_seq")
    private  int id;

    private String name;
    private String category;
    private String location;
    private String description;
    private String budget;
    private String riskaddressing;
    private Date startdate;
    private Date enddate;
    private String agency;

    public Measure() {}

    public Measure(String name, String category, String location, String description, String budget, String riskaddressing, Date startdate, Date enddate, String agency){
        this.name = name;
        this.category = category;
        this.location = location;
        this.description = description;
        this.budget = budget;
        this.riskaddressing = riskaddressing;
        this.startdate = startdate;
        this.enddate = enddate;
        this.agency = agency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getRiskaddressing() {
        return riskaddressing;
    }

    public void setRiskaddressing(String riskaddressing) {
        this.riskaddressing = riskaddressing;
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public String getStart() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.startdate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return month+1 +"/"+year;
    }
    public String getEnd() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.enddate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return month+1 +"/"+year;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public static Finder<Integer,Measure> find = new Finder<Integer,Measure>(Measure.class);

    public static List<Measure> all() {
        return find.all();
    }
    public static void create(Measure item) {
        item.save();
    }

     public static void delete(Integer id){
        find.ref(id).delete();
    }

    public static void update(Measure item){item.update();}


}
