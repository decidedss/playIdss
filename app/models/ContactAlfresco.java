package models;

import com.avaje.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name="_contacts_alfresco", catalog="gis", schema="public")

public class ContactAlfresco extends Model {


    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="_contacts_alfresco_id_seq")
    private int contact_id;

    private String username;

    private int group_id;

    private String agency;

    public int getContact_id() {
        return contact_id;
    }

    public void setContact_id(int contact_id) {
        this.contact_id = contact_id;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public static Finder<Integer, ContactAlfresco> find = new Finder<>(ContactAlfresco.class);

    public static void create(ContactAlfresco item) {
        item.save();
    }

    public static void delete(Integer id){
        find.ref(id).delete();
    }


}
