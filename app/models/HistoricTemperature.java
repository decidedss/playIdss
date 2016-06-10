package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_meteo_current_year_temperature", catalog="gis", schema="public")

public class HistoricTemperature extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_meteo_current_year_temperature_id_seq")
    private int id;

    private String year;
    private String month;
    private double mean_max;
    private double mean_min;
    private double mean;
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

    public double getMean_max() {
        return mean_max;
    }

    public void setMean_max(double mean_max) {
        this.mean_max = mean_max;
    }

    public double getMean_min() {
        return mean_min;
    }

    public void setMean_min(double mean_min) {
        this.mean_min = mean_min;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    // Get data from db
    public static Finder<Integer, HistoricTemperature> find = new Finder<Integer, HistoricTemperature>(HistoricTemperature.class);
    public static List<HistoricTemperature> all() { return find.all(); }
}
