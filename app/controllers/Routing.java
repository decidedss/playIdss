package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.routing;


/**
 * Created by gspyrou on 8/7/2015.
 */
public class Routing extends Controller {

    public static Result index() {
        if (session().get("userName") != null) {
            return ok(routing.render("Hello"));
        }
        else {
            return redirect("/login");
        }

    }

}
