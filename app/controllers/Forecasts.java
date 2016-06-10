package controllers;

import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Forecasts extends Controller {

    public static Result forecast() throws IOException {
        if (session().get("userName") != null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<Double, Double>> geodataCoords = new HashMap<String, Map<Double, Double>>();

            for(ForecastStations g : geodata){
                Map<Double, Double> geo = new HashMap<Double, Double>();
                geo.put(g.getLat(), g.getLon());
                geodataCoords.put(g.getPlace(), geo);

                List<ForecastCapacitation> list = ForecastCapacitation.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ForecastCapacitation l : list){
                    data.put(l.getHours(), l.getValue());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);
            }

            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();

            return ok(views.html.forecast.render(geodataCoords));
        }
        else
            return  redirect("/login");
    }

    public static Result getForecastCapacitation(String location) throws IOException{
        if (session().get("userName") != null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<Integer, Double>> precipitation = new HashMap<String, Map<Integer, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastCapacitation> list = ForecastCapacitation.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ForecastCapacitation l : list){
                    data.put(l.getHours(), l.getValue());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);

                precipitation.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(precipitation.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    public static Result getForecastPrecipitation(String location) throws IOException{
        if (session().get("userName") != null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<Integer, Double>> precipitation = new HashMap<String, Map<Integer, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastPrecipitation> list = ForecastPrecipitation.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ForecastPrecipitation l : list){
                    data.put(l.getHours(), l.getValue());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);

                precipitation.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(precipitation.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    public static Result getForecastTemperature(String location) throws IOException{
        if (session().get("userName") != null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<Integer, Double>> temperature = new HashMap<String, Map<Integer, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastTemperature> list = ForecastTemperature.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ForecastTemperature l : list){
                    data.put(l.getHours(), l.getValue());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);

                temperature.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(temperature.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    public static Result getForecastWindDegrees(String location) throws IOException{
        if (session().get("userName") != null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<Integer, Double>> temperature = new HashMap<String, Map<Integer, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastWind> list = ForecastWind.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ForecastWind l : list){
                    data.put(l.getHours(), l.getValue_degrees());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);

                temperature.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(temperature.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    public static Result getForecastWindSpeed(String location) throws IOException{
        if (session().get("userName") != null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<Integer, Double>> temperature = new HashMap<String, Map<Integer, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastWind> list = ForecastWind.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ForecastWind l : list){
                    data.put(l.getHours(), l.getValue_speed());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);

                temperature.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(temperature.get(location)));
        }else{
            return  redirect("/login");
        }
    }

}