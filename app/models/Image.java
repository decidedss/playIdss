package models;

import com.avaje.ebean.Model;
import play.data.validation.Constraints;
import play.i18n.Messages;

import java.io.File;
import java.io.*;
import java.time.LocalDateTime;

public class Image extends Model {

    public int id;

    @Constraints.Required
    public String name;

    public String username;

    private LocalDateTime insert_date;

    public static Finder<Integer, Image> find =  new Finder<Integer, Image>(Image.class);

    public static Finder<String, Image> findUsername =  new Finder<String, Image>(Image.class);

    public LocalDateTime getInsert_date() {
        return insert_date;
    }

    public void setInsert_date(LocalDateTime insert_date) {
        this.insert_date = insert_date;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Image(){
    }

    public Image(String name, File image, String username) throws IOException {
        this.name = name;
        this.username = username;
        this.insert_date = LocalDateTime.now();
        InputStream inStream = null;
        OutputStream outStream = null;
        inStream = new FileInputStream(image);
        outStream = new FileOutputStream(Messages.get("uploadDir") + this.name);

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
