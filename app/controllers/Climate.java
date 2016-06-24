package controllers;

import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Climate extends Controller {

    public static Result climate() {
        if (session().get("userName") != null && session().get("agency")!=null) {
            Notifications notifications = new Notifications();
            notifications.getNotificationEvents();
            return ok(views.html.climate.render("iDSS weather data"));
        }
        else {
            return redirect("/login");
        }
    }

    /**
     * Get coordinates for forecasts about weather
     * @return map of places-coordinates
     */
    public static Result getCoordinatesForecast(){
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<Double, Double>> geodataCoords = new HashMap<String, Map<Double, Double>>();

            for (ForecastStations g : geodata) {
                Map<Double, Double> geo = new HashMap<Double, Double>();
                geo.put(g.getLat(), g.getLon());
                geodataCoords.put(g.getPlace()+"_"+g.getTitle(), geo);
            }
            return ok(Json.toJson(geodataCoords));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get coordinates for all other weather data
     * @return map of places-coordinates
     */
    public static Result getCoordinatesHistoric(){
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodataHist = MeteoStations.find.findList();
            Map<String, Map<Double, Double>> geodataCoordsHist = new HashMap<String, Map<Double, Double>>();

            for (MeteoStations g : geodataHist) {
                Map<Double, Double> geo = new HashMap<Double, Double>();
                geo.put(g.getLat(), g.getLon());
                geodataCoordsHist.put(g.getPlace()+"_"+g.getTitle(), geo);
            }
            return ok(Json.toJson(geodataCoordsHist));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get current temperature
     * @return get map with data related to specific location
     */
    public static Result getTempCurrent(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<String, Double>> temp = new HashMap<String, Map<String, Double>>();

            for(MeteoStations g : geodata){
                List<Last2Days> list = Last2Days.find.where().eq("station", g.getPlace()).findList();
                Map<String, Double> data = new HashMap<String, Double>();

                for(Last2Days l : list){
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String time=dateFormat.format(l.getTime());
                    if(l.getTemp_out() != -100)
                        data.put(time, l.getTemp_out());
                }

                Map<String, Double> sorted = new TreeMap<String, Double>(data);
                temp.put(g.getPlace(), sorted);
            }

            return ok(Json.toJson(temp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get current rain-related information
     * @return get map with data related to specific location
     */
    public static Result getRainCurrent(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<String, Double>> temp = new HashMap<String, Map<String, Double>>();

            for(MeteoStations g : geodata){
                List<Last2Days> list = Last2Days.find.where().eq("station", g.getPlace()).findList();
                Map<String, Double> data = new HashMap<String, Double>();

                for(Last2Days l : list){
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String time=dateFormat.format(l.getTime());
                    if(l.getRain() != -100)
                        data.put(time, l.getRain());
                }

                Map<String, Double> sorted = new TreeMap<String, Double>(data);
                temp.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(temp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get current wind-speed-related information
     * @return get map with data related to specific location
     */
    public static Result getWindSpeedCurrent(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<String, Double>> temp = new HashMap<String, Map<String, Double>>();

            for(MeteoStations g : geodata){
                List<Last2Days> list = Last2Days.find.where().eq("station", g.getPlace()).findList();
                Map<String, Double> data = new HashMap<String, Double>();

                for(Last2Days l : list){
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String time=dateFormat.format(l.getTime());
                    if(l.getWind_speed() != -100)
                        data.put(time, l.getWind_speed());
                }

                Map<String, Double> sorted = new TreeMap<String, Double>(data);
                temp.put(g.getPlace(), sorted);
            }

            return ok(Json.toJson(temp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get current wind-degree-related information
     * @return get map with data related to specific location
     */
    public static Result getWindDegreeCurrent(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<String, String>> temp = new HashMap<String, Map<String, String>>();

            for(MeteoStations g : geodata){
                List<Last2Days> list = Last2Days.find.where().eq("station", g.getPlace()).findList();
                Map<String, String> data = new HashMap<String, String>();

                for(Last2Days l : list){
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String time=dateFormat.format(l.getTime());
                    data.put(time, l.getWind_dir());
                }

                Map<String, String> sorted = new TreeMap<String, String>(data);
                temp.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(temp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get forecast capacitation-related information
     * @return get map with data related to specific location
     */
    public static Result getForecastCapacitation(String location){
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<String, Double>> precipitation = new HashMap<String, Map<String, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastCapacitation> list = ForecastCapacitation.find.where().eq("station", g.getPlace()).findList();
                LinkedHashMap<String, Double> data = new LinkedHashMap<String, Double>();

                for(ForecastCapacitation l : list){
                    if(l.getHours() >= 18) {
                        data.put(l.getHours_mapping(), l.getValue());
                    }
                }

                precipitation.put(g.getPlace(), data);
            }
            return ok(Json.toJson(precipitation.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get forecast precipitation-related information
     * @return get map with data related to specific location
     */
    public static Result getForecastPrecipitation(String location){
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<String, Double>> precipitation = new HashMap<String, Map<String, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastPrecipitation> list = ForecastPrecipitation.find.where().eq("station", g.getPlace()).findList();
                LinkedHashMap<String, Double> data = new LinkedHashMap<String, Double>();

                for(ForecastPrecipitation l : list){
                    if(l.getHours() >= 18)
                        data.put(l.getHours_mapping(), l.getValue());
                }

                precipitation.put(g.getPlace(), data);
            }
            return ok(Json.toJson(precipitation.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get forecast temperature-related information
     * @return get map with data related to specific location
     */
    public static Result getForecastTemperature(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<String, Double>> temperature = new HashMap<String, Map<String, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastTemperature> list = ForecastTemperature.find.where().eq("station", g.getPlace()).findList();
                LinkedHashMap<String, Double> data = new LinkedHashMap<String, Double>();

                for(ForecastTemperature l : list){
                    if(l.getHours() >= 18)
                        data.put(l.getHours_mapping(), l.getValue());
                }

                temperature.put(g.getPlace(), data);
            }
            return ok(Json.toJson(temperature.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get forecast wind-degree-related information
     * @return get map with data related to specific location
     */
    public static Result getForecastWindDegrees(String location){
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<String, Double>> temperature = new HashMap<String, Map<String, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastWind> list = ForecastWind.find.where().eq("station", g.getPlace()).findList();
                LinkedHashMap<String, Double> data = new LinkedHashMap<String, Double>();

                for(ForecastWind l : list){
                    if(l.getHours() >= 18)
                        data.put(l.getHours_mapping(), l.getValue_degrees());
                }

                temperature.put(g.getPlace(), data);
            }
            return ok(Json.toJson(temperature.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get forecast wind-speed-related information
     * @return get map with data related to specific location
     */
    public static Result getForecastWindSpeed(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<ForecastStations> geodata = ForecastStations.find.findList();
            Map<String, Map<String, Double>> temperature = new HashMap<String, Map<String, Double>>();

            for(ForecastStations g : geodata){
                List<ForecastWind> list = ForecastWind.find.where().eq("station", g.getPlace()).findList();
                LinkedHashMap<String, Double> data = new LinkedHashMap<String, Double>();

                for(ForecastWind l : list){
                    if(l.getHours() >= 18)
                        data.put(l.getHours_mapping(), l.getValue_speed());
                }

                temperature.put(g.getPlace(), data);
            }
            return ok(Json.toJson(temperature.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get historic mean-temp information
     * @return get map with data related to specific location
     */
    public static Result getMeanTemp(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<Integer, Double>> meanTemp = new HashMap<String, Map<Integer, Double>>();

            for(MeteoStations g : geodata){
                List<ClimateHistoric> list = ClimateHistoric.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ClimateHistoric l : list){
                    data.put(l.getDay(), l.getMean_temp());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);
                meanTemp.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(meanTemp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get historic high-temp information
     * @return get map with data related to specific location
     */
    public static Result getHighTemp(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<Integer, Double>> highTemp = new HashMap<String, Map<Integer, Double>>();

            for(MeteoStations g : geodata){
                List<ClimateHistoric> list = ClimateHistoric.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ClimateHistoric l : list){
                    data.put(l.getDay(), l.getMean_temp());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);
                highTemp.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(highTemp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get historic low-temp information
     * @return get map with data related to specific location
     */
    public static Result getLowTemp(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<Integer, Double>> lowTemp = new HashMap<String, Map<Integer, Double>>();

            for(MeteoStations g : geodata){
                List<ClimateHistoric> list = ClimateHistoric.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ClimateHistoric l : list){
                    data.put(l.getDay(), l.getLow());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);
                lowTemp.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(lowTemp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get current year temp-related information
     * @return get map with data related to specific location
     */
    public static Result getTempCurrentYear(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<String, String>> temp = new HashMap<String, Map<String, String>>();

            int year = Calendar.getInstance().get(Calendar.YEAR);

            for(MeteoStations g : geodata){
                List<HistoricTemperature> list = HistoricTemperature.find.where().eq("station", g.getPlace()).findList();
                Map<String, String> data = new HashMap<String, String>();

                for(HistoricTemperature l : list){
                    if(Integer.toString(year).equals("20"+l.getYear()))
                        data.put(l.getMonth(), l.getMean() + "\t" + l.getMean_max() + "\t" + l.getMean_min());
                }

                Map<String, String> sorted = new TreeMap<String, String>(data);
                temp.put(g.getPlace(), sorted);
            }

            return ok(Json.toJson(temp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get current year precipitation-related information
     * @return get map with data related to specific location
     */
    public static Result getPrecipitationCurrentYear(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<String, Double>> rainfall = new HashMap<String, Map<String, Double>>();

            int year = Calendar.getInstance().get(Calendar.YEAR);

            for(MeteoStations g : geodata){
                List<HistoricPrecipitation> list = HistoricPrecipitation.find.where().eq("station", g.getPlace()).findList();
                Map<String, Double> data = new HashMap<String, Double>();

                for(HistoricPrecipitation l : list){
                    if(Integer.toString(year).equals("20"+l.getYear()))
                        data.put(l.getMonth(), l.getRainfall());
                }

                Map<String, Double> sorted = new TreeMap<String, Double>(data);
                rainfall.put(g.getPlace(), sorted);
            }

            return ok(Json.toJson(rainfall.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get current year wind-speed-related information
     * @return get map with data related to specific location
     */
    public static Result getWindSpeedCurrentYear(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<String, Double>> wind = new HashMap<String, Map<String, Double>>();

            int year = Calendar.getInstance().get(Calendar.YEAR);

            for(MeteoStations g : geodata){
                List<HistoricWind> list = HistoricWind.find.where().eq("station", g.getPlace()).findList();
                Map<String, Double> data = new HashMap<String, Double>();

                for(HistoricWind l : list){
                    if(Integer.toString(year).equals("20"+l.getYear()))
                        data.put(l.getMonth(), l.getAvg_speed());
                }

                Map<String, Double> sorted = new TreeMap<String, Double>(data);
                wind.put(g.getPlace(), sorted);
            }

            return ok(Json.toJson(wind.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get historic temp-related information
     * @return get map with data related to specific location
     */
    public static Result getTempHistoric(String location) {
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<Integer, String>> temp = new HashMap<String, Map<Integer, String>>();

            for(MeteoStations g : geodata){
                List<ClimateHistoric> list = ClimateHistoric.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, String> data = new HashMap<Integer, String>();

                for(ClimateHistoric l : list){
                    data.put(l.getDay(), l.getMean_temp() + "\t" + l.getHigh() + "\t" + l.getLow());
                }

                Map<Integer, String> sorted = new TreeMap<Integer, String>(data);
                temp.put(g.getPlace(), sorted);
            }

            return ok(Json.toJson(temp.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get historic rain-related information
     * @return get map with data related to specific location
     */
    public static Result getRainHistoric(String location){
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<Integer, Double>> rain = new HashMap<String, Map<Integer, Double>>();

            for(MeteoStations g : geodata){
                List<ClimateHistoric> list = ClimateHistoric.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, Double> data = new HashMap<Integer, Double>();

                for(ClimateHistoric l : list){
                    data.put(l.getDay(), l.getRain());
                }

                Map<Integer, Double> sorted = new TreeMap<Integer, Double>(data);
                rain.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(rain.get(location)));
        }else{
            return  redirect("/login");
        }
    }

    /**
     * Get historic wind-related information
     * @return get map with data related to specific location
     */
    public static Result getWindHistoric(String location){
        if (session().get("userName") != null && session().get("agency")!=null) {
            List<MeteoStations> geodata = MeteoStations.find.findList();
            Map<String, Map<Integer, String>> wind = new HashMap<String, Map<Integer, String>>();

            for(MeteoStations g : geodata){
                List<ClimateHistoric> list = ClimateHistoric.find.where().eq("station", g.getPlace()).findList();
                Map<Integer, String> data = new HashMap<Integer, String>();

                for(ClimateHistoric l : list){
                    data.put(l.getDay(), l.getAvg_wind_speed() + "\t" + l.getHight_wind());
                }

                Map<Integer, String> sorted = new TreeMap<Integer, String>(data);
                wind.put(g.getPlace(), sorted);
            }
            return ok(Json.toJson(wind.get(location)));
        }else{
            return  redirect("/login");
        }
    }
}
