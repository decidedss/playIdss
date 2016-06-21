package controllers;

import models.Geodata;
import models.Sharing;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.*;


public class Maps extends Controller {


    public static Result index() throws  IOException{
        if (session().get("userName") != null) {
            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();

            Map<String, String> groupnameAgency = new HashMap<String, String>();
            for(Sharing contact : Sharing.find.findList()){
                groupnameAgency.put(contact.getAgency(), contact.getAgency_displayname());
            }

            return ok(views.html.maps.render(getGeodataMap(), groupnameAgency));
        }
        else
            return  redirect("/login");
    }

    public static Map<String, List<Geodata>> getGeodataMap() {

        Map<String, List<Geodata>> data = new TreeMap<>();

        List<Geodata> groups = new ArrayList<>();
        if (Controller.lang().language().equals("el")){
            groups = Geodata.find.select("groupname").setDistinct(true).orderBy("groupname asc").findList();
        }
        else if (Controller.lang().language().equals("en")) {
            groups = Geodata.find.select("groupname_en").setDistinct(true).orderBy("groupname asc").findList();
        }
        //
        for (Geodata g: groups){

            List<Geodata> geodata = new ArrayList<>();
            if (Controller.lang().language().equals("el")){
                geodata = Geodata.find.where().eq("groupname", g.getGroupname()).orderBy("id asc").findList();
                data.put(g.getGroupname(), geodata);
            }
            else if (Controller.lang().language().equals("en")){
                geodata = Geodata.find.where().eq("groupname_en", g.getGroupname_en()).orderBy("id asc").findList();
                data.put(g.getGroupname_en(), geodata);
            }
        }
        return data;
    }

}




