package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="_agency_sharing", catalog="gis", schema="public")

public class Sharing extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_agency_sharing_id_seq")
    private int id;

    private String agency;

    private String agency_displayname;

    // Default value = true
    @Column(columnDefinition="tinyint(1) default 1")
    private boolean share;

    private LocalDateTime insert_date;

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public boolean isShare() {
        return share;
    }

    public void setShare(boolean share) {
        this.share = share;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getAgency_displayname() {
        return agency_displayname;
    }

    public void setAgency_displayname(String agency_displayname) {
        this.agency_displayname = agency_displayname;
    }
    public LocalDateTime getInsert_date() {
        return insert_date;
    }

    public void setInsert_date(LocalDateTime insert_date) {
        this.insert_date = insert_date;
    }

    public static Finder<Integer, Sharing> find = new Finder<Integer, Sharing>(Sharing.class);

    public static List<Sharing> all() {
        return find.all();
    }

    public static void create(Sharing item) {
       item.save();
    }

    public static void delete(Integer id){
        find.ref(id).delete();
    }

    public static void update(Sharing item){item.update();}

}
