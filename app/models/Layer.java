package models;

import com.avaje.ebean.Model;

import javax.persistence.Id;


public class Layer extends Model {

    @Id
    private int id;

    private String name;

    private String title;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



}
