package models;

import com.avaje.ebean.Model;
import play.i18n.Messages;

import javax.persistence.*;
import java.io.File;
import java.io.*;
import java.util.List;

@Entity
@Table(name="_event_types", catalog="gis", schema="public")

public class EventType extends Model {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="_event_types_id_seq")
    private  int id;

    private String code;

    private String description;

    private String description_en;

    private String username;

    private String icon;

    private String icon_thumbnail;

    private String disaster_description;

    private String disaster_description_en;

    private String disaster_icon;

    private String disaster_icon_thumbnail;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription_en() {
        return description_en;
    }

    public void setDescription_en(String description_en) {
        this.description_en = description_en;
    }

    public String getIcon_thumbnail() {
        return icon_thumbnail;
    }

    public void setIcon_thumbnail(String icon_thumbnail) {
        this.icon_thumbnail = icon_thumbnail;
    }

    public String getDisaster_description() {
        return disaster_description;
    }

    public void setDisaster_description(String disaster_description) {
        this.disaster_description = disaster_description;
    }

    public String getDisaster_icon_thumbnail() {
        return disaster_icon_thumbnail;
    }

    public void setDisaster_icon_thumbnail(String disaster_icon_thumbnail) {
        this.disaster_icon_thumbnail = disaster_icon_thumbnail;
    }

    public String getDisaster_icon() {
        return disaster_icon;
    }

    public void setDisaster_icon(String disaster_icon) {
        this.disaster_icon = disaster_icon;
    }

    public String getDisaster_description_en() {
        return disaster_description_en;
    }

    public void setDisaster_description_en(String disaster_description_en) {
        this.disaster_description_en = disaster_description_en;
    }

    public static Finder<Integer, EventType> find = new Finder<Integer, EventType>(EventType.class);

    public static List<EventType> all() {
        return find.orderBy("id asc").findList();
    }

    public static void create(EventType item) {
       item.save();
    }

     public static void delete(Integer id){
        find.ref(id).delete();
    }


    public EventType(String iconName, File image, String username, String code, String description) throws IOException {
        this.icon = Messages.get("publicIconUrl") + iconName;
        this.username = username;
        this.code = code;
        this.description = description;
        InputStream inStream = null;
        OutputStream outStream = null;
        inStream = new FileInputStream(image);
        outStream = new FileOutputStream(Messages.get("publicIconDir")+ iconName);

        byte[] buffer  = new byte[(int) image.length()];
        int length;
        //copy the file content in bytes
        while ((length = inStream.read(buffer)) > 0){
            outStream.write(buffer, 0, length);
        }

        inStream.close();
        outStream.close();
        this.save();
    }


}
