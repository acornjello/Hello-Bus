package test.sks.com.busapp;

/**
 * Created by acornjello on 2017-08-12.
 */

public class FavoriteRouteItem {
    private int id;
    private int route_id;
    private String route_name;
    private String route_city;
    private int station_id;
    private String station_name;
    private String mobile_no;
    private String x;
    private String y;


    public FavoriteRouteItem (int id, int route_id, String route_name, String route_city) {
        this.id = id;
        this.route_id = route_id;
        this.route_name = route_name;
        this.route_city = route_city;
    }

    public FavoriteRouteItem (int id, int route_id, String route_name, String route_city, int station_id, String station_name, String mobile_no) {
        this.id = id;
        this.route_id = route_id;
        this.route_name = route_name;
        this.route_city = route_city;
        this.station_id = station_id;
        this.station_name = station_name;
        this.mobile_no = mobile_no;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public int getRoute_id() { return route_id; }

    public void setRoute_id(int route_id) { this.route_id = route_id; }

    public String getRoute_name() { return route_name; }

    public void setRoute_name(String route_name) { this.route_name = route_name; }

    public String getRoute_city() {  return route_city; }

    public void setRoute_city(String route_city) { this.route_city = route_city; }

    public int getStation_id() { return station_id; }

    public void setStation_id(int station_id) { this.station_id = station_id; }

    public String getStation_name() { return station_name; }

    public void setStation_name(String station_name) { this.station_name = station_name; }

    public String getMobile_no() { return mobile_no; }

    public void setMobile_no(String mobile_no) { this.mobile_no = mobile_no; }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
