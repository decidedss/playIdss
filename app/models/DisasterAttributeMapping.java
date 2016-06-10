package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_disasters_attribute_mapping", catalog="gis", schema="public")
public class DisasterAttributeMapping extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_disasters_attributes_mapping_id_seq")
    private String disaster_type;

    private String disaster_area;

    private String disaster_cause;

    private String disaster_characteristics;

    private String disaster_impacts_infrastructure;

    private String disaster_impacts_other;

    private String disaster_duration;


    public String getDisaster_type() {
        return disaster_type;
    }

    public void setDisaster_type(String disaster_type) {
        this.disaster_type = disaster_type;
    }

    public String getDisaster_area() {
        return disaster_area;
    }

    public void setDisaster_area(String disaster_area) {
        this.disaster_area = disaster_area;
    }

    public String getDisaster_cause() {
        return disaster_cause;
    }

    public void setDisaster_cause(String disaster_cause) {
        this.disaster_cause = disaster_cause;
    }

    public String getDisaster_characteristics() {
        return disaster_characteristics;
    }

    public void setDisaster_characteristics(String disaster_characteristics) {
        this.disaster_characteristics = disaster_characteristics;
    }

    public String getDisaster_impacts_infrastructure() {
        return disaster_impacts_infrastructure;
    }

    public void setDisaster_impacts_infrastructure(String disaster_impacts_infrastructure) {
        this.disaster_impacts_infrastructure = disaster_impacts_infrastructure;
    }

    public String getDisaster_impacts_other() {
        return disaster_impacts_other;
    }

    public void setDisaster_impacts_other(String disaster_impacts_other) {
        this.disaster_impacts_other = disaster_impacts_other;
    }

    public String getDisaster_duration() {
        return disaster_duration;
    }

    public void setDisaster_duration(String disaster_duration) {
        this.disaster_duration = disaster_duration;
    }

    public static Finder<String, DisasterAttributeMapping> find = new Finder<String, DisasterAttributeMapping>(DisasterAttributeMapping.class);

    public static List<DisasterAttributeMapping> all() {
        return find.all();
    }
}
