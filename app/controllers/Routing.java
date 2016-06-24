package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.routing;


public class Routing extends Controller {

    public static Result index() {
        if (session().get("userName") != null && session().get("agency") != null) {
            return ok(routing.render("Hello"));
        }
        else {
            return redirect("/login");
        }

    }

}
