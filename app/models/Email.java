package models;

import com.avaje.ebean.Model;

import java.util.List;


public class Email extends Model {


    private String email;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static Finder<Integer, Email> find = new Finder<Integer, Email>(Email.class);

    public static List<Email> all() {
        return find.all();
    }
    public static void create(Email item) {
       item.save();
    }


}
