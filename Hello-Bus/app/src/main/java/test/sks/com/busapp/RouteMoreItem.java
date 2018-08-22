package test.sks.com.busapp;

/**
 * Created by acornjello on 2017-08-30.
 */

public class RouteMoreItem {
    private int id;
    private String city;
    private String enterprise;
    private String route_nm;
    private String start_station, end_station;
    private String weekdays_interval, weekend_interval;
    private String up_start_time, up_end_time, down_start_time, down_end_time;

    public RouteMoreItem(int id, String city, String enterprise, String route_nm, String start_station, String end_station, String weekdays_interval, String weekend_interval, String up_start_time, String up_end_time, String down_start_time, String down_end_time) {
        this.id = id;
        this.city = city;
        this.enterprise = enterprise;
        this.route_nm = route_nm;
        this.start_station = start_station;
        this.end_station = end_station;
        this.weekdays_interval = weekdays_interval;
        this.weekend_interval = weekend_interval;
        this.up_start_time = up_start_time;
        this.up_end_time = up_end_time;
        this.down_start_time = down_start_time;
        this.down_end_time = down_end_time;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public String getEnterprise() { return enterprise; }

    public void setEnterprise(String enterprise) { this.enterprise = enterprise; }

    public String getRoute_nm() { return route_nm; }

    public void setRoute_nm(String route_nm) { this.route_nm = route_nm; }

    public String getStart_station() { return start_station; }

    public void setStart_station(String start_station) { this.start_station = start_station; }

    public String getEnd_station() { return end_station; }

    public void setEnd_station(String end_station) { this.end_station = end_station; }

    public String getWeekdays_interval() { return weekdays_interval; }

    public void setWeekdays_interval(String weekdays_interval) { this.weekdays_interval = weekdays_interval; }

    public String getWeekend_interval() { return weekend_interval; }

    public void setWeekend_interval(String weekend_interval) { this.weekend_interval = weekend_interval; }

    public String getUp_start_time() { return up_start_time; }

    public void setUp_start_time(String up_start_time) { this.up_start_time = up_start_time; }

    public String getUp_end_time() { return up_end_time; }

    public void setUp_end_time(String up_end_time) { this.up_end_time = up_end_time; }

    public String getDown_start_time() { return down_start_time; }

    public void setDown_start_time(String down_start_time) { this.down_start_time = down_start_time; }

    public String getDown_end_time() { return down_end_time; }

    public void setDown_end_time(String down_end_time) { this.down_end_time = down_end_time; }
}
