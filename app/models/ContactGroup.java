package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_contacts_groups", catalog="gis", schema="public")

public class ContactGroup extends Model {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="_contacts_groups_id_seq")
    private  int id;

    private String groupname;

    private String agency;

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public static Finder<Integer, ContactGroup> find = new Finder<Integer, ContactGroup>(ContactGroup.class);

    public static List<ContactGroup> all() {
        return find.all();
    }

    public static void create(ContactGroup item) {
       item.save();
    }

     public static void delete(Integer id){
        find.ref(id).delete();
    }



}
