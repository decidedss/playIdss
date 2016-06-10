package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_forecast_precipitation", catalog="gis", schema="public")

public class ForecastPrecipitation extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_forecast_precipitation_id_seq")
    private int id;

    private String station;
    private Double value;

    @Column(name="\"date\"")
    private String date;

    private Integer hours;
    private String hours_mapping;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public String getHours_mapping() {
        return hours_mapping;
    }

    public void setHours_mapping(String hours_mapping) {
        this.hours_mapping = hours_mapping;
    }

    // Get data from db
    public static Finder<Integer, ForecastPrecipitation> find = new Finder<Integer, ForecastPrecipitation>(ForecastPrecipitation.class);
    public static List<ForecastPrecipitation> all() { return find.all(); }
}
