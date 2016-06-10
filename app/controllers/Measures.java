package controllers;

import models.Measure;
import models.MeasuresCategories;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static play.data.Form.form;

public class Measures extends Controller {

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

    static Form<Measure> measureForm = Form.form(Measure.class);

    public static Result getMeasures() throws IOException {

        if (session().get("userName") != null) {

            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();

            List<MeasuresCategories> categories = new ArrayList<MeasuresCategories>();
            categories = MeasuresCategories.find.findList();

            List<String> categoriesEn = new ArrayList<String>();
            List<String> categoriesEl = new ArrayList<String>();

            for(MeasuresCategories cat : categories){
                categoriesEl.add(cat.getTitle_el());
                categoriesEn.add(cat.getTitle_en());
            }

            return ok(views.html.measures.render(Measure.all(), measureForm, categoriesEn, categoriesEl, session().get("agency"), isAdmin(session().get("userName")), isAgencyAdmin(session().get("userName")), hasEdit(session().get("userName"))));
        }
        else
            return redirect("/login");
    }

    /**
     * create timesheet
     * @return list of measures
     */
    public static Result timesheet() throws IOException {
        if (session().get("userName") != null) {
            return ok(views.html.timesheet.render(Measure.all(), session().get("agency"), isAdmin(session().get("userName"))));
        }
        else {
            return redirect("/login");
        }
    }

    /**
     * add new measure to database
     */
    public static Result newMeasure() {
        Form<Measure> mForm = play.data.Form.form(Measure.class).bindFromRequest();

        if (session().get("userName") != null) {
            if (mForm.get().getName()!=null ){
                Measure.create(mForm.get());
                return redirect("/measures");
            }
            else {

                List<MeasuresCategories> categories = new ArrayList<MeasuresCategories>();
                categories = MeasuresCategories.find.findList();

                List<String> categoriesEn = new ArrayList<String>();
                List<String> categoriesEl = new ArrayList<String>();

                for(MeasuresCategories cat : categories){
                    categoriesEl.add(cat.getTitle_el());
                    categoriesEn.add(cat.getTitle_en());
                }

                return ok(views.html.addMeasure.render(mForm, categoriesEn, categoriesEl));
            }
        }
        else {
            return redirect("/login");
        }
    }

    /**
     * delete a measure from database
     */
    public static Result deleteMeasure(Integer measureId) {

        if (session().get("userName") != null) {
            try {
                Measure.delete(measureId);
                return redirect("/measures");
            }catch (Exception e){

            }
            return badRequest();
        } else
            return forbidden();
    }

    /**
     * get measure based on id
     * @return measure
     * @throws IOException
     */
    public static Result getMeasureById(int id) throws IOException {
        Measure m = Measure.find.byId(id);
        return ok(Json.toJson(m));
    }

    /**
     * update a measure's information in database
     * @throws IOException, ParseException
     */
    public static Result updateMeasure() throws IOException, ParseException {
        Measure m = new Measure();
        Form<Measure> form = form(Measure.class).bindFromRequest();

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        m.setId(Integer.parseInt(form.data().get("id")));
        m.setName(form.data().get("name"));
        m.setCategory(form.data().get("category"));
        m.setLocation(form.data().get("location"));
        m.setDescription(form.data().get("description"));
        m.setRiskaddressing(form.data().get("riskaddressing"));
        m.setBudget(form.data().get("budget"));
        m.setStartdate(formatter.parse(form.data().get("startdate")));
        m.setEnddate(formatter.parse(form.data().get("enddate")));
        m.setAgency(form.data().get("agency"));

        Measure.update(form.get());

        return redirect("/measures");
    }
}

