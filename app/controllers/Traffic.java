package controllers;

import models.TrafficWay;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Traffic extends Controller {

    public static Result traffic() throws IOException, ParserConfigurationException, SAXException {
        if (session().get("userName") != null && session().get("agency") != null) {
            return ok(views.html.traffic.render());
        }else{
            return redirect("/login");
        }
    }



    public static Result getCoordinates() throws IOException, ParserConfigurationException, SAXException {
        if (session().get("userName") != null && session().get("agency") != null) {
            List<TrafficWay> traffic = TrafficWay.find.findList();
            Map<String, List<double[]>> idCoords = new HashMap<String, List<double[]>>();

            for(TrafficWay t : traffic){

                if(idCoords.containsKey(t.getLink_id())){
                    List<double[]> x = idCoords.get(t.getLink_id());
                    double[] array = new double[] {t.getLon(), t.getLat()};
                    x.add(array);

                    idCoords.put(t.getLink_id(), x);

                }else{
                    List<double[]> x = new ArrayList<double[]>();
                    double[] array = new double[] {t.getLon(), t.getLat()};
                    x.add(array);
                    idCoords.put(t.getLink_id(), x);
                }
            }
            return ok(Json.toJson(idCoords));
        }else{
            return redirect("/login");
        }
    }

    public static Result getCongestion() throws IOException, ParserConfigurationException, SAXException, ParseException {
        if (session().get("userName") != null && session().get("agency") != null) {
            Map<String, String> idCongestion = parseCongestion();
            return ok(Json.toJson(idCongestion));
        }else{
            return redirect("/login");
        }
    }

    public static Map<String, String> parseCongestion() throws IOException, ParseException {
        Map<String, String> idCongestion = new HashMap<String, String>();

        //get data
        URL url = new URL("http://feed.opendata.imet.gr:23577/fcd/congestions.json?offset=0&limit=-1");
        URLConnection yc = url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(yc.getInputStream()));

        String jsonData = "", line;

        while ((line = br.readLine()) != null) {
            jsonData += line + "\n";
        }

        br.close();

        JSONArray jsonarray = new JSONArray(jsonData);

        java.util.Date date= new java.util.Date();
        DateTime dt1 = new DateTime(new Timestamp(date.getTime()));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            DateTime dt2 = new DateTime(format.parse(jsonobject.getString("Timestamp")));

            if(Days.daysBetween(dt2, dt1).getDays() == 0 && Hours.hoursBetween(dt2, dt1).getHours() % 24 == 0 && Minutes.minutesBetween(dt2, dt1).getMinutes() % 60 < 21)
                idCongestion.put(jsonobject.getString("Link_id"), jsonobject.getString("Congestion"));
        }

        return idCongestion;
    }
}
