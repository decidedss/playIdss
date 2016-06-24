package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import models.*;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Contacts extends Controller {

    static Form<Contact> contactForm = Form.form(Contact.class);

    public static Result getContacts() {
        String username = session().get("userName");

        if (session().get("userName") != null && session().get("agency")!=null) {

            // Get history of user's sms
            ArrayList<String> userSmsHistory = new ArrayList<>();
             List<SqlRow> list = Ebean.createSqlQuery("select message from _sms where username=:username group by message").setParameter("username", username).findList();
            for (SqlRow msg: list){
                userSmsHistory.add(msg.getString("message"));
            }
            //
            List<Contact> contacts = User.getAllAgencyHumanResources();
            //
            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();
            return ok(views.html.contacts.render(contacts, contactForm, userSmsHistory, getContactsGroups(), Contacts.getNonEmptyContactGroups()));
        }
        else
            return redirect("/login");
    }

    public static Result getContactById(int id) {
        Contact c = Contact.find.ref(id);
        return ok(Json.toJson(c));
    }


    public static Result getContactAlfrescoByUsername(String username) {
        ContactAlfresco ca = ContactAlfresco.find.where().eq("username", username).findUnique();
        if (ca!=null){
            return ok(Json.toJson(ca));
        }
        else return ok("");
    }


    public static Result addContact() {
        String username = session().get("userName");
        Form<Contact> cForm = play.data.Form.form(Contact.class).bindFromRequest();

        if (session().get("userName") != null && session().get("agency")!=null) {

            // Validation /////////////////////////////
            // Check current mobile number
             List<Contact> existing = Ebean.find(Contact.class).where().eq("mobile",  cForm.get().getMobile()).eq("agency", User.getPersonAgency(username).replaceAll(" ", "")).findList();
            if (existing.size()>0) {
                flash("mobile", "exists");
            }
            else {
                Contact c =  cForm.get();
                // Check if mobile format is ok
                if( cForm.get().getMobile().length()!=10 || !cForm.get().getMobile().startsWith("69")){
                    flash("mobile", "wrong");
                }
                else {
                    c.setUsername(username);
                    c.setAgency(User.getPersonAgency(username));
                    Contact.create(c);
                }
            }
            /////////////////////////////////////////
            return redirect("/contacts");
        }
        else return redirect("/login");
    }


    public static Result updateContact() {
        String username = session().get("userName");
        Form<Contact> cForm = play.data.Form.form(Contact.class).bindFromRequest();

        if (session().get("userName") != null && session().get("agency")!=null) {
            // Validation /////////////////////////////
            // Check current mobile number
            List<Contact> existing = Contact.find.where().eq("mobile",  cForm.get().getMobile()).eq("agency", User.getPersonAgency(username).replaceAll(" ", "")).ne("id", cForm.get().getId()).findList();
            if (existing.size()>0) {
                flash("mobile", "exists");
            }
            else {
                Contact c =  cForm.get();
                // Check if mobile format is ok
                if( cForm.get().getMobile().length()!=10 || !cForm.get().getMobile().startsWith("69")){
                    flash("mobile", "wrong");
                }
                else {
                    c.setGroup_id(c.getGroup_id()); // if reset to 0
                    c.update();
                }
            }
            /////////////////////////////////////////
            return redirect("/contacts");
        }
        else {
            return redirect("/login");
        }
    }


    public static Result updateAlfrescoContact() {
        String username = session().get("userName");
        Form<ContactAlfresco> caForm = play.data.Form.form(ContactAlfresco.class).bindFromRequest();//new Form<>(ContactAlfresco.class).bindFromRequest();
        ContactAlfresco ca = caForm.get();

        if (session().get("userName") != null && User.getPersonAgency(session().get("userName"))!=null) {

            // Check if already exists in _contact_alfresco
            if (ca.getContact_id()>0){
                if (ca.getGroup_id() > 0){ // Update record
                    ca.setGroup_id(ca.getGroup_id());
                    ca.update();
                }
                else { // Delete record
                    ca.delete();
                }
            }
            else { // Add record
                ca.setAgency(session().get("agency"));
                ContactAlfresco.create(ca);
            }
            return redirect("/contacts");
        }
        else {
            return redirect("/login");
        }
    }


    public static Result deleteContact(Integer contactId) {

        if (session().get("userName") != null && session().get("agency")!=null) {
            try {
                Contact.delete(contactId);
            } catch (Exception e){
                e.printStackTrace();
            }
            return redirect("/contacts");
        } else {
            return redirect("/login");
        }
    }


    public static Result twilioSMS(String text, String mobile) throws TwilioRestException {

        if (session().get("userName") != null && session().get("agency")!=null) {
            TwilioRestClient client = new TwilioRestClient(Messages.get("twilio_sid"), Messages.get("twilio_token"));

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //      params.add(new BasicNameValuePair("From", "+15084553540"));
            params.add(new BasicNameValuePair("From", Messages.get("twilio_phone_number")));
            params.add(new BasicNameValuePair("Body", text));
            params.add(new BasicNameValuePair("To", "+30" + mobile));
            MessageFactory messageFactory = client.getAccount().getMessageFactory();
            Message message = messageFactory.create(params);

            return ok("SMS sent!");
        }
        else {
            return redirect("/login");
        }
    }


    /**
     * Finds all the groups created by the agency
     * @return
     * @throws IOException
     */
    public static List<ContactGroup> getContactsGroups()  {

        // Get all groups for user's agency
        List<ContactGroup> cgList = ContactGroup.find.where().eq("agency", session().get("agency")).findList();

        return cgList;
    }


    /**
     * Find groups linked to the agency's contacts
     * @return
     * @throws IOException
     */

    public static List<ContactGroup> getNonEmptyContactGroups() {
        List<Integer> li = new ArrayList<>();
        List<Integer> liAlfresco = new ArrayList<>();

        // Get all distinct contactgroups with group_id > 0
        for(Contact c: Contact.find.where().eq("agency", session().get("agency")).ne("group_id",Integer.valueOf(0)).select("group_id").setDistinct(true).findList()) {
            li.add(c.getGroup_id());
        }

        // Get all distinct alfresco contact croups with group_id > 0
        for(ContactAlfresco ca: ContactAlfresco.find.where().eq("agency", session().get("agency")).ne("group_id",Integer.valueOf(0)).select("group_id").setDistinct(true).findList()) {
            liAlfresco.add(ca.getGroup_id());
        }
        // Merge two lists
        li.removeAll(liAlfresco);
        li.addAll(liAlfresco);
        Collections.sort(li);

        return ContactGroup.find.where().in("id", li).findList();
    }


    public static Result addGroup(String groupname) {

        if (session().get("userName") != null && session().get("agency")!=null) {
            String agency = User.getPersonAgency(session().get("userName"));

            // Add distinct group names for each agency
            if (ContactGroup.find.where().eq("agency", agency).eq("groupname", groupname).findList().size() == 0) {

                ContactGroup cg = new ContactGroup();
                cg.setGroupname(groupname);
                cg.setAgency(agency);
                ContactGroup.create(cg);
                JsonNode n = Json.toJson(cg);
                return ok(n);
            } else {
                return ok("");
            }
        } else {
            return redirect("/login");
        }
    }


    public static Result deleteGroup(String groupname) {
        if (session().get("userName") != null && session().get("agency")!=null) {

            String agency = User.getPersonAgency(session().get("userName"));

            // Delete defined group from agency contacts
            int id = ContactGroup.find.where().eq("agency", agency).eq("groupname", groupname).findUnique().getId();
            ContactGroup.delete(id);

            // Update contacts after group deletion with 0 id
            for (Contact c : Contact.find.where().eq("group_id", id).findList()) {
                c.setGroup_id(0);
                c.update();
            }

            // Update contacts_alfresco after group deletion - delete record from _contacts_alfresco
            for (ContactAlfresco ca : ContactAlfresco.find.where().eq("group_id", id).findList()) {
                ca.delete();
            }
            return ok("deleted");
        }
        else {
            return redirect("/login");
        }
    }

    public static Result notifyContacts() throws EmailException, TwilioRestException {

        if (session().get("userName") != null && session().get("agency")!=null) {

            DynamicForm requestData = Form.form().bindFromRequest();

            // Get contacts for all the groups selected
            ArrayList<Integer> glist = new ArrayList<>();
            if (requestData.get("contactGroups") != "") {
                String groups = requestData.get("contactGroups");
                ArrayList<String> list = new ArrayList<>(Arrays.asList(groups.split(",")));
                for (String g : list) { // convert to int
                    if (!g.isEmpty())
                        glist.add(Integer.parseInt(g));
                }
            }

            List<Contact> contacts = new ArrayList<>();
            List<ContactAlfresco> contactsAlfresco = new ArrayList<>();
            if (glist.size() > 0) {
                contacts = Contact.find.where().in("group_id", glist).eq("agency", session().get("agency")).findList();
                contactsAlfresco = ContactAlfresco.find.where().in("group_id", glist).eq("agency", session().get("agency")).findList();
            }
            for (ContactAlfresco ca : contactsAlfresco) { // merge contacts with alfresco contacts
                Contact c = new Contact();
                Person p = null;
                try {
                    p = User.getPerson(ca.getUsername());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (p != null) {
                    c.setEmail(p.getEmail());
                    c.setMobile(p.getMobile());
                    contacts.add(c);
                }
            }

            String message = requestData.get("message-text");

            // Notify by EMAIL **************************************
            if (requestData.get("toggle-email") != null) {

                // Unique values
                HashMap<String, String> emails = new HashMap<>();
                for (Contact c : contacts) {
                    emails.put(c.getEmail(), c.getEmail());
                }
                Reporting.EmailValidator em = new Reporting.EmailValidator();
                for (String m : emails.keySet()) {
                    if (em.validate(m)) {
                        HtmlEmail email = new HtmlEmail();
                        email.setHostName(Messages.get("emailHostname"));
                        email.setSmtpPort(Integer.parseInt(Messages.get("emailSmtpPort")));
                        email.setStartTLSRequired(true);
                        email.setCharset(org.apache.commons.mail.EmailConstants.UTF_8);
                        email.setAuthenticator(new DefaultAuthenticator(Messages.get("emailUsername"), Messages.get("emailPassword")));
                        email.setFrom(Messages.get("emailUsername"));
                        email.setSubject(Messages.get("subjectMailNotify"));
                        email.setHtmlMsg(message);
                        email.addTo(m);
                        email.send();
                    }
                }
            }

            // Notify by SMS **************************************
            if (requestData.get("toggle-sms") != null) {

                // Unique values
                HashMap<String, String> mobiles = new HashMap<>();
                for (Contact c : contacts) {
                    mobiles.put(c.getMobile(), c.getMobile());
                }

                for (String m : mobiles.keySet()) {
                    if (m.length() == 10) {
                        twilioSMS(message, m);
                    }
                }
            }

            // Save message in sms history
            LocalDateTime today = LocalDateTime.now();
            Sms s = new Sms();
            s.setMessage(message);
            s.setUsername(session().get("userName"));
            s.setInsert_date(today);
            Sms.create(s);

            return redirect("/contacts");
        } else {
            return redirect("/login");
        }
    }


}