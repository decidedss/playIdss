package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_traffic_way", catalog="gis", schema="public")

public class TrafficWay extends Model {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_traffic_way_id_seq")
    private int id;

    private String link_id;
    private String node;
    private double lat;
    private double lon;

    public int getId() { return id; }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink_id() {
        return link_id;
    }

    public void setLink_id(String link_id) {
        this.link_id = link_id;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

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
    public static Finder<Integer, TrafficWay> find = new Finder<Integer, TrafficWay>(TrafficWay.class);
    public static List<TrafficWay> all() { return find.all(); }
}
