package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_forecast_wind", catalog="gis", schema="public")

public class ForecastWind extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_forecast_wind_id_seq")
    private int id;


    private String station;
    private Double value_degrees;
    private Double value_speed;

    @Column(name="\"date\"")
    private String date;

    private Integer hours;
    private String hours_mapping;

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Double getValue_degrees() {
        return value_degrees;
    }

    public void setValue_degrees(Double value_degrees) {
        this.value_degrees = value_degrees;
    }

    public Double getValue_speed() {
        return value_speed;
    }

    public void setValue_speed(Double value_speed) {
        this.value_speed = value_speed;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHours_mapping() {
        return hours_mapping;
    }

    public void setHours_mapping(String hours_mapping) {
        this.hours_mapping = hours_mapping;
    }

    // Get data from db
    public static Finder<Integer, ForecastWind> find = new Finder<Integer, ForecastWind>(ForecastWind.class);
    public static List<ForecastWind> all() { return find.all(); }
}
