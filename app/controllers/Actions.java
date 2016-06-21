package controllers;

import com.avaje.ebean.Expr;
import models.*;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static play.data.Form.form;


public class Actions extends Controller {

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

    static Form<ActionM> actionForm = Form.form(ActionM.class);

    /**
     * Retrieve stored phases, phenomenon, and implementing bodies
     * @throws IOException
     */
    public static Result getActions() throws IOException {
        Form<ActionM> mForm = play.data.Form.form(ActionM.class).bindFromRequest();
        if (session().get("userName") != null) {
            List<ActionsPhase> phase = new ArrayList<ActionsPhase>();
            phase = ActionsPhase.find.findList();

            List<ActionsPhenomenon> phenomenon = new ArrayList<ActionsPhenomenon>();
            phenomenon = ActionsPhenomenon.find.findList();

            List<ActionsImplemetingBody> implemetingBodies = new ArrayList<ActionsImplemetingBody>();
            implemetingBodies = ActionsImplemetingBody.find.findList();

            return ok(views.html.actions.render(phase, phenomenon, implemetingBodies, session().get("agency")));
        }
        else
            return redirect("/login");
    }

    /**
     * Search based on phase and phenomenon
     * @return list of actions
     * @throws IOException
     */
    public static Result searchActions() throws IOException {
        Form<ActionM> mForm = play.data.Form.form(ActionM.class).bindFromRequest();
        if (session().get("userName") != null) {

            String phases = mForm.data().get("phases");
            String phenomenon = mForm.data().get("phenomenon");

            List<String> phasesList = new ArrayList<String>(Arrays.asList(phases.split(",")));
            List<ActionM> allActions = new ArrayList<ActionM>();

            List<ActionsPhase> phasesAll = new ArrayList<ActionsPhase>();
            phasesAll = ActionsPhase.find.findList();

            for(ActionsPhase ph : phasesAll){
                if(phasesList.contains(ph.getTitle())){
                    phasesList.add(ph.getTitle_en());
                }else if(phasesList.contains(ph.getTitle_en())){
                    phasesList.add(ph.getTitle());
                }
            }

            for(String phase : phasesList){
                List<ActionM> actions = ActionM.find.where().conjunction()
                        .add(Expr.like("phase", "%"+phase+"%"))
                        .add(Expr.like("phenomenon", "%"+phenomenon+"%"))
                        .endJunction().findList();
                allActions.addAll(actions);
            }

            if(allActions.size() > 0)
                return ok(views.html.actions_results.render(allActions, true, isAdmin(session().get("userName")), isAgencyAdmin(session().get("userName")), hasEdit(session().get("userName")), session().get("agency"), ActionsPhase.find.findList(), ActionsPhenomenon.find.findList()));

            return ok(views.html.actions_results.render(allActions, false, isAdmin(session().get("userName")), isAgencyAdmin(session().get("userName")), hasEdit(session().get("userName")), session().get("agency"), ActionsPhase.find.findList(), ActionsPhenomenon.find.findList()));
        }
        else
            return redirect("/login");
    }

    /**
     * Delete action
     */
    public static Result deleteAction(Integer id) {

        if (session().get("userName") != null) {
            try {
                ActionM.delete(id);
                return redirect("/actions");
            }catch (Exception e){

            }
            return badRequest();
        } else
            return forbidden();
    }

    /**
     * Edit action
     */
    public static Result editAction() {
        Form<ActionM> form = play.data.Form.form(ActionM.class).bindFromRequest();

        ActionM action = new ActionM();

        action.setId_actions(Integer.parseInt(form.data().get("id_actions")));
        String phases = form.data().get("phases");
        action.setPhase(phases.replace(",", ", "));

        if(form.data().get("phenomenon").equals("other"))
            action.setPhenomenon(form.data().get("phenomenonOther"));
        else
            action.setPhenomenon(form.data().get("phenomenon"));

        action.setCategory(form.data().get("category"));
        action.setDescription(form.data().get("description"));

        if(form.data().get("body") == ""){ action.setBody("-"); }
        else { action.setBody(form.data().get("body")); }

        if(form.data().get("implementing_body") == ""){ action.setImplementing_body("-"); }
        else { action.setImplementing_body(form.data().get("implementing_body")); }

        if(form.data().get("participating_body") == ""){ action.setParticipating_body("-"); }
        else { action.setParticipating_body(form.data().get("participating_body")); }

        if(form.data().get("phenomenon").equals("other"))
            ActionsPhenomenon.add(play.data.Form.form(ActionM.class).bindFromRequest().data().get("phenomenonOther"), play.data.Form.form(ActionM.class).bindFromRequest().data().get("agency"));
        else
            ActionsPhenomenon.add(play.data.Form.form(ActionM.class).bindFromRequest().data().get("phenomenon"), play.data.Form.form(ActionM.class).bindFromRequest().data().get("agency"));


        ActionM.update(action);

        return redirect("/actions");
    }

    public static Result getAction(int id) throws IOException {
        ActionM m = ActionM.find.byId(id);
        return ok(Json.toJson(m));
    }

    /**
     * Add new action
     */
    public static Result addAction(){

        Form<ActionM> mForm = play.data.Form.form(ActionM.class).bindFromRequest();

        if (session().get("userName") != null) {
            if (mForm.get().getPhenomenon() !=null ){
                Form<ActionM> form = play.data.Form.form(ActionM.class).bindFromRequest();
                ActionM action = new ActionM();

                String phases = mForm.data().get("phasesAdd");
                action.setPhase(phases.replace(",", ", "));

                if(form.data().get("phenomenon").equals("other"))
                    action.setPhenomenon(form.data().get("phenomenonOther"));
                else
                    action.setPhenomenon(form.data().get("phenomenon"));

                action.setCategory(form.data().get("category"));
                action.setDescription(form.data().get("description"));

                if(form.data().get("body") == ""){ action.setBody("-"); }
                else { action.setBody(form.data().get("body")); }

                if(form.data().get("implementing_body") == ""){ action.setImplementing_body("-"); }
                else { action.setImplementing_body(form.data().get("implementing_body")); }

                if(form.data().get("participating_body") == ""){ action.setParticipating_body("-"); }
                else { action.setParticipating_body(form.data().get("participating_body")); }

                action.setAgency(form.data().get("agency"));

                ActionM.add(action);

                if(form.data().get("phenomenon").equals("other"))
                    ActionsPhenomenon.add(play.data.Form.form(ActionM.class).bindFromRequest().data().get("phenomenonOther"), play.data.Form.form(ActionM.class).bindFromRequest().data().get("agency"));
                else
                    ActionsPhenomenon.add(play.data.Form.form(ActionM.class).bindFromRequest().data().get("phenomenon"), play.data.Form.form(ActionM.class).bindFromRequest().data().get("agency"));

                return redirect("/actions");
            }
            else {
                return redirect("/actions");
            }
        } else
            return redirect("/login");
    }

    /**
     * Get all available phenomenon per agency
     * @return list of phenomenon
     * @throws IOException
     */
    public static Result phenomenonList() throws IOException {
        if (session().get("userName") != null) {

            List<ActionsPhenomenon> phenomenon = new ArrayList<ActionsPhenomenon>();
            phenomenon = ActionsPhenomenon.find.findList();

            return ok(views.html.editPhenomenon.render(phenomenon, isAdmin(session().get("userName")), isAgencyAdmin(session().get("userName")), hasEdit(session().get("userName")), session().get("agency")));
        }else{
            return redirect("/login");
        }
    }

    /**
     * Delete phenomenon
     */
    public static Result phenomenonDelete(Integer id) {

        if (session().get("userName") != null) {
            try {
                ActionsPhenomenon.delete(id);
                return redirect("/actions/editPhenomenon");
            }catch (Exception e){

            }
            return badRequest();
        } else
            return forbidden();
    }

    /**
     * update the title of a phenomenon in database
     * @throws IOException, ParseException
     */
    public static Result updatePhenomenon() throws IOException, ParseException {
        ActionsPhenomenon m = new ActionsPhenomenon();
        Form<ActionsPhenomenon> form = form(ActionsPhenomenon.class).bindFromRequest();

        m.setId(Integer.parseInt(form.data().get("id")));
        m.setTitle(form.data().get("title"));
        m.setAgency(form.data().get("agency"));

        ActionsPhenomenon.update(form.get());

        return redirect("/actions/editPhenomenon");
    }

    /**
     * get phenomenon based on id
     * @return phenomenon
     * @throws IOException
     */
    public static Result getPhenomenonById(int id) throws IOException {
        ActionsPhenomenon m = ActionsPhenomenon.find.byId(id);
        return ok(Json.toJson(m));
    }


}
