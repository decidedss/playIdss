package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_infrastructure_mapping", catalog="gis", schema="public")
public class InfrastructureMapping extends Model {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_infrastructure_mapping_id_seq")
    private int id;

    private String layer_id;

    private String layer_title;

    private String layer_title_en;

    private String geometry_type;


    public String getLayer_id() {
        return layer_id;
    }

    public void setLayer_id(String layer_id) {
        this.layer_id = layer_id;
    }

    public String getGeometry_type() {
        return geometry_type;
    }

    public void setGeometry_type(String geometry_type) {
        this.geometry_type = geometry_type;
    }

    public String getLayer_title() {
        return layer_title;
    }

    public void setLayer_title(String layer_title) {
        this.layer_title = layer_title;
    }

    public String getLayer_title_en() {
        return layer_title_en;
    }

    public void setLayer_title_en(String layer_title_en) { this.layer_title_en = layer_title_en; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public static Finder<Integer, InfrastructureMapping> find = new Finder<Integer, InfrastructureMapping>(InfrastructureMapping.class);

    public static List<InfrastructureMapping> all() {
        return find.all();
    }

}
