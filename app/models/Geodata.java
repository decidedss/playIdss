package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_geodata_layers", catalog="gis", schema="public")
public class Geodata extends Model {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_geodata_layers_id_seq")
    private int id;

    public String groupname;


    public String groupname_en;

    public String tablename;

    public String layertitle;

    public String layerid;

    public String visibleid;

    public String getLayerid() {
        return layerid;
    }

    public void setLayerid(String layerid) {
        this.layerid = layerid;
    }

    public String getVisibleid() {
        return visibleid;
    }

    public void setVisibleid(String visibleid) {
        this.visibleid = visibleid;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }


    public String getGroupname_en() {
        return groupname_en;
    }

    public void setGroupname_en(String groupname_en) {
        this.groupname_en = groupname_en;
    }

    public String getLayertitle() {
        return layertitle;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setLayertitle(String layertitle) {
        this.layertitle = layertitle;
    }

    public static Finder<Integer,Geodata> find = new Finder<Integer,Geodata>(Geodata.class);

    public static List<Geodata> all() {
        return find.all();
    }

}
