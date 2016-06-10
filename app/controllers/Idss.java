package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Idss extends Controller {


    public static Result index() {
        if (session().get("userName") != null)
            return ok(views.html.idss_index.render());
        else
            return redirect("/login");
    }


    public static Result view() {
        if (session().get("userName") != null)
            return ok(views.html.idss_view.render());
        else
            return redirect("/login");
    }


    public static Result plan() {
        if (session().get("userName") != null)
            return ok(views.html.idss_plan.render());
        else
            return redirect("/login");
    }


    public static Result act() {
        if (session().get("userName") != null)
            return ok(views.html.idss_act.render());
        else
            return redirect("/login");
    }




}



