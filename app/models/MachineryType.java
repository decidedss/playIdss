package models;


import com.avaje.ebean.Model;
import controllers.Application;
import controllers.User;
import play.i18n.Messages;

import javax.imageio.ImageIO;
import javax.persistence.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.util.List;

@Entity
@Table(name="_machinery_type", catalog="gis", schema="public")
public class MachineryType extends Model {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_machinery_type_id_seq")
    private int id;

    private String vehicle_type;
    private String username;
    private String agency;
    private String icon;
    private String thumbnail;

    public MachineryType(){}

    public MachineryType(String iconName, File image, String username, String vehicle_type) throws  IOException{

        // Remove all empty characters
        // iconName = iconName.replaceAll(" ", "");
        long unixTime = System.currentTimeMillis() / 1000L; // unix timestamp
        iconName = iconName.replaceAll(" ", "");
        iconName = iconName.substring(0, iconName.lastIndexOf(".")) + unixTime + iconName.substring(iconName.lastIndexOf("."));

        this.icon = Messages.get("publicIconUrl") + iconName;
        this.username = username;

        String agency = User.getPersonAgency(username);
        this.agency = agency;
        this.vehicle_type = vehicle_type;

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

        String th = saveThumbnail(image,iconName);
        if (th!=null){
            this.setThumbnail(th);
        }

        this.save();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }



    public static Finder<Integer,MachineryType> find = new Finder<Integer,MachineryType>(MachineryType.class);

    public static List<MachineryType> all() {
        return find.all();
    }

    public static List<MachineryType> userMachinery() {
        return find.where().eq("username", Application.session().get("userName")).findList();
    }

    public static List<MachineryType> agencyMachinery() throws IOException {
//        return find.where().eq("agency", User.getPersonAgency(Application.session().get("userName"))).findList();
        return MachineryType.all();
    }

    public static void create(MachineryType item) {
        item.save();
    }

    public static void update(String iconName, java.io.File image, MachineryType item) throws IOException{

        if (iconName!=null && image!=null) {
            // Remove all empty characters
            long unixTime = System.currentTimeMillis() / 1000L; // unix timestamp
            iconName = iconName.replaceAll(" ", "");
            iconName = iconName.substring(0, iconName.lastIndexOf(".")) + unixTime + iconName.substring(iconName.lastIndexOf("."));
            //
            item.setIcon(Messages.get("publicIconUrl") + iconName);
            InputStream inStream = null;
            OutputStream outStream = null;
            inStream = new FileInputStream(image);
            outStream = new FileOutputStream(Messages.get("publicIconDir") + iconName);
            byte[] buffer = new byte[(int) image.length()];
            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            inStream.close();
            outStream.close();
            //
            String th = saveThumbnail(image,iconName);
            if (th!=null){
                item.setThumbnail(th);
            }
            //
        }
        item.update();
    }

    public static void delete(Integer id){
        find.ref(id).delete();
    }

    private static String saveThumbnail(java.io.File image, String iconName) throws IOException{
        String newFileName = "thumb_"+iconName;
        //
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        ImageIO.read(image);
        img.createGraphics().drawImage(ImageIO.read(image).getScaledInstance(50, 50, java.awt.Image.SCALE_SMOOTH),0,0,null);
        //
        boolean addedThumbnail = ImageIO.write(img, "jpg", new File(Messages.get("publicIconDir") + newFileName));
        if (addedThumbnail)
            return Messages.get("publicIconUrl") + newFileName;
        else
            return null;

    }
}
