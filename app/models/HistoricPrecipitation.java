package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_meteo_current_year_precipitation", catalog="gis", schema="public")

public class HistoricPrecipitation extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_meteo_current_year_precipitation_id_seq")
    private int id;

    private String year;
    private String month;
    private double rainfall;
    private double maxobsday;
    private String day_rainfall;
    private String days_rainover_01;
    private String days_rainover_02;
    private String days_rainover_03;
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

    public double getRainfall() {
        return rainfall;
    }

    public void setRainfall(double rainfall) {
        this.rainfall = rainfall;
    }

    public double getMaxobsday() {
        return maxobsday;
    }

    public void setMaxobsday(double maxobsday) {
        this.maxobsday = maxobsday;
    }

    public String getDay_rainfall() {
        return day_rainfall;
    }

    public void setDay_rainfall(String day_rainfall) {
        this.day_rainfall = day_rainfall;
    }

    public String getDays_rainover_01() {
        return days_rainover_01;
    }

    public void setDays_rainover_01(String days_rainover_01) {
        this.days_rainover_01 = days_rainover_01;
    }

    public String getDays_rainover_02() {
        return days_rainover_02;
    }

    public void setDays_rainover_02(String days_rainover_02) {
        this.days_rainover_02 = days_rainover_02;
    }

    public String getDays_rainover_03() {
        return days_rainover_03;
    }

    public void setDays_rainover_03(String days_rainover_03) {
        this.days_rainover_03 = days_rainover_03;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    // Get data from db
    public static Finder<Integer, HistoricPrecipitation> find = new Finder<Integer, HistoricPrecipitation>(HistoricPrecipitation.class);
    public static List<HistoricPrecipitation> all() { return find.all(); }
}
