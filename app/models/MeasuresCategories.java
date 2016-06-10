package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_measures_categories", catalog="gis", schema="public")

public class MeasuresCategories {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_measures_categories_id_seq")
    private  int id;

    private String title_en;
    private String title_el;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getTitle_el() {
        return title_el;
    }

    public void setTitle_el(String title_el) {
        this.title_el = title_el;
    }

    public static Model.Finder<Integer,MeasuresCategories> find = new Model.Finder<Integer,MeasuresCategories>(MeasuresCategories.class);

    public static List<MeasuresCategories> all() {
        return find.all();
    }
}
