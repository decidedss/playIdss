package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.data.Form.form;

public class Resources extends Controller {


    public static Result index() {
        if (session().get("userName") != null){
            return ok(views.html.resources.render());
        }
        else
            return redirect("/login");
    }

    /**
     * Check whether the agency is the superUser
     * @return boolean
     * @throws IOException
     */
    public static boolean isAdmin(String username) throws IOException {
        ArrayList<String> groups = User.getPerson(username).getGroups();

        if (groups.contains("GROUP_ALFRESCO_ADMINISTRATORS")) {
            return true;
        }
        return false;
    }

    /**
     * Check whether the agency is admin
     * @return boolean
     * @throws IOException
     */
    public static boolean isAgencyAdmin(String username) throws IOException{
        ArrayList<String> groups = User.getPerson(username).getGroups();

        if (groups.contains("GROUP_idss_admin")) {
            return true;
        }
        return false;
    }

    /**
     * Check whether the agency has edit permission
     * @return boolean
     * @throws IOException
     */
    public static boolean hasEdit(String username) throws IOException{
        ArrayList<String> groups = User.getPerson(username).getGroups();

        if (groups.contains("GROUP_idss_edit")) {
            return true;
        }
        return false;
    }

    /**
     * Get the agencies that share their content
     * @return list of agencies
     * @throws IOException
     */
    public static List<String> getSharedAgencies() throws IOException{

        ArrayList<String> sharedAgencies = new ArrayList<>();
        for (Sharing s: Sharing.all()){
            if (User.groupExists(s.getAgency(), session().get("alf_ticket")) && s.isShare()){
                sharedAgencies.add(s.getAgency());
            }
        }
        // Remove own agency from list
        sharedAgencies.remove(session().get("agency"));

        return sharedAgencies;
    }

    public static Result infrastructure() throws IOException{
        if (session().get("userName") != null) {
            List<InfrastructureMapping> layers = InfrastructureMapping.all();
            List<String> layersIds = new ArrayList<String>();
            for(InfrastructureMapping l : layers){
                layersIds.add(l.getLayer_id());
            }

            List<String> groups = User.getPerson(session().get("userName")).getGroups();
            String group;

            if(isAdmin(session().get("userName"))){
                group = "GROUP_ALFRESCO_ADMINISTRATORS";
            }else{
                groups.remove("GROUP_idss_edit");
                groups.remove("GROUP_idss_view");
                groups.remove("GROUP_idss_admin");
                group = groups.get(0).replace("GROUP_", "");
            }

            Map<String, String> groupnameAgency = new HashMap<String, String>();
            List<Sharing> contactGroups = new ArrayList<Sharing>();
            contactGroups = Sharing.find.findList();

            for(Sharing contact : contactGroups){
                groupnameAgency.put(contact.getAgency(), contact.getAgency_displayname());
            }

            Map<String, String> infrastructureAttributes = new HashMap<String, String>();
            List<InfrastructureAttributesMapping> infrastructureAttributesMapping = new ArrayList<InfrastructureAttributesMapping>();
            infrastructureAttributesMapping = InfrastructureAttributesMapping.find.findList();

            if(session().get("lang").contains("el")) {
                for (InfrastructureAttributesMapping attributes : infrastructureAttributesMapping) {
                    infrastructureAttributes.put(attributes.getLayer_id(), attributes.getAttributes().replace(",", "||"));
                }
            }else{
                for (InfrastructureAttributesMapping attributes : infrastructureAttributesMapping) {
                    infrastructureAttributes.put(attributes.getLayer_id(), attributes.getAttributes_en().replace(",", "||"));
                }
            }
            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();
            return ok(views.html.infrastructure.render(layers, String.join(",", getSharedAgencies()), isAdmin(session().get("userName")), group, hasEdit(session().get("userName")), groupnameAgency, infrastructureAttributes));
        }
        else
            return redirect("/login");
    }

    public static Result machinery() throws IOException {
        if (session().get("userName") != null) {
            Map<String, String> groupnameAgency = new HashMap<String, String>();
            List<Sharing> contactGroups = new ArrayList<Sharing>();
            contactGroups = Sharing.find.findList();
            for(Sharing contact : contactGroups){
                groupnameAgency.put(contact.getAgency(), contact.getAgency_displayname());
            }
            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();
            return ok(views.html.machinery.render(MachineryType.agencyMachinery(), session().get("userName"),  session().get("agency"), String.join(",", getSharedAgencies()), isAdmin(session().get("userName")), hasEdit(session().get("userName")), groupnameAgency));
        }
        else
            return redirect("/login");
    }

    public static Result human() {
        if (session().get("userName") != null)
            return ok(views.html.human.render());
        else
            return redirect("/login");
    }

    public static Result saveGeometry(String geojson){

        if (session().get("userName") != null) {
            String result = geojson.replaceAll("\\\\", "");

            JsonNode node = Json.parse(result);
            return ok("all good");
        }
        else
            return redirect("/login");
    }

    /**
     * Get all machinery instances stored in the database
     * @return list of machinery instance
     * @throws IOException
     */
    public static Result machineryList() throws IOException {
        List<MachineryType> machineryTypes = new ArrayList<MachineryType>();
        List<MachineryLayer> MachineryLayers = new ArrayList<MachineryLayer>();

        if(isAdmin(session().get("userName"))){
            machineryTypes = MachineryType.find.findList();
            MachineryLayers = MachineryLayer.find.findList();
        }else{
            machineryTypes.addAll(MachineryType.find.where().eq("agency", User.getPersonAgency(Application.session().get("userName"))).findList());
            machineryTypes.addAll(MachineryType.find.where().eq("agency", "ALFRESCO_ADMINISTRATORS").findList());

            MachineryLayers = MachineryLayer.find.where().eq("agency", User.getPersonAgency(Application.session().get("userName"))).findList();
        }

        Map<Integer, String> idType = new HashMap<Integer, String>();
        for(MachineryType mt: machineryTypes) {
            idType.put(mt.getId(), mt.getVehicle_type());
        }

        List<MachineryLayer> machineryLayersOther = new ArrayList<MachineryLayer>();
        for(String agency : getSharedAgencies()){
            List<MachineryLayer> list = MachineryLayer.find.where().eq("agency", agency).findList();
            machineryLayersOther.addAll(list);
        }

        Map<String, String> groupnameAgency = new HashMap<String, String>();
        List<Sharing> contactGroups = new ArrayList<Sharing>();
        contactGroups = Sharing.find.findList();

        for(Sharing contact : contactGroups){
            groupnameAgency.put(contact.getAgency(), contact.getAgency_displayname());
        }

        if (session().get("userName") != null) {
            return ok(views.html.addMachinery.render(machineryTypes, MachineryLayers, machineryLayersOther, idType, isAdmin(session().get("userName")), hasEdit(session().get("userName")), isAgencyAdmin(session().get("userName")), groupnameAgency));
        }
        else
            return redirect("/login");
    }

    /**
     * Get all machinery types stored in the database
     * @return list of machinery types
     * @throws IOException
     */
    public static Result machineryAddType() throws IOException {
        List<MachineryType> machineryList = MachineryType.all();
        Map<Integer, String> idType = new HashMap<Integer, String>();
        for(MachineryType mt: machineryList){
            idType.put(mt.getId(), mt.getVehicle_type());
        }

        if (session().get("userName") != null) {

            List<MachineryType> machineryTypes = new ArrayList<MachineryType>();
            List<MachineryType> machineryTypesAgency = new ArrayList<MachineryType>();

            machineryTypes = MachineryType.find.where().eq("agency", "ALFRESCO_ADMINISTRATORS").findList();

            if(!isAdmin(session().get("userName"))){
                machineryTypesAgency = MachineryType.find.where().eq("agency", User.getPersonAgency(Application.session().get("userName"))).findList();
            }

            return ok(views.html.addMachineryType.render(machineryTypes, machineryTypesAgency, idType, isAdmin(session().get("userName")), isAgencyAdmin(session().get("userName")), hasEdit(session().get("userName"))));
        }
        else
            return redirect("/login");
    }

    /**
     * Add new machinery type
     * @throws IOException
     */
    public static Result machineryTypeAdd() throws IOException{
        Form<Reporting.UploadImageForm> mForm = form(Reporting.UploadImageForm.class).bindFromRequest();

        if (mForm.hasErrors()) {
            List<MachineryType> machineryList = MachineryType.find.where().eq("agency", User.getPersonAgency(Application.session().get("userName"))).findList();
            Map<Integer, String> idType = new HashMap<Integer, String>();
            for(MachineryType mt: machineryList){
                idType.put(mt.getId(), mt.getVehicle_type());
            }

            List<MachineryLayer> machineryLayersOther = new ArrayList<MachineryLayer>();
            for(String agency : getSharedAgencies()){
                List<MachineryLayer> list = MachineryLayer.find.where().eq("agency", agency).findList();
                machineryLayersOther.addAll(list);
            }

            Map<String, String> groupnameAgency = new HashMap<String, String>();
            List<Sharing> contactGroups = new ArrayList<Sharing>();
            contactGroups = Sharing.find.findList();

            for(Sharing contact : contactGroups){
                groupnameAgency.put(contact.getAgency(), contact.getAgency_displayname());
            }

            return badRequest(views.html.addMachinery.render(MachineryType.userMachinery(), MachineryLayer.userMachinery(), machineryLayersOther, idType, isAdmin(session().get("userName")), hasEdit(session().get("userName")), isAgencyAdmin(session().get("userName")), groupnameAgency));
        } else {
            new MachineryType(
                    mForm.get().image.getFilename(),
                    mForm.get().image.getFile(),
                    session().get("userName"),
                    mForm.data().get("vehicle_type")
            );

            return redirect("/resources/machinery/addType");
        }
    }

    /**
     * Update info of a machinery type
     * @throws IOException
     */
    public static Result machineryTypeUpdate() throws IOException {
        MachineryType m = new MachineryType();

        Form<Reporting.UploadImageForm> form = form(Reporting.UploadImageForm.class).bindFromRequest();

        m.setId(Integer.parseInt(form.data().get("id")));
        m.setVehicle_type(form.data().get("vehicle_type"));
        m.setUsername(session().get("userName"));
        m.setAgency(User.getPersonAgency(session().get("userName")));

        if (form.hasErrors()) { // image is missing
            MachineryType.update(null, null, m);
        } else {
            MachineryType.update(form.get().image.getFilename(), form.get().image.getFile(), m);
        }
        return redirect("/resources/machinery/addType");
    }

    /**
     * g a machinery type
     * @throws IOException
     */
    public static Result machineryTypeDelete(Integer contactId) {
        if (session().get("userName") != null) {
            try {
                MachineryType.delete(contactId);
                return redirect("/resources/machinery/addType");
            }catch (Exception e){
                e.printStackTrace();
            }
            return badRequest();
        } else
            return forbidden();
    }

    /**
     * Update a machinery instance
     * @throws IOException
     */
    public static Result machineryLayerUpdate() throws IOException {
        MachineryLayer m = new MachineryLayer();

        Form<MachineryLayer> form = form(MachineryLayer.class).bindFromRequest();
        m.setBhp(form.data().get("bhp"));
        m.setId(Integer.parseInt(form.data().get("id")));
        m.setBrand(form.data().get("brand"));
        m.setLicence_plate(form.data().get("licence_plate"));
        m.setSeats(form.data().get("seats"));
        m.setEquipment(form.data().get("equipment"));
        m.setCargo_type(form.data().get("cargo_type"));
        m.setCapacity_m3(form.data().get("capacity_m3"));
        m.setDriver(form.data().get("driver"));
        m.setDisaster_type(form.data().get("disaster_type"));
        m.setMachinery_status(form.data().get("machinery_status"));
        m.setTires_status(form.data().get("tires_status"));
        m.setNotes(form.data().get("notes"));
        m.setUsername(session().get("userName"));
        m.setAgency(User.getPersonAgency(session().get("userName")));
        m.setAvailability(form.data().get("availability"));

        MachineryLayer.updateLayer(m);

        return redirect("/resources/machinery/edit");
    }

    /**
     * Delete a machinery instance
     */
    public static Result machineryLayerDelete(Integer contactId) {

        if (session().get("userName") != null) {
            try {
                MachineryLayer.deleteLayer(contactId);
                return redirect("/resources/machinery/edit");
            }catch (Exception e){
                e.printStackTrace();
            }
            return badRequest();
        } else
            return forbidden();
    }

    /**
     * Get a machinery type based on a defined id
     * @return Machinery type
     * @throws IOException
     */
    public static Result machineryGetById(int id) throws IOException {
        MachineryType m = MachineryType.find.byId(id);
        return ok(Json.toJson(m));
    }

    /**
     * Get a machinery instance based on a defined id
     * @return Machinery instance
     * @throws IOException
     */
    public static Result machineryGetLayerById(int id) throws IOException {
        MachineryLayer m = MachineryLayer.find.byId(id);
        return ok(Json.toJson(m));
    }

    /**
     * Get a machinery's type icon based on a defined id
     * @return Machinery's type icon
     */
    public static Result getMachineryIcon(String id){
        String icon = MachineryType.find.byId(Integer.parseInt(id)).getIcon();
        return ok(icon);
    }

    /**
     * Get a machinery's type thumbnail based on a defined id
     * @return Machinery's type thumbnail
     */
    public static Result getMachineryThumbnail(String id) throws IOException {
        String thumbnail = MachineryType.find.byId(Integer.parseInt(id)).getThumbnail();
        return ok(thumbnail);
    }

}