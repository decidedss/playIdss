package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Person;
import models.Sharing;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.data.Form.form;

public class Application extends Controller {

    public static Session ses = null;

    public static Result index() {

        if (session().get("userName") != null) {
            session().put("lang", Controller.lang().language());
            return redirect("/welcome");
        }
        else {
            return redirect("/login");
        }
    }

    public static Result login() throws IOException {
        session().clear();
        session().put("lang", Controller.lang().language());
        return ok(views.html.login.render(form(Login.class), ""));
    }

    public static Result signup_agency(){
        return ok(views.html.signup_agency.render());
    }


    public static Result agency_login() throws IOException {
        String ticket = loginAlfresco(Messages.get("ALFRSCO_USERNAME"), Messages.get("ALFRSCO_PASSWORD"));
        // Logout from previous session
        Application.logout();
        session().put("lang", Controller.lang().language());
        //
        Map<String, String> sMap = new HashMap<>();
        for (Sharing s: Sharing.find.orderBy("agency_displayname asc").findList()){
            if (User.groupExists(s.getAgency(), ticket)) {
                sMap.put(s.getAgency(), s.getAgency_displayname());
            }
        }
        // Remove the group of the administrator from the list
        sMap.remove("ALFRESCO_ADMINISTRATORS");
        return ok(views.html.agency_login.render(form(Login.class), sMap, ""));
    }



    public static Result signup_info() throws IOException{
        Form<Person> mForm = play.data.Form.form(Person.class).bindFromRequest();
        return ok(views.html.signup_info.render(mForm));
    }

    public static Result signup_confirmation() throws IOException{

        Form<Person> mForm = play.data.Form.form(Person.class).bindFromRequest();

            String ticket = loginAlfresco(Messages.get("ALFRSCO_USERNAME"), Messages.get("ALFRSCO_PASSWORD"));
            session().put("alf_ticket", ticket);

            // Check if userName exists in Alfresco
            if (User.getPerson(mForm.get().getUserName()) != null) {
                flash("alfresco_username", "exists");
                return redirect("/signup/info");
            }
            // Validate Password
            if (!mForm.get().getPassword().trim().equals(mForm.get().getPassword_verify().trim())) {
                flash("matching_password", "wrong");
                return redirect("/signup/info");
            }
            // Validate Email
            Reporting.EmailValidator em = new Reporting.EmailValidator();
            if (!em.validate(mForm.get().getEmail())) {
                flash("email", "wrong");
                return redirect("/signup/info");
            }

            // Check if email exists in Alfresco - parse all /s/api/people
//            if (User.personExists(mForm.get().getEmail(), true)) {
//                flash("email", "exists");
//                return redirect("/signup/info");
//            }

            // Check if email exists in Alfresco people - cmis query
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put(SessionParameter.USER, "admin");
            parameter.put(SessionParameter.PASSWORD, "admin");
            parameter.put(SessionParameter.ATOMPUB_URL, Messages.get("ALFRSCO_ATOMPUB_URL"));
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            List<Repository> repositories = factory.getRepositories(parameter);
            Application.ses = repositories.get(0).createSession();
            ItemIterable<QueryResult> query = Application.ses.query("SELECT * FROM cm:person where cm:email = '" + mForm.get().getEmail() + "'", false);
            if (query.getTotalNumItems() > 0) {
                flash("email", "exists");
                return redirect("/signup/info");
            }

            // Check if group name exists in Alfresco
            if (User.groupExists(mForm.get().getOrganization(), ticket)){
                flash("agency", "exists");
                return redirect("/signup/info");
            }
            //////////////////////////////////////////////////////////////////

            Person person = mForm.get();
            return ok(views.html.signup_confirmation.render(person));
    }

    public static Result signup_save() throws IOException{
        Form<Person> mForm = play.data.Form.form(Person.class).bindFromRequest();
        String response = User.addToAlfrescoWithGroup(mForm.get());
        if (response != null) {
            // Fill in session
            session().put("userName", mForm.get().getUserName().trim());
            Person person = User.getPerson(session().get("userName"));
            session().put("groups", person.getGroups().toString());
            session().put("lang", Controller.lang().language());
            Sharing sh = Sharing.find.where().eq("agency", User.getPersonAgency(person.getUserName()).trim()).findUnique();
            if (sh != null) {
                session().put("agencyDisplay", sh.getAgency_displayname());
            }
            session().put("agency", User.getPersonAgency(person.getUserName()).trim());

            String agency = mForm.get().getOrganization();
            return ok(views.html.signup_agency_members.render(agency, User.getGroupMembers(agency)));
        } else {
            return ok(views.html.signup_agency_exists.render());
        }
    }

    public static Result about() throws IOException {
        if (session().get("userName") != null){
            return ok(views.html.about_in.render());
        }
        else {
            return ok(views.html.about_out.render());
        }
    }


    /**
     * Get ticket from alfresco
     * @return
     * @throws IOException
     */
    public static String loginAlfresco(String u, String p) throws IOException {

        String loginUrl = Messages.get("ALFRSCO_REST_API_URL") + "/login?u=" + u + "&pw="+ p +"&format=json";
        HttpGet httpget = new HttpGet(loginUrl);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(httpget);
        StringBuilder sb = new StringBuilder();
        DataInputStream in = new DataInputStream(response.getEntity().getContent());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            sb.append(line);
        }
        in.close();
        br.close();
        String json = sb.toString();
        JsonNode result = Json.parse(json);

        return result.get("data").findValue("ticket").textValue();
    }

    /**
     * Get ticket from alfresco
     * @return
     * @throws IOException
     */
    public static String loginAlfrescoOld() throws IOException {

        String loginUrl = Messages.get("ALFRSCO_REST_API_URL") + "/login?u=" + Messages.get("ALFRSCO_USERNAME") + "&pw="+ Messages.get("ALFRSCO_PASSWORD") +"&format=json";
        HttpGet httpget = new HttpGet(loginUrl);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(httpget);
        StringBuilder sb = new StringBuilder();
        DataInputStream in = new DataInputStream(response.getEntity().getContent());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            sb.append(line);
        }
        in.close();
        br.close();
        String json = sb.toString();
        JsonNode result = Json.parse(json);

        return result.get("data").findValue("ticket").textValue();
    }

    public static Result logout() {

        session().clear();
        return redirect("/login");
    }

    public static Result authentication_common(HashMap<String, String> data){

        try{
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();

            // user credentials
            parameter.put(SessionParameter.USER, data.get("username"));
            parameter.put(SessionParameter.PASSWORD, data.get("password"));

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, Messages.get("ALFRSCO_ATOMPUB_URL"));
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameter.put(SessionParameter.REPOSITORY_ID, "-default-");

            // create session
//            List<Repository> repositories = factory.getRepositories(parameter);
//            ses = repositories.get(0).createSession();
            ses = factory.createSession(parameter);

            ItemIterable<QueryResult> query = ses.query("SELECT * FROM cm:person where cm:userName = '" + data.get("username") + "'", false);

            for (QueryResult item : query) {
                session().put("fullName", item.getPropertyByQueryName("cm:firstName").getFirstValue().toString() + " " + item.getPropertyByQueryName("cm:lastName").getFirstValue().toString());
                session().put("userName", item.getPropertyByQueryName("cm:userName").getFirstValue().toString());
                session().put("lang", Controller.lang().language());
            }
            return ok("Authentication successfull");

        } catch(CmisUnauthorizedException ex){
            return badRequest();
        }
    }


    /**
     * Authenticate user and fill in the session()
     * @return
     * @throws IOException
     */
    public static Result authenticate() throws IOException {

        String ticket = loginAlfresco(Messages.get("ALFRSCO_USERNAME"), Messages.get("ALFRSCO_PASSWORD"));
        session().put("alf_ticket", ticket);

        // Get Data from Login Form
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        HashMap<String, String> data = (HashMap<String, String>) loginForm.data();
        //
        Result res = authentication_common(data);
        //
        if(res.status()!=200){ // password not authenticated
            return ok(views.html.login.render(form(Login.class), Messages.get("errorWrongCredentials")));
        }
        else {
            if (User.getPerson(data.get("username"))!=null) { // there is a user like that
                Person person = User.getPerson(data.get("username").trim());
//              grandAccess(query);
                session().put("groups", person.getGroups().toString());
                session().put("lang", Controller.lang().language());
                session().put("agency", User.getPersonAgency(data.get("username").trim()));
                Sharing sh = Sharing.find.where().eq("agency", User.getPersonAgency(person.getUserName()).trim()).findUnique();
                if (sh!=null) {
                    session().put("agencyDisplay", sh.getAgency_displayname());
                }

                Notifications notifications = new Notifications();
                notifications.getNotificationEvents();

                return redirect("/welcome");
            }
            else {
                return ok(views.html.login.render(form(Login.class), Messages.get("errorNoSuchUser") + " '"+ data.get("username")+"'."));
            }
        }
    }


    /**
     * Authenticate user with agency and fill in the session()
     * @return
     * @throws IOException
     */
    public static Result authenticateAgency() throws IOException {
        String ticket = loginAlfresco(Messages.get("ALFRSCO_USERNAME"), Messages.get("ALFRSCO_PASSWORD"));
        session().put("alf_ticket", ticket);

        // Get Data from Login Form
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        HashMap<String, String> data = (HashMap<String, String>) loginForm.data();
        // System.out.println(data);
        //
        Result res = authentication_common(data);
        //
        Map<String, String> sMap = new HashMap<>();
        for (Sharing s: Sharing.find.orderBy("agency_displayname asc").findList()){
            sMap.put(s.getAgency(), s.getAgency_displayname());
        }

        if(res.status()!=200){ // password not authenticated
            return ok(views.html.agency_login.render(form(Login.class), sMap, Messages.get("errorWrongCredentials")));
        }
        else {
            if (User.getPerson(data.get("username")) != null) { // there is a user like that
                Person person = User.getPerson(data.get("username"));

                // Check if this person is member of the defined group
                if (person.getGroups().contains("GROUP_" +data.get("agencyname"))) {
                    session().put("groups", person.getGroups().toString());
                    session().put("lang", Controller.lang().language());
                    session().put("agency", User.getPersonAgency(data.get("username").trim()));
                    Sharing sh = Sharing.find.where().eq("agency", User.getPersonAgency(person.getUserName()).trim()).findUnique();
                    if (sh!=null) {
                        session().put("agencyDisplay", sh.getAgency_displayname());
                    }

                    return redirect("/welcome");
                } else {
                    return ok(views.html.agency_login.render(form(Login.class), sMap, Messages.get("errorIncorrectAgency")));
                }
            } else {
                 return ok(views.html.agency_login.render(form(Login.class), sMap, Messages.get("errorNoSuchUser") + " '" + data.get("username") + "'."));
            }

        }
    }

    public static void grandAccess(ItemIterable<QueryResult> query) {
        for (QueryResult item : query) {
            session().put("fullName", item.getPropertyByQueryName("cm:firstName").getFirstValue().toString() + " " + item.getPropertyByQueryName("cm:lastName").getFirstValue().toString());
            session().put("userName", item.getPropertyByQueryName("cm:userName").getFirstValue().toString());
        }
    }

    public static class Login {
        public String agencyname;
        public String username;
        public String password;
    }

    public static Result changeLanguage (String lang)
    {
        Controller.changeLang(lang);
        session().put("lang", lang);
        String host = request().host();
        String referer = request().getHeader("referer");
        String redirectUrl = referer.substring(referer.indexOf(host)+host.length());
        return redirect(redirectUrl);
    }

    public static Result sendEmail(String event, String coordinates, String image, String comments, String address) {

        try {
            HtmlEmail email = new HtmlEmail();
            email.setHostName(Messages.get("emailHostname"));
            email.setSmtpPort(Integer.parseInt(Messages.get("emailSmtpPort")));
            email.setStartTLSRequired(true);
            email.setCharset(org.apache.commons.mail.EmailConstants.UTF_8);
            email.setAuthenticator(new DefaultAuthenticator(Messages.get("emailUsername"), Messages.get("emailPassword")));
            email.setFrom(Messages.get("emailUsername"));
            email.setSubject(Messages.get("subjectMailNotify"));

            String img ="";
            if (!image.isEmpty()){
                img = "<img src=\"" + image +  "\" style=\"width:304px;height:236px;\">" +  "<br/>";
//                System.out.println(img);
            }
            String agencyDisplayName = Sharing.find.where().eq("agency", session().get("agency")).findUnique().getAgency_displayname();
            email.setHtmlMsg(Messages.get("agency") + ": <strong>" + agencyDisplayName + "</strong>" +
                           "<hr/>" +event + "<br/><a href=\"http://maps.google.com/maps?q=" + coordinates +"\">"+ Messages.get("coordinates")+ "</a><br/>"  +
                           "<br/><br/>"+ img + "<br/>"+Messages.get("comments") +": " + comments);

            System.out.println("Email sent to: " + address);
            email.addTo(address);
            email.send();

        } catch (EmailException e) {
            e.printStackTrace();
        }
        return ok("Message sent!");
    }

    public static void createFolderByGroup(String name) throws IOException {

        Folder f = new Folder();
        f.setName(name.replaceAll(" ", "")); // trim empty characters (we assumed that the trimmed text is the identifier)
        f.setType("cm:folder");
        JsonNode n = Json.toJson(f);
        String str = n.toString();

        String response = null;
        String foldersUrl = Messages.get("ALFRSCO_REST_API_URL") + "/site/folder/" + Messages.get("alfresco_site") + "/documentLibrary/" + "?alf_ticket=" + session().get("alf_ticket");
        HttpPost post = new HttpPost(foldersUrl);
        StringEntity params = new StringEntity(str, "UTF-8");

        try {
            post.setEntity(params);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            HttpClient httpClient = HttpClientBuilder.create().build();
            response = null;
            while (response == null){
                response = httpClient.execute(post, new BasicResponseHandler());
            }
//            System.out.println("Created folder " + name);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class Folder {
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String name;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String type;
    }

    public static Result remoteSensing(){

        return  ok(views.html.remotesensing.render());
    }





}



