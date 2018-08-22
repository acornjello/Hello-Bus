package test.sks.com.busapp;

/**
 * Created by acornjello on 2017-08-23.
 */

public class AlertItem {
    private int _id;
    private int station_id;
    private String station_name;
    private int route_id;
    private String route_name;
    private String locationNo;
    private String predictTime;
    private int position;
    private String activityName;

    public AlertItem(int _id, int station_id, String station_name, int route_id, String route_name, int position, String activityName) {
        this._id = _id;
        this.station_id = station_id;
        this.station_name = station_name;
        this.route_id = route_id;
        this.route_name = route_name;
        this.position = position;
        this.activityName = activityName;
    }

    public int getId() { return _id; }

    public void setId(int _id) {
        this._id = _id;
    }

    public int getStationId() { return station_id; }

    public void setStationId(int station_id) { this.station_id = station_id; }

    public int getRouteId() { return route_id; }

    public void setRouteId(int route_id) { this.route_id = route_id; }

    public String getStationName() {
        return station_name;
    }

    public void setStationName(String station_name) {
        this.station_name = station_name;
    }

    public String getRouteName() {
        return route_name;
    }


    public String getLocationNo() { return locationNo; }

    public void setLocationNo(String locationNo) { this.locationNo = locationNo; }

    public String getPredictTime() { return predictTime; }

    public void setPredictTime(String predictTime) { this.predictTime = predictTime; }

    public int getPosition() { return position; }

    public void setPosition(int position) { this.position = position; }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
}


