package models;

import com.avaje.ebean.Model;
import controllers.Application;
import controllers.User;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;

@Entity
@Table(name="_machinery_layer", catalog="gis", schema="public")

public class MachineryLayer extends Model {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_machinery_layer_id_seq")
    private int id;

    private int machinery_id;
    private String brand;
    private String licence_plate;
    private String bhp;
    private String seats;
    private String equipment;
    private String cargo_type;
    private String driver;
    private String disaster_type;
    private String machinery_status;
    private String tires_status;
    private String capacity_m3;
    private String notes;
    private String username;
    private String agency;
    private String availability;

    public MachineryLayer() {}

    public MachineryLayer(String username, String brand, String licence_plate, String bhp, String seats, String equipment, String cargo_type, String capacity_m3, String driver, String disaster_type, String machinery_status, String tires_status, String notes, String availability) throws IOException {
        this.username = username;
        String agency = User.getPersonAgency(username);
        this.agency = agency;
        this.brand = brand;
        this.licence_plate = licence_plate;
        this.bhp = bhp;
        this.seats = seats;
        this.equipment = equipment;
        this.cargo_type = cargo_type;
        this.capacity_m3 = capacity_m3;
        this.driver = driver;
        this.disaster_type = disaster_type;
        this.machinery_status = machinery_status;
        this.tires_status = tires_status;
        this.notes = notes;
        this.availability = availability;

        this.save();
    }

    public String getAgency() {  return agency; }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMachinery_id() {
        return machinery_id;
    }

    public void setMachinery_id(int machinery_id) {
        this.machinery_id = machinery_id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLicence_plate() {
        return licence_plate;
    }

    public void setLicence_plate(String licence_plate) {
        this.licence_plate = licence_plate;
    }

    public String getBhp() {
        return bhp;
    }

    public void setBhp(String bhp) {
        this.bhp = bhp;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getCargo_type() {
        return cargo_type;
    }

    public void setCargo_type(String cargo_type) {
        this.cargo_type = cargo_type;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDisaster_type() {
        return disaster_type;
    }

    public void setDisaster_type(String disaster_type) {
        this.disaster_type = disaster_type;
    }

    public String getMachinery_status() {
        return machinery_status;
    }

    public void setMachinery_status(String machinery_status) {
        this.machinery_status = machinery_status;
    }

    public String getTires_status() {
        return tires_status;
    }

    public void setTires_status(String tires_status) {
        this.tires_status = tires_status;
    }

    public String getCapacity_m3() {
        return capacity_m3;
    }

    public void setCapacity_m3(String capacity_m3) {
        this.capacity_m3 = capacity_m3;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvailability() { return availability; }

    public void setAvailability(String availability) { this.availability = availability; }

    public static Finder<Integer, MachineryLayer> find = new Finder<Integer, MachineryLayer>(MachineryLayer.class);

    public static List<MachineryLayer> all() {
        return find.all();
    }

    public static List<MachineryLayer> userMachinery() {
        return find.where().eq("username", Application.session().get("userName")).findList();
    }
    public static List<MachineryLayer> agencyMachinery() throws IOException {
        return find.where().eq("agency", User.getPersonAgency(Application.session().get("userName"))).findList();
    }

    public static void deleteLayer(Integer id){
        find.ref(id).delete();
    }

    public static void updateLayer(MachineryLayer item){item.update();}

    public String getAgencyDisplay() {
        return Sharing.find.where().eq("agency", agency).findUnique().getAgency_displayname();
    }
}
