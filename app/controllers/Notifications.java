package controllers;

import models.Notification;
import models.Sharing;
import play.mvc.Controller;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Notifications extends Controller{

    public Notifications(){}

    /**
     * Update notifications
     * @return list of reports
     * @throws IOException
     */
    public void getNotificationEvents() throws IOException {
        Date date = new Date();
        List<Notification> agencyDisasters =  Notification.find.where().eq("is_disaster", false).eq("agency", session().get("agency")).orderBy("insert_date desc").findList();
        agencyDisasters.addAll(getSharedEvents());

        int cntNotification = 0;
        for(Notification notification : agencyDisasters){
            Date convertToDate = Date.from(notification.getInsert_date().atZone(ZoneId.systemDefault()).toInstant());
            long diffDays = (date.getTime() - convertToDate.getTime()) / (24 * 60 * 60 * 1000);
            long diffHours = (date.getTime() - convertToDate.getTime()) / (60 * 60 * 1000) % 24;
            if(diffDays < 1){ if(diffHours < 13){ cntNotification++; } }
        }
        if (cntNotification > 0) {
            session().put("notifications", Integer.toString(cntNotification));
        }
        else {
            session().remove("notifications");
        }
    }

    /**
     * Disasters reported from other agencies that share their content
     * @return list of disasters
     * @throws IOException
     */
    public List<Notification> getSharedEvents() throws IOException{

        ArrayList<String> list = new ArrayList<>();
        for (Sharing s: Sharing.all()){
            // @TODO probably unnecessary extra check in case some agency has been deleted only from Alfresco and not from the local DB
            if (User.groupExists(s.getAgency(), session().get("alf_ticket")) && s.isShare()){
                list.add(s.getAgency());
            }
        }
        // Remove own agency from list
        list.remove(session().get("agency"));
        List<Notification> sharedEvents =  Notification.find.where().in("agency", list).eq("is_disaster", false).findList();

        return sharedEvents;
    }
}
