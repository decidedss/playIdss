package models;


import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_meteo_current_year_wind", catalog="gis", schema="public")

public class HistoricWind extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_meteo_current_year_wind_id_seq")
    private int id;

    private String year;
    private String month;
    private double avg_speed;
    private String dom_dir;
    private String station;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getAvg_speed() {
        return avg_speed;
    }

    public void setAvg_speed(double avg_speed) {
        this.avg_speed = avg_speed;
    }

    public String getDom_dir() {
        return dom_dir;
    }

    public void setDom_dir(String dom_dir) {
        this.dom_dir = dom_dir;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    // Get data from db
    public static Finder<Integer, HistoricWind> find = new Finder<Integer, HistoricWind>(HistoricWind.class);
    public static List<HistoricWind> all() { return find.all(); }
}
