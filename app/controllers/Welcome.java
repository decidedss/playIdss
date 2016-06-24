package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Welcome extends Controller {


    public static Result index() {
        if (session().get("userName") != null && session().get("agency") != null) {
            return ok(views.html.welcome.render());
        }
        else
            return  redirect("/login");
    }

}




