package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_contacts", catalog="gis", schema="public")

public class Contact extends Model {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="_contacts_id_seq")
    private int id;

    private String lastname;

    private String firstname;

    private String email;

    private String profession;

    private String position;

    @Column(unique = true)
    private String mobile;

    private String username;

    private String agency;

    private int group_id;

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public static Finder<Integer, Contact> find = new Finder<Integer, Contact>(Contact.class);

    public static List<Contact> all() {
        return find.all();
    }

//    public static List<Contact> agencyContacts() {
//        List<Contact> agencyContacts = find.where().eq("agency", play.mvc.Http.Context.current().session().get("agency")).findList();
//        return agencyContacts;
//    }

    public static void create(Contact item) {
       item.save();
    }

    public static void delete(Integer id){
        find.ref(id).delete();
    }

    public String getContactGroup(){
        if (this.group_id!=0){
            String groupname = ContactGroup.find.ref(this.group_id).getGroupname();
            return groupname;
        }
        else
            return "";

    }

    public String getAgencyDisplay() {
        return Sharing.find.where().eq("agency", agency).findUnique().getAgency_displayname();
    }

}
