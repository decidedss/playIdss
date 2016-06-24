package controllers;

import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Events extends Controller {

    public static Result index() throws IOException {
        if (session().get("userName") != null && session().get("agency")!=null) {
            session().put("days", "3");

                // Get notifications from sharing agency and myself
                List<String> agencies = new ArrayList<>();
                agencies.add(User.getPersonAgency(session().get("userName")));
                for (Sharing s: Sharing.find.all()){
                    if (User.groupExists(s.getAgency(), session().get("alf_ticket")) && s.isShare()){
                        agencies.add(s.getAgency());
                    }
                }
                List<Notification> notifications = Notification.find.where().eq("is_disaster", false).in("agency", agencies).orderBy("insert_date desc").setMaxRows(10).findList();
                //
                List<Notification> last3daysNotifications = new ArrayList<>();
                for (Notification n: getAgencyEvents()){
                    int diff  = (int)ChronoUnit.DAYS.between(n.getInsert_date(), LocalDateTime.now()); // Today, Yesterday and the day before yesterday
                    if (diff < 3) {
                        last3daysNotifications.add(n);
                    }
                }
                return ok(views.html.events.render(last3daysNotifications, EventType.all(), notifications, "", ""));
        }
        else {
            return redirect("/login");
        }
    }

    public static Result allEvents() {
        if (session().get("userName") != null && session().get("agency")!=null) {
            session().put("days", "all");

            // Get notifications from sharing agency and myself
            List<String> agencies = new ArrayList<>();
            agencies.add(User.getPersonAgency(session().get("userName")));
            for (Sharing s: Sharing.find.all()){
                if (User.groupExists(s.getAgency(), session().get("alf_ticket")) && s.isShare()){
                    agencies.add(s.getAgency());
                }
            }
            List<Notification> notifications = Notification.find.where().eq("is_disaster", false).in("agency", agencies).orderBy("insert_date desc").setMaxRows(10).findList();
            return ok(views.html.events.render(getAgencyEvents(), EventType.all(), notifications, "", ""));
        }
        else {
            return redirect("/login");
        }
    }

    public static Result disasters() throws  IOException {
        if (session().get("userName") != null && session().get("agency")!=null) {
            return ok(views.html.disasters.render(getAgencyDisasters(),  EventType.all(), "", "", DisasterAttributeMapping.all()));
        }
        else {
            return redirect("/login");
        }
    }

    public static Result editDisaster() {
        if (session().get("userName") != null && session().get("agency")!=null) {
            Form<Notification> dForm = play.data.Form.form(Notification.class).bindFromRequest();
            Notification disaster = dForm.get();
            disaster.update();
            return redirect("/disasters");
        } else {
            return redirect("/login");
        }
    }

    public static Result findEvents() {
        Form<DayRange> dayRangeFormForm = play.data.Form.form(DayRange.class).bindFromRequest();
        /**
         * We assume that the number of days clicked on the range slider will always be positive
         * otherwise the method could not be triggered
         */
        int days = dayRangeFormForm.get().getDayRange();

        List<Notification> filteredEvents = new ArrayList<>();

        if (session().get("userName") != null  && session().get("agency")!=null) {
            List<Notification> allEvents = getAgencyEvents();

            for (Notification event : allEvents)
                if (event.getInsert_date().isEqual(LocalDateTime.now()) || event.getInsert_date().isBefore(LocalDateTime.now()))
                    if ((int) ChronoUnit.DAYS.between(event.getInsert_date(), LocalDateTime.now()) <= days)
                        filteredEvents.add(event);

            session().put("days", days + " days");
            return ok(views.html.events.render(filteredEvents,  EventType.all(), null, "", ""));
        }
        else {
            return redirect("/login");
        }
    }

    public static Result eventCalendarSearch() throws ParseException {

        if (session().get("userName") != null  && session().get("agency")!=null) {
            Form<Calendar> cForm = play.data.Form.form(Calendar.class).bindFromRequest();
            String from = cForm.get().getFrom();
            String to = cForm.get().getTo();

            List<Notification> filteredList = new ArrayList<>();

            for (Notification n: getAgencyEvents()) {

                String fromT = from.replace(" ", "T");
                String toT = to.replace(" ", "T");

                LocalDateTime ldFrom = LocalDateTime.parse(fromT);
                LocalDateTime ldTo = LocalDateTime.parse(toT);

                if (n.getInsert_date().compareTo(ldFrom)>=0 && ldTo.compareTo(n.getInsert_date())>=0){
                    filteredList.add(n);
                }
            }

            return ok(views.html.events.render(filteredList,  EventType.all(),
                    Notification.find.where().eq("is_disaster", false).eq("agency", session().get("agency")).orderBy("insert_date desc").setMaxRows(10).findList(),
                    from, to));
        }
        else {
            return redirect("/login");
        }
    }


    public static Result disasterCalendarSearch() throws ParseException {

        if (session().get("userName") != null && session().get("agency")!=null) {
            Form<Calendar> cForm = play.data.Form.form(Calendar.class).bindFromRequest();
            String from = cForm.get().getFrom();
            String to = cForm.get().getTo();

            List<Notification> filteredList = new ArrayList<>();

            for (Notification n: getAgencyDisasters()) {

                String fromT = from.replace(" ", "T");
//                System.out.println("from:" + fromT);
                String toT = to.replace(" ", "T");
//                System.out.println("to:" + toT);

                LocalDateTime ldFrom = LocalDateTime.parse(fromT);
                LocalDateTime ldTo = LocalDateTime.parse(toT);

                if (n.getDisaster_date()!=null &&  n.getDisaster_date().length()>0){ // If there is a date filled in for the disaster
                    String disasterDate = n.getDisaster_date();
                    disasterDate = disasterDate.replace(" ", "T");
                    LocalDateTime ldDisasterDate = LocalDateTime.parse(disasterDate);

                    if (ldDisasterDate.compareTo(ldFrom)>=0 && ldTo.compareTo(ldDisasterDate)>=0){
                        filteredList.add(n);
                    }
                }
            }

            return ok(views.html.disasters.render(filteredList,  EventType.all(), from, to, DisasterAttributeMapping.all()));
        }
        else {
            return redirect("/login");
        }
    }


    public static class Calendar {

        public String from;
        public String to;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }


    /**
     * Agency's events merged with shared events
     * @return list of reports
     */
    public static List<Notification> getAgencyEvents(){
        List<Notification> agencyEvents =  Notification.find.where().eq("agency", session().get("agency")).eq("is_disaster",false).findList();
        agencyEvents.addAll(getSharedEvents());
        Notifications notifications = new Notifications();
        notifications.getNotificationEvents();
        return agencyEvents;
    }

    /**
     * Agency's disasters @TODO merge with shared disasters
     * @return list of disasters
     */
    public static List<Notification> getAgencyDisasters(){

        List<Notification> agencyDisasters =  Notification.find.where().eq("agency", session().get("agency")).eq("is_disaster",true).findList();
        agencyDisasters.addAll(getSharedDisasters());
        return agencyDisasters;
    }

    /**
     * Events reported from other agencies that share their content
     * @return list of reports
     */
    public static List<Notification> getSharedEvents(){

        ArrayList<String> list = new ArrayList<>();
        for (Sharing s: Sharing.all()){
            // @TODO probably unnecessary extra check in case some agency has been deleted only from Alfresco and not from the local DB
            if (controllers.User.groupExists(s.getAgency(), session().get("alf_ticket")) && s.isShare()){
                list.add(s.getAgency());
            }
        }
        // Remove own agency from list
        list.remove(session().get("agency"));
        List<Notification> sharedEvents =  Notification.find.where().in("agency", list).eq("is_disaster", false).findList();

        return sharedEvents;
    }


    /**
     * Disasters reported from other agencies that share their content
     * @return list of disasters
     */
    public static List<Notification> getSharedDisasters(){

        ArrayList<String> list = new ArrayList<>();
        for (Sharing s: Sharing.all()){
            // @TODO probably unnecessary extra check in case some agency has been deleted only from Alfresco and not from the local DB
            if (User.groupExists(s.getAgency(), session().get("alf_ticket")) && s.isShare()){
                list.add(s.getAgency());
            }
        }
        // Remove own agency from list
        list.remove(session().get("agency"));
        List<Notification> sharedDisasters =  Notification.find.where().in("agency", list).eq("is_disaster", true).findList();

        return sharedDisasters;
    }


    // Mark as disaster
    public static Result update(boolean status, Integer id) {

        if (session().get("userName") != null && session().get("agency")!=null) {

            Notification n = Notification.find.byId(id);
            n.setIs_disaster(status);
            n.update();
            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();

            return ok("update");
        } else {
            return redirect("/login");
        }
    }


    // Delete event (_notifications) from DB
    public static Result delete(Integer id) {
        if (session().get("userName") != null && session().get("agency")!=null) {

            Notification n = Notification.find.byId(id);
            n.delete();
            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();

            return ok("delete");
        } else {
            return redirect("/login");
        }
    }


}

