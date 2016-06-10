package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_infrastructure_attributes_mapping", catalog="gis", schema="public")
public class InfrastructureAttributesMapping extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_infrastructure_attributes_mapping_id_seq")
    private int id;

    private String layer_id;
    private String attributes;
    private String attributes_en;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLayer_id() {
        return layer_id;
    }

    public void setLayer_id(String layer_id) {
        this.layer_id = layer_id;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getAttributes_en() { return attributes_en; }

    public void setAttributes_en(String attributes_en) { this.attributes_en = attributes_en; }

    public static Finder<Integer, InfrastructureAttributesMapping> find = new Finder<Integer, InfrastructureAttributesMapping>(InfrastructureAttributesMapping.class);

    public static List<InfrastructureAttributesMapping> all() {
        return find.all();
    }
}
