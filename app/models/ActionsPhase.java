package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_actions_phase", catalog="gis", schema="public")
public class ActionsPhase extends Model {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_actions_phase_seq")

    private int id;
    private String title;
    private String title_en;
    private String agency;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public static Finder<Integer, ActionsPhase> find = new Finder<Integer, ActionsPhase>(ActionsPhase.class);

    public static List<ActionsPhase> all() { return find.all(); }
}
