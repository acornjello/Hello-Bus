package test.sks.com.busapp;

/**
 * Created by acorn on 2017-09-17.
 */

public class ArrivalAlarmItem {
    private int _id;
    private int route_id;
    private String route_name;
    private String station_name;
    private String mobile_no;
    private int station_id;
    private String activity_name;

    public ArrivalAlarmItem(int _id, int route_id, String route_name, int station_id, String station_name, String mobile_no, String activity_name) {
        this._id = _id;
        this.route_id = route_id;
        this.route_name = route_name;
        this.station_id = station_id;
        this.station_name = station_name;
        this.mobile_no = mobile_no;
        this.activity_name = activity_name;
    }

    public int getId() { return _id; }

    public void setId(int _id) {
        this._id = _id;
    }

    public int getRouteId() { return route_id; }

    public void setRouteId(int route_id) { this.route_id = route_id; }

    public String getRouteName() { return route_name; }

    public void setRoute_name(String route_name) { this.route_name = route_name; }

    public int getStationId() { return station_id; }

    public void setStationId(int station_id) { this.station_id = station_id; }

    public String getStationName() {
        return station_name;
    }

    public void setStationName(String station_name) {
        this.station_name = station_name;
    }

    public String getMobile_no() { return mobile_no; }

    public void setMobile_no(String mobile_no) { this.mobile_no = mobile_no; }

    public String getActivityName() {
        return activity_name;
    }

    public void setActivityName(String activity_name) {
        this.activity_name = activity_name;
    }
}
