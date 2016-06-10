package models;


import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="_meteo_last2days", catalog="gis", schema="public")

public class Last2Days extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_meteo_last2days_id_seq")
    private int id;

    private Date day;
    private Date time;

    private double temp_out;
    private double wind_speed;
    private String wind_dir;
    private double rain;
    private String station;

    public Date getDay() { return day; }

    public void setDay(Date date) {
        this.day = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public double getTemp_out() {
        return temp_out;
    }

    public void setTemp_out(double temp_out) {
        this.temp_out = temp_out;
    }

    public double getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(double wind_speed) {
        this.wind_speed = wind_speed;
    }

    public String getWind_dir() {
        return wind_dir;
    }

    public void setWind_dir(String wind_dir) {
        this.wind_dir = wind_dir;
    }

    public double getRain() {
        return rain;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    // Get data from db
    public static Finder<Integer, Last2Days> find = new Finder<Integer, Last2Days>(Last2Days.class);
    public static List<Last2Days> all() { return find.all(); }
}
