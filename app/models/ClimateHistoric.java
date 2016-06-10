package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="_meteo_currentmonth", catalog="gis", schema="public")

public class ClimateHistoric extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_meteo_currentmonth_id_seq")
    private int id;

    @Column(name="\"day\"")
    private int day;

    private double mean_temp;
    private double high;
    private double low;
    private double rain;
    private double avg_wind_speed;
    private double hight_wind;
    private Date time_wind;
    private String dom_dir;
    private int month;
    private String station;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getMean_temp() {
        return mean_temp;
    }

    public void setMean_temp(double mean_temp) {
        this.mean_temp = mean_temp;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getRain() {
        return rain;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }

    public double getAvg_wind_speed() {
        return avg_wind_speed;
    }

    public void setAvg_wind_speed(double avg_wind_speed) {
        this.avg_wind_speed = avg_wind_speed;
    }

    public double getHight_wind() {
        return hight_wind;
    }

    public void setHight_wind(double hight_wind) {
        this.hight_wind = hight_wind;
    }

    public Date getTime_wind() {
        return time_wind;
    }

    public void setTime_wind(Date time_wind) {
        this.time_wind = time_wind;
    }

    public String getDom_dir() {
        return dom_dir;
    }

    public void setDom_dir(String dom_dir) {
        this.dom_dir = dom_dir;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    // Get data from db
    public static Finder<Integer, ClimateHistoric> find = new Finder<Integer, ClimateHistoric>(ClimateHistoric.class);
    public static List<ClimateHistoric> all() { return find.all(); }
}
