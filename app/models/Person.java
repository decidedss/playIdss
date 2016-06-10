package models;


import com.avaje.ebean.Model;

import javax.persistence.Transient;
import java.util.ArrayList;

/**
 * Created by pmalasio on 8/7/2015.
 * This Model represents the person object in Alfresco.
 */
public class Person extends Model {


    public String getNewpw() {
        return newpw;
    }

    public void setNewpw(String newpw) {
        this.newpw = newpw;
    }

    @javax.persistence.Id

    // @JsonIgnoreProperties(ignoreUnknown = true)

    String newpw;

    private String eligible = "";

    private String view = "";
    private String edit = "";

    private boolean admin = false; // idss_admin role in Alfresco

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }


    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }


    public String getEdit() {
        return edit;
    }

    public void setEdit(String edit) {
        this.edit = edit;
    }


    public String getEligible() {
        return eligible;
    }

    public void setEligible(String eligible) {
        this.eligible = eligible;
    }

    private String url = "";

    private String userName = "";

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password = "";

    @Transient
    private String password_verify = "";

    public String getPassword_verify() {
        return password_verify;
    }

    public void setPassword_verify(String password_verify) {
        this.password_verify = password_verify;
    }

    private boolean enabled = true;

    private String firstName = "";

    private String lastName = "";

    private String jobtitle = "";

    private String organization = "";

    private String organizationId = "";

    private String location = "";

    private String telephone = "";

    private String mobile = "";

    private String email = "";

    private String companyaddress1 = "";

    private String companyaddress2 = "";

    private String companyaddress3 = "";

    private String companypostcode1 = "";

    private String companytelephone = "";

    private String companyfax = "";

    private String companyemail = "";

    private String skype = "";

    private String instantmsg = "";

    private String userStatus = "";

    private String userStatusTime = "";

    private String googleusername= "";

    private int quota = 0;

    private int sizeCurrent = 0;

    private boolean emailFeedDisabled = false;

    private String persondescription = "";

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }

    private ArrayList<String> groups;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyaddress1() {
        return companyaddress1;
    }

    public void setCompanyaddress1(String companyaddress1) {
        this.companyaddress1 = companyaddress1;
    }

    public String getCompanyaddress2() {
        return companyaddress2;
    }

    public void setCompanyaddress2(String companyaddress2) {
        this.companyaddress2 = companyaddress2;
    }

    public String getCompanyaddress3() {
        return companyaddress3;
    }

    public void setCompanyaddress3(String companyaddress3) {
        this.companyaddress3 = companyaddress3;
    }

    public String getCompanypostcode1() {
        return companypostcode1;
    }

    public void setCompanypostcode1(String companypostcode1) {
        this.companypostcode1 = companypostcode1;
    }

    public String getCompanytelephone() {
        return companytelephone;
    }

    public void setCompanytelephone(String companytelephone) {
        this.companytelephone = companytelephone;
    }

    public String getCompanyfax() {
        return companyfax;
    }

    public void setCompanyfax(String companyfax) {
        this.companyfax = companyfax;
    }

    public String getCompanyemail() {
        return companyemail;
    }

    public void setCompanyemail(String companyemail) {
        this.companyemail = companyemail;
    }

    public String getSkype() {
        return skype;
    }

    public void setSkype(String skype) {
        this.skype = skype;
    }

    public String getInstantmsg() {
        return instantmsg;
    }

    public void setInstantmsg(String instantmsg) {
        this.instantmsg = instantmsg;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserStatusTime() {
        return userStatusTime;
    }

    public void setUserStatusTime(String userStatusTime) {
        this.userStatusTime = userStatusTime;
    }

    public String getGoogleusername() {
        return googleusername;
    }

    public void setGoogleusername(String googleusername) {
        this.googleusername = googleusername;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public int getSizeCurrent() {
        return sizeCurrent;
    }

    public void setSizeCurrent(int sizeCurrent) {
        this.sizeCurrent = sizeCurrent;
    }

    public boolean isEmailFeedDisabled() {
        return emailFeedDisabled;
    }

    public void setEmailFeedDisabled(boolean emailFeedDisabled) {
        this.emailFeedDisabled = emailFeedDisabled;
    }

    public String getPersondescription() {
        return persondescription;
    }

    public void setPersondescription(String persondescription) {
        this.persondescription = persondescription;
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
    }

   // public static Finder<Integer,Person> find = new Finder<Integer,Person>(Integer.class, Person.class);

    public static void create(Person item) {
        item.save();
    }

//    public static List<Person> all() {
//        return find.all();
//    }

    public Person() {
  }

    public Person(String firstName, String lastName, String userName, String jobtitle, String organization, String email) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.jobtitle = jobtitle;
        this.organization = organization;
        this.email = email;
    }
}
