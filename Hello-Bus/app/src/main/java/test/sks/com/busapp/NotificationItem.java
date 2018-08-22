package test.sks.com.busapp;

/**
 * Created by acornjello on 2017-08-23.
 */

public class NotificationItem {
    private String station_name;
    private String route_name;
    private String locationNo;
    private String predictTime;

    public NotificationItem() {

    }

    public String getStationName() {
        return station_name;
    }

    public void setStationName(String station_name) {
        this.station_name = station_name;
    }

    public String getRouteName() {
        return route_name;
    }

    public void setRouteName(String route_name) {
        this.route_name = route_name;
    }

    public String getLocationNo() { return locationNo; }

    public void setLocationNo(String locationNo) { this.locationNo = locationNo; }

    public String getPredictTime() { return predictTime; }

    public void setPredictTime(String predictTime) { this.predictTime = predictTime; }
}


