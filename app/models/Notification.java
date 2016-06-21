package models;


import com.avaje.ebean.Model;
import org.apache.commons.lang.StringEscapeUtils;
import play.mvc.Controller;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Entity
@Table(name="_notifications", catalog="gis", schema="public")

public class Notification extends Model {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="_notifications_id_seq")
    private int id;

    private String descr;

    private String type;

    private String username;

    private String agency;

    @Transient
    private String agencyDisplay;

    private boolean is_disaster;

    private Double lat;

    private Double lon;

    private String image;

    private LocalDateTime insert_date;

    @Transient
    private String contactGroups;

    private String disaster_date;

    private String disaster_duration;

    private String disaster_area;

    private String disaster_cause;

    private String disaster_characteristics;

    private String disaster_injuries;

    private String disaster_deaths;

    private String disaster_impacts_infrastructure;

    private String disaster_impacts_other;

    private String disaster_means_forces;

    private String disaster_action_list;

    private String disaster_remarks;

    private String disaster_suggestions;

    public String getDisaster_duration() {
        return disaster_duration;
    }

    public void setDisaster_duration(String disaster_duration) {
        this.disaster_duration = disaster_duration;
    }

    public String getDisaster_date() {
            return disaster_date;
    }
    public void setDisaster_date(String disaster_date) {
        this.disaster_date = disaster_date;
    }

    public String getDisaster_area() {
        return disaster_area;
    }

    public void setDisaster_area(String disaster_area) {
        this.disaster_area = disaster_area;
    }

    public String getDisaster_cause() {
        return disaster_cause;
    }

    public void setDisaster_cause(String disaster_cause) {
        this.disaster_cause = disaster_cause;
    }

    public String getDisaster_characteristics() {
        return disaster_characteristics;
    }

    public void setDisaster_characteristics(String disaster_characteristics) {
        this.disaster_characteristics = disaster_characteristics;
    }

    public String getDisaster_injuries() {
        return disaster_injuries;
    }

    public void setDisaster_injuries(String disaster_injuries) {
        this.disaster_injuries = disaster_injuries;
    }

    public String getDisaster_deaths() {
        return disaster_deaths;
    }

    public void setDisaster_deaths(String disaster_deaths) {
        this.disaster_deaths = disaster_deaths;
    }

    public String getDisaster_impacts_infrastructure() {
        return disaster_impacts_infrastructure;
    }

    public void setDisaster_impacts_infrastructure(String disaster_impacts_infrastructure) {
        this.disaster_impacts_infrastructure = disaster_impacts_infrastructure;
    }

    public String getDisaster_impacts_other() {
        return disaster_impacts_other;
    }

    public void setDisaster_impacts_other(String disaster_impacts_other) {
        this.disaster_impacts_other = disaster_impacts_other;
    }

    public String getDisaster_means_forces() {
        return disaster_means_forces;
    }

    public void setDisaster_means_forces(String disaster_means_forces) {
        this.disaster_means_forces = disaster_means_forces;
    }

    public String getDisaster_action_list() {
        return disaster_action_list;
    }

    public void setDisaster_action_list(String disaster_action_list) {
        this.disaster_action_list = disaster_action_list;
    }

    public String getDisaster_remarks() {
        return disaster_remarks;
    }

    public void setDisaster_remarks(String disaster_remarks) {
        this.disaster_remarks = disaster_remarks;
    }

    public String getDisaster_suggestions() {
        return disaster_suggestions;
    }

    public void setDisaster_suggestions(String disaster_suggestions) {
        this.disaster_suggestions = disaster_suggestions;
    }


    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescr() {
        return descr;
    }
    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean is_disaster() {
        return is_disaster;
    }

    public void setIs_disaster(boolean is_disaster) {
        this.is_disaster = is_disaster;
    }

    public LocalDateTime getInsert_date() {
        return insert_date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getInsert_dateFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDateTime = insert_date.format(formatter); // "1986-04-08 12:30"
        return formattedDateTime;
    }


    public String getInsert_dateFormattedReverse() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = insert_date.format(formatter);
        return formattedDateTime;
    }

    public String getContactGroups() {
        return contactGroups;
    }

    public void setContactGroups(String contactGroups) {
        this.contactGroups = contactGroups;
    }



    public String getAgencyDisplay() {
        return Sharing.find.where().eq("agency", agency).findUnique().getAgency_displayname();
    }

    public void setAgencyDisplay(String agencyDisplay) {
        this.agencyDisplay = agencyDisplay;
    }


    public String getDisasterAttributeMapping() {
        String attr = "";
        DisasterAttributeMapping mapping = DisasterAttributeMapping.find.where().eq("disaster_type", type).findUnique();
        if (mapping!=null){
            //System.out.println(Controller.lang().language());
            if (Controller.lang().language().equals("el")){
                attr =  mapping.getDisaster_duration()
                        + "#" + mapping.getDisaster_area()
                        + "#" + mapping.getDisaster_cause()
                        + "#" + mapping.getDisaster_characteristics()
                        + "#" + mapping.getDisaster_impacts_infrastructure()
                        + "#" + mapping.getDisaster_impacts_other()
                ;
            }
            else { // en
                attr =  mapping.getDisaster_duration_en()
                        + "#" + mapping.getDisaster_area_en()
                        + "#" + mapping.getDisaster_cause_en()
                        + "#" + mapping.getDisaster_characteristics_en()
                        + "#" + mapping.getDisaster_impacts_infrastructure_en()
                        + "#" + mapping.getDisaster_impacts_other_en()
                ;
            }

        }

        return StringEscapeUtils.unescapeHtml(attr);
//        return attr;
    }

    public boolean recent3days(){
        // Return events created in the last 3 days
        int diff  = (int)ChronoUnit.DAYS.between(insert_date, LocalDateTime.now());
        if (diff <= 3) {
            return true;
        }
        else
            return false;
    }
    public void setInsert_date(LocalDateTime insert_date) {
        this.insert_date = insert_date;
    }

    public static Finder<Integer, Notification> find = new Finder<Integer, Notification>(Notification.class);
    public static List<Notification> all() {
        return find.all();
    }

    public static List<Notification> allEvents() {
        return find.where().eq("is_disaster", false).findList();
    }

    public static List<Notification> allDisasters() {
        return find.where().eq("is_disaster", true).findList();
    }

    public static void create(Notification item) {
        item.save();
    }

    /**
     * Custom method that returns true if the notification - report corresponds to the logged in agency
     * @return
     */
    public boolean isOwn(){
//        System.out.println(play.mvc.Http.Context.current().session().get("agency") + "_" + agency);
        if (agency.equals(play.mvc.Http.Context.current().session().get("agency")))
             return true;
        else
            return false;
    }
}
