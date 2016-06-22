package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_actions", catalog="gis", schema="public")

public class ActionM extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_actions_id_seq")

    private int id;

    private String phenomenon;
    private String category;
    private String description;
    private String phase;
    private String body;
    private String implementing_body;
    private String participating_body;
    private String agency;

    public ActionM() {}

    public ActionM(String phenomenon, String phase, String description, String category, String body, String implementing_body, String participating_body){
        this.phenomenon = phenomenon;
        this.phase = phase;
        this.description = description;
        this.category = category;
        this.body = body;
        this.implementing_body = implementing_body;
        this.participating_body = participating_body;

        this.save();
    }

    public int getId_actions() { return id; }

    public void setId_actions(int id_actions) { this.id = id_actions; }

    public String getPhenomenon() {  return phenomenon; }

    public void setPhenomenon(String phenomenon) { this.phenomenon = phenomenon; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getPhase() { return phase; }

    public void setPhase(String phase) { this.phase = phase; }

    public String getBody() { return body; }

    public void setBody(String body) { this.body = body; }

    public String getImplementing_body() { return implementing_body; }

    public void setImplementing_body(String implementing_body) {
        if(implementing_body.equals(' ')){
            this.implementing_body = "";
        }else{
            this.implementing_body = implementing_body;
        }
    }

    public String getParticipating_body() { return participating_body;}

    public void setParticipating_body(String participating_body) {
        if(participating_body.equals(' ')){
            this.participating_body = "";
        }else{
            this.participating_body = participating_body;
        }
     }

    public String getAgency() { return agency; }

    public void setAgency(String agency) { this.agency = agency; }

    // get/delete/store data from/to database

    public static Finder<Integer, ActionM> find = new Finder<Integer, ActionM>(ActionM.class);

    public static List<ActionM> all() { return find.all(); }

    public static void delete(Integer id){ find.ref(id).delete(); }

    public static void add(ActionM item) {
        item.save();
    }

    public static void update(ActionM item){item.update();}

    public String getAgencyDisplay() { return Sharing.find.where().eq("agency", agency).findUnique().getAgency_displayname(); }
}
