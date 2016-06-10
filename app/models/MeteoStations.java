package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_meteo_stations", catalog="gis", schema="public")

public class MeteoStations extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_meteo_stations_id_seq")
    private int id;

    private String place;
    private String title;
    private double lat;
    private double lon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    // Get data from db
    public static Finder<Integer, MeteoStations> find = new Finder<Integer, MeteoStations>(MeteoStations.class);
    public static List<MeteoStations> all() { return find.all(); }
}
