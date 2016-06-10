package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_actions_implementing_body", catalog="gis", schema="public")
public class ActionsImplemetingBody extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_actions_implementing_body_seq")

    private int id;
    private String title;
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

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public static Finder<Integer, ActionsImplemetingBody> find = new Finder<Integer, ActionsImplemetingBody>(ActionsImplemetingBody.class);

    public static List<ActionsImplemetingBody> all() { return find.all(); }
}
