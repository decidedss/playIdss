package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="_sms", catalog="gis", schema="public")

public class Sms extends Model {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="_sms_id_seq")
    private  int id;

    private String message;

    private String username;

    private LocalDateTime insert_date;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getInsert_date() {
        return insert_date;
    }

    public void setInsert_date(LocalDateTime insert_date) {
        this.insert_date = insert_date;
    }


    public static Finder<Integer, Sms> find = new Finder<Integer, Sms>(Sms.class);

    public static List<Sms> all() {
        return find.all();
    }

    public static void create(Sms item) {
       item.save();
    }

     public static void delete(Integer id){
        find.ref(id).delete();
    }


}
