package controllers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.utils.RandomPassword;
import models.*;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class User extends Controller {

    public static Result index() throws IOException{
        if (session().get("userName") != null) {

            // Get all Data from user
            HttpClient httpClient = HttpClientBuilder.create().build();
            String url = Messages.get("ALFRSCO_REST_API_URL") +"/people/"+session().get("userName") +"?alf_ticket="+session().get("alf_ticket");
            HttpGet httpget = new HttpGet(url);
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

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false); // to see what happens with this
            Person person = mapper.readValue(json, Person.class);

            Sharing s = Sharing.find.where().eq("agency", session().get("agency")).findUnique();
            boolean share = s.isShare();
            String sharingGroupName = s.getAgency_displayname();
            return ok(views.html.person.render(person, sharingGroupName, "", isAdmin(session().get("userName")), share));
        }
        else
            return  redirect("/login");
    }

    public static Result agencyMembers() throws IOException {

        // Find agency admin
        Person agencyAdmin = User.getPerson(session().get("userName"));
        ArrayList<Person> members = User.getGroupMembers(session().get("agency"));

        // Visible to the idss_admin (administrator of the current agency)
        // If the idss_admin is member of the group-agency can view the list of members except for himself
        if (session().get("userName")!=null && agencyAdmin.getGroups().contains("GROUP_"+session().get("agency")) & agencyAdmin.getGroups().contains("GROUP_idss_admin")  ) {
           String sharingDisplayName = Sharing.find.where().eq("agency", session().get("agency").replaceAll(" ", "")).findUnique().getAgency_displayname();
           return ok(views.html.signup_agency_members.render(sharingDisplayName, members));
        }
        else
            return redirect("/login");
    }

    public static Result password_update() {
        Form<Email> eForm = play.data.Form.form(Email.class).bindFromRequest();
        if (session().get("userName")!=null)
            return redirect("/welcome");
        else
            return ok(views.html.password_update.render(""));
    }


    public static Result add() throws IOException{
        Form<Person> mForm = play.data.Form.form(Person.class).bindFromRequest(); // new

         if (session().get("userName") != null) {

             if (mForm.get().getUserName()!="" ){
                 String response = addToAlfresco(mForm.get());
                 if (response!=null){
                     return redirect("/welcome");
                 }
                 else {
                     return badRequest("fail!");
                 }
             }
             else {
                 return ok(views.html.addPerson.render(mForm));
             }
        }
        else {
             return redirect("/login");
         }

    }

    public static Result addWithGroup() throws IOException {
        Form<Person> mForm = play.data.Form.form(Person.class).bindFromRequest();
        Person p = mForm.get();
        ArrayList<String> gr = new ArrayList<>();
        String group = session().get("agency");
        gr.add("GROUP_" + group);

        if(!p.getView().isEmpty())
            gr.add("GROUP_"+ p.getView());
        if(!p.getEdit().isEmpty())
            gr.add("GROUP_"+ p.getEdit());
        p.setGroups(gr);
        RandomPassword rp = new RandomPassword();
        String password = rp.generateRandomString();
        p.setPassword(password);
        //


        // Check if email exists in Alfresco - parse all /s/api/people
        if(User.personExists(mForm.get().getEmail())){
            flash("email", "exists");
            return redirect("/signup/agency/members");
        }

        // If there is a logged in user
        if (session().get("userName")!=null){

            // If email entered is valid
            Reporting.EmailValidator em = new Reporting.EmailValidator();
            if (em.validate(mForm.get().getEmail())) { // If is valid

                String response =  User.addToAlfresco(p);
                // If username doesn't exist in Alfresco already add them
                if (response!="exists") {

                    // Update the rest of the alfresco agency user details
                    updateAgencyMember(mForm);

                    // Send email to the user just added
                    signupmail(p.getUserName());
                    return ok(views.html.signup_agency_members.render(Sharing.find.where().eq("agency", session().get("agency").replaceAll(" ", "")).findUnique().getAgency_displayname(), User.getGroupMembers(group)));
                }
                else {
                    flash("username", "exists");
                    return redirect("/signup/agency/members");
                }

            }
            else {  // If email is not valid
                flash("email", "wrong");
                return redirect("/signup/agency/members");
            }
        }
        else {
            return redirect("/logout");
        }
    }

    // With or without groups
    public static String addToAlfresco(Person p) throws IOException{

        JsonNode n = Json.toJson(p);
        String str = n.toString();

        // Check if username already exists in Alfresco
        HttpClient httpClient = HttpClientBuilder.create().build();
        String getUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people/" + p.getUserName()+ "?alf_ticket=" + session().get("alf_ticket");
        HttpGet httpget = new HttpGet(getUrl);
        HttpResponse responseUsername = httpClient.execute(httpget);
        if (responseUsername.getStatusLine().getStatusCode()!= 200){ // username does not exist
            String response = null;
            String postUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people?alf_ticket=" + session().get("alf_ticket");
            HttpPost post = new HttpPost(postUrl);
            StringEntity params = new StringEntity(str, "UTF-8");
            try {
                post.setEntity(params);
                post.setHeader("Content-Type", "application/json; charset=utf-8");
                response = null;
                while (response == null){
                    response = httpClient.execute(post, new BasicResponseHandler());
               }

            } catch (Exception e) {
                    e.printStackTrace();
            }
//            System.out.println("RESPONSE:" + response);
            return response;
        }
        else { // username EXISTS already in Alfresco
            return "exists";
        }

    }

    public static Boolean addGroupToAlfresco(String groupname) throws IOException{

        boolean addedGroup = false;

        // Get all groups and check if the new group name already exists
        HttpClient httpClient = HttpClientBuilder.create().build();
        String getUrl = Messages.get("ALFRSCO_REST_API_URL") + "/groups/" + groupname.replaceAll(" ", "") + "?alf_ticket=" + session().get("alf_ticket");
        HttpGet httpget = new HttpGet(getUrl);
        HttpResponse response = httpClient.execute(httpget);
        // System.out.println("Status code ==> " + response.getStatusLine().getStatusCode());

        if (response.getStatusLine().getStatusCode() != 200){ // if code != 200 it means that it does not already exist (should be 404)

            Group group = new Group();
            group.setAuthorityType("GROUP");
            group.setDisplayName(groupname);

            // Post Group to Alfresco
            JsonNode n = Json.toJson(group);
            String str = n.toString();

            String res = null;
            String postUrl = Messages.get("ALFRSCO_REST_API_URL") + "/rootgroups/" +  groupname.replaceAll(" ", "") + "?alf_ticket=" + session().get("alf_ticket");
            HttpPost post = new HttpPost(postUrl);
            StringEntity params = new StringEntity(str, "UTF-8");
            try {
                post.setEntity(params);
                post.setHeader("Content-Type", "application/json; charset=utf-8");
                HttpClient httpClientGroup = HttpClientBuilder.create().build();
                addedGroup = true;
                while (res == null){
                    res = httpClientGroup.execute(post, new BasicResponseHandler());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add group folder in documentLibrary
            Application.createFolderByGroup(groupname);

            // Add group to site membership
            addSiteMembership(groupname);
        }
        return addedGroup;
    }


    public static void addSiteMembership(String groupname) throws IOException {

        Membership m = new Membership();
        m.setRole("SiteManager");
        Group g = new Group();
        g.setAuthorityType("GROUP");
        g.setDisplayName(groupname);
        g.setFullName("GROUP_" + groupname.replaceAll(" ", ""));
        m.setGroup(g);

        // Post membership to Alfresco
        JsonNode n = Json.toJson(m);
        String str = n.toString();

        String res = null;
        String postUrl = Messages.get("ALFRSCO_REST_API_URL") + "/sites/idss/memberships" + "?alf_ticket=" + session().get("alf_ticket");
        HttpPost post = new HttpPost(postUrl);
        StringEntity params = new StringEntity(str, "UTF-8");

        try {
            post.setEntity(params);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            HttpClient httpClientGroup = HttpClientBuilder.create().build();
            while (res == null){
                res = httpClientGroup.execute(post, new BasicResponseHandler());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static class Membership {
        public String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }

        public Group group;


    }


    /**
     * @return all groups registered in Alfresco
     * @throws IOException
     */
    public static ArrayList<String> getAllGroups(String ticket) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        String getUrl = Messages.get("ALFRSCO_REST_API_URL") + "/groups?alf_ticket=" + ticket;
        HttpGet httpget = new HttpGet(getUrl);
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
        ArrayList<String> groups = new ArrayList<>();

        String excluded = Messages.get("excluded");
        ArrayList<String> excludedAlfrescoGroups = new ArrayList<>(Arrays.asList(excluded.split("#")));

        for (JsonNode n : result.get("data")) {
            String agency = n.get("displayName").textValue();

            if (!excludedAlfrescoGroups.contains(agency))
                groups.add(n.get("displayName").textValue());
        }
        return groups;
    }

        /**
         * First adds group to alfresco, then person to this group
         * @param p
         * @return
         * @throws IOException
         */
    public static String addToAlfrescoWithGroup(Person p) throws IOException{

        // Trim empty characters - not sure if needed
        Person person = p; // trimmed person
        person.setUserName(p.getUserName().trim());
        person.setLastName(p.getLastName().trim());
        person.setFirstName(p.getFirstName().trim());
        person.setEmail(p.getEmail().trim());
        person.setJobtitle(p.getJobtitle().trim());
        person.setPassword(p.getPassword().trim());
        person.setOrganization(p.getOrganization().trim());

        ArrayList<String> gr = new ArrayList<>();
        // Here get agency info and create group
        if(addGroupToAlfresco(person.getOrganization())){ // If added now move forward
            gr.add("GROUP_" + person.getOrganization().replaceAll(" ", ""));
            gr.add("GROUP_idss_admin");
            gr.add("GROUP_idss_view");
            gr.add("GROUP_idss_edit");
            person.setGroups(gr);

            session().put("agencyDisplay", p.getOrganization());
            session().put("agency", person.getOrganization().replaceAll(" ", ""));

            // Also add agency to our local db table _agency_sharing
            Sharing s = new Sharing();
            s.setAgency(person.getOrganization().replaceAll(" ", ""));
            s.setAgency_displayname(person.getOrganization());
            s.setShare(false);
            s.setInsert_date(LocalDateTime.now());
            Sharing.create(s);

        } else { // If group already exists
            return null;
        }

        JsonNode n = Json.toJson(person);
        String str = n.toString();

        String response = null;
        String postUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people?alf_ticket=" + session().get("alf_ticket");
        HttpPost post = new HttpPost(postUrl);
        StringEntity params = new StringEntity(str, "UTF-8");

        try {
            post.setEntity(params);
            post.setHeader("Content-Type", "application/json; charset=utf-8");

            HttpClient httpClient = HttpClientBuilder.create().build();
            response = null;
            while (response == null){
                response = httpClient.execute(post, new BasicResponseHandler());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("RESPONSE:" + response);
        return response;
    }


    public static ArrayList<Person> getGroupMembers(String shortName) throws IOException{

        ArrayList<Person> members = new ArrayList<>();

        // Get all groups and check if the new group name already exists
        HttpClient httpClient = HttpClientBuilder.create().build();
        String getUrl = Messages.get("ALFRSCO_REST_API_URL") + "/groups/" + shortName.replaceAll(" ", "") + "/children?alf_ticket=" + session().get("alf_ticket");
        HttpGet httpget = new HttpGet(getUrl);
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
//        System.out.println(json);
        if (result.get("data")!=null) {
            for (JsonNode n : result.get("data")) {
                Person p = getPerson(n.get("shortName").textValue());
                members.add(p);
            }
        }

        return members;
    }

    /**
     *
     * @param shortName is the name of the group to check if exists
     * @return true or false if group/agency was found in Alfresco
     * @throws IOException
     */
    public static boolean groupExists(String shortName, String alf_ticket) throws IOException{
        HttpClient httpClient = HttpClientBuilder.create().build();
        String getUrl = Messages.get("ALFRSCO_REST_API_URL") + "/groups/" + shortName.replaceAll(" ", "") + "?alf_ticket=" + alf_ticket;
        HttpGet httpget = new HttpGet(getUrl);
        HttpResponse response = httpClient.execute(httpget);
        if (response.getStatusLine().getStatusCode()==200)
            return true;
        else
            return false;
    }


    /**
     *
     * @param email check if person exists in Alfresco with the given email
     * @return true or false if person was found in Alfresco
     * @throws IOException
     */
    public static boolean personExists(String email) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        String getUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people?alf_ticket=" + session().get("alf_ticket");
        HttpGet httpget = new HttpGet(getUrl);
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
        for (JsonNode n : result.get("people")) {
            if (n.get("email").textValue().equals(email)){
                return true;
            }
        }
        return false;
    }


    public static Result getPersonJson(String userName) throws  IOException {
        Person p = getPerson(userName);

        return ok(Json.toJson(p));
    }

    public static Result deletePerson(String userName) throws  IOException {

        String status = "";

        String deleteUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people/"+ userName +"?alf_ticket=" + session().get("alf_ticket");
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpDelete httpDelete = new HttpDelete(deleteUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        httpDelete.setHeader("Content-Type", "application/json; charset=utf-8");

        try {
            HttpResponse response = httpClient.execute(httpDelete);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                // EntityUtils to get the response content
                String content =  EntityUtils.toString(respEntity);
                status = String.valueOf(response.getStatusLine().getStatusCode());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(Json.toJson(status));
    }

    public static Person getPerson(String userName) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        String getUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people/" + userName + "?groups=true&alf_ticket=" + session().get("alf_ticket");
        HttpGet httpget = new HttpGet(getUrl);
        HttpResponse response = httpClient.execute(httpget);

        if (response.getStatusLine().getStatusCode() != 200){
            return null;
        }
        else {
            StringBuilder sb = new StringBuilder();
            DataInputStream in = new DataInputStream(response.getEntity().getContent());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            in.close();
            br.close();
            String json = sb.toString();
            String person_without_groups = json.substring(0, json.indexOf("groups")-3);
            person_without_groups = person_without_groups + "} }";

            JsonNode result = Json.parse(json);
            ArrayList<String> groups = new ArrayList<>();
            for (JsonNode n : result.get("groups")) {
                //groups.add(n.get("displayName").textValue());
                groups.add(n.get("itemName").textValue());
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false); // to see what happens with this
            Person person = mapper.readValue(person_without_groups, Person.class);
            person.setGroups(groups);

            return person;
        }
    }

    public static String getPersonAgency(String username) throws IOException {
        ArrayList<String> groups = getPerson(username).getGroups();
        String agency;
//        System.out.println(groups);

        if (groups.contains("GROUP_idss_view"))
            groups.remove("GROUP_idss_view");
        if (groups.contains("GROUP_idss_edit"))
            groups.remove("GROUP_idss_edit");
        if (groups.contains("GROUP_idss_admin"))
            groups.remove("GROUP_idss_admin");

        agency = groups.get(0).replace("GROUP_", "");
//        System.out.println(agency);
        return agency;
    }

    public static boolean isAdmin(String username) throws IOException {
        if (session().get("userName")!=null) {
            ArrayList<String> groups = getPerson(username).getGroups();

            if (groups.contains("GROUP_ALFRESCO_ADMINISTRATORS")) {
                return true;
            }
        }
        return false;
    }

    public static class Password {
        public String password;
    }


    // Password update from the profile page of the user
    public static Result passwordmail_in() throws IOException {
        Form<Password> pForm = play.data.Form.form(Password.class).bindFromRequest();
        HashMap<String, String> data = (HashMap<String, String>) pForm.data();
        String password = data.get("password");
        Person p = getPerson(session().get("userName"));
        updateUserPassword(getPerson(session().get("userName")), password);

        String message =  Messages.get("passwordReset") + "<br/><br/>" +
                Messages.get("passwordResetEmail") + " <a href='" + Messages.get("baseUrl") + "login'>DECIDE</a> "+ Messages.get("with")+ "<br/> " +
                Messages.get("username") + ": <strong>" + session().get("userName") + "</strong><br/>" +
                Messages.get("password") +": <strong>" + password + "</strong>";
        try {
            HtmlEmail email2 = new HtmlEmail();
            email2.setHostName(Messages.get("emailHostname"));
            email2.setSmtpPort(465);
            email2.setCharset(org.apache.commons.mail.EmailConstants.UTF_8);
            email2.setAuthenticator(new DefaultAuthenticator(Messages.get("emailUsername"), Messages.get("emailPassword")));
            email2.setSSLOnConnect(true);
            email2.setFrom(Messages.get("emailUsername"));
            email2.setSubject("[DECIDE] " + Messages.get("passwordUpdateSubject"));
            email2.setHtmlMsg(message);
            email2.addTo(p.getEmail());
            email2.send();
        } catch (EmailException e) {
            e.printStackTrace();
        }

        Sharing share = Sharing.find.where().eq("agency", getPersonAgency(session().get("userName"))).findUnique();
        return ok(views.html.person.render(p, share.getAgency_displayname(), Messages.get("passwordUpdated"), isAdmin(session().get("userName")), share.isShare()));

    }

    public static Result passwordmail() throws IOException {

        String ticket = Application.loginAlfresco();
        session().put("alf_ticket", ticket);

        DynamicForm requestData = Form.form().bindFromRequest();
        String userName = requestData.get("userName");

        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(SessionParameter.USER, "admin");
        parameter.put(SessionParameter.PASSWORD, "admin");
        parameter.put(SessionParameter.ATOMPUB_URL, Messages.get("ALFRSCO_ATOMPUB_URL"));
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        List<Repository> repositories = factory.getRepositories(parameter);
        Application.ses = repositories.get(0).createSession();
        ItemIterable<QueryResult> query =  Application.ses.query("SELECT * FROM cm:person where cm:userName = '" + userName + "'", false);
        if (query.getTotalNumItems()>0){
            //RandomPassword rp = new RandomPassword();
            //String password = rp.generateRandomString();
            Person p = getPerson(requestData.get("userName"));
            updateUserPassword(p, p.getPassword());

            String message = Messages.get("passwordReset")+ "<br/><br/>" +
                    Messages.get("passwordResetEmail") + " <a href='" + Messages.get("baseUrl") + "login'>DECIDE</a> " +  Messages.get("with") + "<br/> " +
                    Messages.get("username") + ": <strong>" + userName + "</strong><br/>" +
                    Messages.get("password") + ": <strong>" + p.getPassword() + "</strong>";
            try {
                HtmlEmail email2 = new HtmlEmail();
                email2.setHostName(Messages.get("emailHostname"));
                email2.setSmtpPort(465);
                email2.setCharset(org.apache.commons.mail.EmailConstants.UTF_8);
                email2.setAuthenticator(new DefaultAuthenticator(Messages.get("emailUsername"), Messages.get("emailPassword")));
                email2.setSSLOnConnect(true);
                email2.setFrom(Messages.get("emailUsername"));
                email2.setSubject("[DECIDE] " + Messages.get("passwordUpdateSubject"));
                email2.setHtmlMsg(message);
                email2.addTo(p.getEmail());
                email2.send();
            } catch (EmailException e) {
                e.printStackTrace();
            }

        }
        else{
            return ok(views.html.password_update.render("No account with the username " + requestData.get("userName") + " has been found!"));
        }

        ////////////////////////////////////////////////////////////////////////

        return ok(views.html.password_update.render(Messages.get("passwordResetMsg")));
    }

    public static Result renameAgency() throws IOException {
        DynamicForm requestData = Form.form().bindFromRequest();

        // Check if entered agency display name is in use by another agency
        List<Sharing> existing = Sharing.find.where().ne("agency", session().get("agency")).eq("agency_displayname", requestData.get("agencyName")).findList();
        if (existing.size()>0){
            flash("agencydisplay", "exists");
            return redirect("/profile");
        }

        Sharing s = Sharing.find.where().eq("agency", session().get("agency")).findUnique();
        if (s!=null){
            s.setAgency_displayname(requestData.get("agencyName"));
            s.update();
            session().put("agencyDisplay",requestData.get("agencyName"));
        }
        return redirect("/profile");
    }

    public static Result signupmail(String username) throws IOException {

        Person p = getPerson(username);
        if (p.getPassword().isEmpty()){
            RandomPassword rp = new RandomPassword();
            String password = rp.generateRandomString();
            p.setPassword(password);
        }
        updateUserPassword(p, p.getPassword());
        Sharing s = Sharing.find.where().eq("agency",getPersonAgency(username)).findUnique();
        String agencyDisplay = s.getAgency_displayname();
        String message = Messages.get("registeredToGroup") +" '" + agencyDisplay + "'. <br/><br/>" +
                         Messages.get("passwordResetEmail")+" <a href='" + Messages.get("baseUrl") + "agency/login'>DECIDE</a> " + Messages.get("with") + "<br/> " +
                         "Agency: <strong>" + agencyDisplay + "</strong><br/>" +
                         Messages.get("username")+": <strong>" + p.getUserName() + "</strong><br/>" +
                         Messages.get("password")+": <strong>" + p.getPassword() + "</strong><br/><br/>" +
                         "<small>"+ Messages.get("passwordResetEmailMsg") +" <a href='" + Messages.get("baseUrl") + "profile'>" + Messages.get("here") + "</a>.</small>";
        try {
            HtmlEmail email2 = new HtmlEmail();
            email2.setHostName(Messages.get("emailHostname"));
            email2.setSmtpPort(465);
            email2.setCharset(org.apache.commons.mail.EmailConstants.UTF_8);
            email2.setAuthenticator(new DefaultAuthenticator(Messages.get("emailUsername"), Messages.get("emailPassword")));
            email2.setSSLOnConnect(true);
            email2.setFrom(Messages.get("emailUsername"));
            email2.setSubject("[DECIDE] " + Messages.get("signup"));
            email2.setHtmlMsg(message);
            email2.addTo(p.getEmail());
            email2.send();
        } catch (EmailException e) {
            e.printStackTrace();
        }

        return redirect("/signup/agency/members");
    }


    public static void updateUserPassword(Person p, String password) throws IOException {
        p.setNewpw(password);
        JsonNode n = Json.toJson(p);
        String str = n.toString();

        String response = null;
        String postUrl = Messages.get("ALFRSCO_REST_API_URL") + "/person/changepassword/" + p.getUserName()+"?alf_ticket=" + session().get("alf_ticket");
        HttpPost post = new HttpPost(postUrl);
        StringEntity params = new StringEntity(str, "UTF-8");

        try {
            post.setEntity(params);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            HttpClient httpClient = HttpClientBuilder.create().build();
            response = null;
            while (response == null){
                response = httpClient.execute(post, new BasicResponseHandler());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Result updateAgencyMembers(String agency) throws IOException{

        Form<Person> pForm = play.data.Form.form(Person.class).bindFromRequest();
        Person p = pForm.get();
        ArrayList<String> groups = getPerson(p.getUserName()).getGroups();

        // Update person with new group
        // http://docs.alfresco.com/5.0/references/RESTful-GroupsChildrenPost.html
        if (!p.getView().isEmpty() && !groups.contains(p.getView())){
            updateUserGroup("idss_view", p.getUserName());
            groups.add(p.getView());
        }
        if (!p.getEdit().isEmpty() && !groups.contains(p.getEdit())){
            updateUserGroup("idss_edit", p.getUserName());
            groups.add(p.getEdit());
        }
        //
        // Delete group from person
        // http://docs.alfresco.com/5.0/references/RESTful-GroupsChildrenDelete.html
        if (p.getView().isEmpty() && groups.contains("GROUP_idss_view")){
            deleteGroupFromUser("idss_view", p.getUserName());
            groups.remove(groups.indexOf("GROUP_idss_view"));
        }
        if (p.getEdit().isEmpty() && groups.contains("GROUP_idss_edit")){
            deleteGroupFromUser("idss_edit", p.getUserName());
            groups.remove(groups.indexOf("GROUP_idss_edit"));
        }
        //
        // System.out.println(groups);
        p.setGroups(groups);
        ////////////////////////////
        JsonNode n = Json.toJson(p);
        String str = n.toString();

        // -------------------------------------------------------------- Finally update person details
        String response = null;
        String putUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people/"+ p.getUserName() +"?alf_ticket=" + session().get("alf_ticket");
        HttpPut put = new HttpPut(putUrl);
        StringEntity params = new StringEntity(str, "UTF-8");

        try {
            put.setEntity(params);
            put.setHeader("Content-Type", "application/json; charset=utf-8");
            HttpClient httpClient = HttpClientBuilder.create().build();
            response = null;
            while (response == null){
                response = httpClient.execute(put, new BasicResponseHandler());
//                System.out.println(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return redirect("/signup/agency/members");
    }

    // Update the grops of the specified user
    public static void updateUserGroup(String group, String userName) {
        String postUrl = Messages.get("ALFRSCO_REST_API_URL") + "/groups/"+ group +  "/children/" + userName +"?alf_ticket=" + session().get("alf_ticket");
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(postUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        try {
            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                // EntityUtils to get the response content
                String content =  EntityUtils.toString(respEntity);
                System.out.println("updateUserGroup response: " + response.getStatusLine().getStatusCode());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void deleteGroupFromUser(String group, String userName){
        String deleteUrl = Messages.get("ALFRSCO_REST_API_URL") + "/groups/"+ group +  "/children/" + userName +"?alf_ticket=" + session().get("alf_ticket");
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpDelete httpDelete = new HttpDelete(deleteUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        httpDelete.setHeader("Content-Type", "application/json; charset=utf-8");

        try {
            HttpResponse response = httpClient.execute(httpDelete);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                // EntityUtils to get the response content
                String content =  EntityUtils.toString(respEntity);
                System.out.println("deleteGroupFromUser response: " + response.getStatusLine().getStatusCode());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Result updateUserDetails() throws IOException{

        Form<Person> pForm = play.data.Form.form(Person.class).bindFromRequest();
        Person p = pForm.get();
        JsonNode n = Json.toJson(p);
        String str = n.toString();

        String response = null;
        String putUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people/"+ session().get("userName") +"?alf_ticket=" + session().get("alf_ticket");
        HttpPut put = new HttpPut(putUrl);
        StringEntity params = new StringEntity(str, "UTF-8");

        try {
            put.setEntity(params);
            put.setHeader("Content-Type", "application/json; charset=utf-8");
            HttpClient httpClient = HttpClientBuilder.create().build();
            response = null;
            while (response == null){
                response = httpClient.execute(put, new BasicResponseHandler());
//                System.out.println(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return redirect("/profile");
    }

    // Internal method to update user details (e.g. after adding a new agency member)
    public static void updateAgencyMember(Form<Person> pForm) throws IOException{
        Person p = pForm.get();
        JsonNode n = Json.toJson(p);
        String str = n.toString();

        String response = null;
        String putUrl = Messages.get("ALFRSCO_REST_API_URL") + "/people/"+ p.getUserName() +"?alf_ticket=" + session().get("alf_ticket");
        HttpPut put = new HttpPut(putUrl);
        StringEntity params = new StringEntity(str, "UTF-8");

        try {
            put.setEntity(params);
            put.setHeader("Content-Type", "application/json; charset=utf-8");
            HttpClient httpClient = HttpClientBuilder.create().build();
            response = null;
            while (response == null){
                response = httpClient.execute(put, new BasicResponseHandler());
//                System.out.println(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Combine all contacts, alfresco users, human resources in one Contact List
     */
    public static List<Contact> getAllAgencyHumanResources() {
        List<Contact> data = new ArrayList<Contact>();

        String agency = "";
        if (session().get("userName") != null) {

            try {
                // If the logged in user has the role ALFRESCO_ADMINISTRATORS show all contacts
                if (isAdmin(session().get("userName"))){
                    List<Contact> all = Contact.find.all();
                    return all;
                }
                else {
                    // Get contacts from DB table _contacts filtered with "agency" ----------------------------
                    agency = session().get("agency");
                    if (!agency.isEmpty()){
                        List<Contact> cList = Contact.find.where().eq("agency", agency).findList();
                        if (!cList.isEmpty()){
                            for (Contact c: cList){
                                data.add(c);
                            }
                        }
                    }

                    // Get alfresco agency members --------------------------------------------------------
                    List<Person> pList = getGroupMembers(agency);
                    if (!pList.isEmpty()){
                        for (Person p: pList){
                            // Matching between Person and Contact
                            Contact c = new Contact();
                            c.setEmail(p.getEmail());
                            c.setFirstname(p.getFirstName());
                            c.setLastname(p.getLastName());
                            c.setLastname(p.getLastName());
                            c.setUsername(p.getUserName());
                            c.setMobile(p.getMobile());
                            c.setProfession(p.getJobtitle());
                            c.setPosition("-");
                            c.setAgency(agency);
                            // Get if any group for alfresco user (table _contacts_alfreso)
                            // @TODO Later on we might have multiple groups per contact (to change unique result)
                            ContactAlfresco ca = ContactAlfresco.find.where().eq("username", p.getUserName()).findUnique();
                            if (ca!=null){
                                c.setGroup_id(ca.getGroup_id());
                            }
                            if (!p.getUserName().equals(session().get("userName"))){ // remove the logged in user from the list
                                data.add(c);
                            }
                        }
                    }
                }
            }catch (IOException io) {
                io.printStackTrace();
            }

        }
        return data;
    }


    public static Result shareAgencyContent(boolean share) throws IOException{
        List<Sharing> sh =  Sharing.find.where().eq("agency", getPersonAgency(session().get("userName")).replaceAll(" ", "")).findList(); // Trim empty characters
        if (sh.size()>0){
            for (Sharing s: sh){
                s.setShare(share);
                Sharing.update(s);
            }
            return ok("updated");
        }
        else
            return ok("fail");
    }


}
