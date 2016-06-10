package models;

import com.avaje.ebean.Model;

public class DayRange extends Model {

    private int dayRange;

    public int getDayRange() {
        return dayRange;
    }

    public void setDayRange(int dayRange) {
        this.dayRange = dayRange;
    }

    public static Finder<Integer, DayRange> find = new Finder<Integer, DayRange>(DayRange.class);

}
