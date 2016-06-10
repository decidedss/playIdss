package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;

public class Welcome extends Controller {


    public static Result index() throws  IOException{
        if (session().get("userName") != null) {
            return ok(views.html.welcome.render());
        }
        else
            return  redirect("/login");
    }

}




