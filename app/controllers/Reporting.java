package controllers;

import com.twilio.sdk.TwilioRestException;
import models.*;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static play.data.Form.form;

public class Reporting extends Controller {

    public static Result index() throws IOException {

        Contacts.getNonEmptyContactGroups();

        if (session().get("userName") != null) {
            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();
            return ok(views.html.reporting.render(form(UploadImageForm.class), EventType.all(), Contacts.getNonEmptyContactGroups()));
        }
        else
            return redirect("/login");
    }

    // 100 MB
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 1000 * 1024 * 1024)
    public static Result newReport() throws IOException, TwilioRestException {

        Notification n = new Notification();

       Form<UploadImageForm> form = form(UploadImageForm.class).bindFromRequest();
        if (form.get().image!=null){
            File file = form.get().image.getFile();
            String filename = form.get().image.getFilename();
            n.setImage(Messages.get("publicImageUrl") + filename.replaceAll(" ", ""));
            saveReportedImage(filename, file);
        }
        n.setInsert_date(LocalDateTime.now());
        n.setUsername(session().get("userName"));
        n.setAgency(session().get("agency"));

        n.setIs_disaster(false);
        n.setType(form.data().get("type"));
        n.setDescr(form.data().get("descr"));

        if (!form.data().get("lat").isEmpty()) n.setLat(Double.parseDouble(form.data().get("lat")));
        if (!form.data().get("lon").isEmpty()) n.setLon(Double.parseDouble(form.data().get("lon")));

        // Save notification to database
        Notification.create(n);

        // Get contacts for all the groups selected
        ArrayList<Integer> glist = new ArrayList<>();
        if (form.data().get("contactGroups")!=""){
            String groups = form.data().get("contactGroups");
            ArrayList<String> list = new ArrayList<>(Arrays.asList(groups.split(",")));
            for (String g: list){ // convert to int
                if (!g.isEmpty())
                    glist.add(Integer.parseInt(g));
            }
//             System.out.println(glist);
        }
        List<Contact> contacts = new ArrayList<>();
        List<ContactAlfresco> contactsAlfresco = new ArrayList<>();
        if (glist.size()>0){
            contacts = Contact.find.where().in("group_id", glist).eq("agency", session().get("agency")).findList();
            contactsAlfresco = ContactAlfresco.find.where().in("group_id", glist).eq("agency", session().get("agency")).findList();
        }
        for (ContactAlfresco ca: contactsAlfresco){ // merge contacts with alfresco contacts
            Contact c = new Contact();
            Person p = User.getPerson(ca.getUsername());
            if (p!=null){
                c.setEmail(p.getEmail());
                c.setMobile(p.getMobile());
                contacts.add(c);
            }
        }

        // Report by EMAIL **************************************
        if (form.data().get("toggle-email")!=null){

            EmailValidator em = new EmailValidator();

            for (Contact c: contacts){
                if (em.validate(c.getEmail())){
                    Application.sendEmail(Messages.get("event_reporting") + ": " +
                                    (session().get("lang").equals("el")? EventType.find.where().eq("code",form.data().get("type")).findUnique().getDescription():EventType.find.where().eq("code",form.data().get("type")).findUnique().getDescription_en()),
                            form.data().get("lat") + ", " + form.data().get("lon"),
                            n.getImage()==null? "" : n.getImage(),
                            form.data().get("descr"),
                            c.getEmail());
                }
            }
        }
        // Report by SMS **************************************
        if (form.data().get("toggle-sms")!=null)
        {
            for (Contact c: contacts){
                if (c.getMobile().length() == 10) { // @TODO will need some validation for mobile number (maybe on insert in the contact form)
                    String agencyDisplayName = Sharing.find.where().eq("agency", session().get("agency")).findUnique().getAgency_displayname();
                    String message = Messages.get("agency") + ": " + agencyDisplayName  + ", " + Messages.get("reporting_navbar") + ": ";
                    if (session().get("lang").equals("el"))
                        message = message + EventType.find.where().eq("code",form.data().get("type")).findUnique().getDescription() + ", ";
                    else
                        message = message + EventType.find.where().eq("code",form.data().get("type")).findUnique().getDescription_en() + ", ";

                    message = message + Messages.get("coordinates") + ": [" + form.data().get("lat").substring(0,8) + "," + form.data().get("lon").substring(0,8) + "], ";
                    message = message + Messages.get("comments") + ": " +  form.data().get("descr");
                    Contacts.twilioSMS(message, c.getMobile());
                }
            }

        }

        return redirect("/reporting");
    }


    public static void saveReportedImage(String image, File file) throws IOException{

        // Remove all empty characters
        image = image.replaceAll(" ", "");

        InputStream inStream = null;
        OutputStream outStream = null;
        inStream = new FileInputStream(file);
        outStream = new FileOutputStream(Messages.get("uploadDir")+ image);
        byte[] buffer  = new byte[(int) image.length()];
        int length;
        //copy the file content in bytes
        while ((length = inStream.read(buffer)) > 0){
            outStream.write(buffer, 0, length);
        }
        inStream.close();
        outStream.close();

    }



    public static Result saveLocation(String location, String eventType, String description) throws IOException {
        Notification loc = new Notification();
        Double lat = Double.valueOf(location.substring(location.indexOf(",")+1));
        Double lon = Double.valueOf(location.substring(0, location.indexOf(",")));
        loc.setLat(lat);
        loc.setLon(lon);
        loc.setInsert_date(LocalDateTime.now());
        loc.setType(eventType);
        loc.setUsername(session().get("userName"));
        loc.setAgency(User.getPersonAgency(session().get("userName")));
        loc.setDescr(description);
        loc.setIs_disaster(false);
        Notification.create(loc);

        return ok(location);
    }

    public static Result addEventType() throws IOException  {
       Form<UploadImageForm> form = form(UploadImageForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(views.html.reporting.render(form, null, null));

        } else {
            new EventType(
                    form.get().image.getFilename(),
                    form.get().image.getFile(),
                    session().get("userName"),
                    form.data().get("code"),
                    form.data().get("description")
            );

            flash("success", "File uploaded.");
            return redirect(routes.Reporting.index());
        }

    }


    public static Result uploadImage() throws IOException {
        Form<UploadImageForm> form = form(UploadImageForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(views.html.reporting.render(form,  null, null));

        } else {
            new Image(
                    form.get().image.getFilename(),
                    form.get().image.getFile(),
                    session().get("userName")
            );

            flash("success", "File uploaded.");
            return redirect(routes.Reporting.index());
        }
    }



    public static class UploadImageForm {
        public Http.MultipartFormData.FilePart image;

        public String validate() {

            Http.MultipartFormData data = request().body().asMultipartFormData();
            image = data.getFile("image");

//            if (image == null) {
//                return "File is missing.";
//            }

            return null;
        }
    }



    public static class EmailValidator {

        private Pattern pattern;
        private Matcher matcher;

        private static final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        public EmailValidator() {
            pattern = Pattern.compile(EMAIL_PATTERN);
        }

        /**
         * Validate hex with regular expression
         *
         * @param hex
         *            hex for validation
         * @return true valid hex, false invalid hex
         */
        public boolean validate(final String hex) {

            matcher = pattern.matcher(hex);
            return matcher.matches();

        }
    }

}



