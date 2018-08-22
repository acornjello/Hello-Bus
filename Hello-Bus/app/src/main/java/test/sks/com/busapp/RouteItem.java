package test.sks.com.busapp;

/**
 * Created by acornjello on 2017-07-26.
 */

public class RouteItem {
    private int route_id;
    private int station_id;
    private String route_nm;
    private String locationNo1;
    private String locationNo2;
    private String predictTime1;
    private String predictTime2;
    private String station_nm;
    private String mobile_no;
    private String city;
    private String x, y;
    private boolean isAlert = false;


    public RouteItem() {
        route_id = -1;
        station_id = -1;
        route_nm = "";
        locationNo1 = "-2";
        locationNo2 = "-2";
        predictTime1 = "도착정보없음";
        predictTime2 = "도착정보없음";
        station_nm = "";
        isAlert = false;
        mobile_no = "";
        city = "";
        x=  "";
        y= "";
    }
    // TODO (4) 인자 늘려야함?
    public RouteItem (int route_id, String route_nm){
        this.route_id = route_id;
        this.route_nm = route_nm;
    }
    // TODO station_nm이 start_station이 되는 경우 있음( city검색 시 사용 / SearchRoute )
    public RouteItem (int route_id, String route_nm, String station_nm){
        this.route_id = route_id;
        this.route_nm = route_nm;
        this.station_nm = station_nm;
    }

    public int getRoute_id() {
        return route_id;
    }

    public void setRoute_id(int route_id) {
        this.route_id = route_id;
    }

    public String getRoute_nm() {
        return route_nm;
    }

    public void setRoute_nm(String route_nm) {
        this.route_nm = route_nm;
    }

    public String getPredictTime1() { return predictTime1; }

    public void setPredictTime1(String predictTime1) { this.predictTime1 = predictTime1; }

    public String getPredictTime2() { return predictTime2; }

    public void setPredictTime2(String predictTime2) { this.predictTime2 = predictTime2; }

    public String getLocationNo1() { return locationNo1; }

    public void setLocationNo1(String locationNo1) { this.locationNo1 = locationNo1; }

    public String getLocationNo2() { return locationNo2; }

    public void setLocationNo2(String locationNo2) { this.locationNo2 = locationNo2; }

    public int getStation_id() { return station_id; }

    public void setStation_id(int station_id) {
        this.station_id = station_id;
    }

    public String getStation_nm() { return station_nm; }

    public void setStation_nm(String station_nm) { this.station_nm = station_nm; }

    public boolean isAlert() {
        return isAlert;
    }

    public void setAlert(boolean Alert) {
        isAlert = Alert;
    }

    public String getMobile_no() { return mobile_no; }

    public void setMobile_no(String mobile_no) { this.mobile_no = mobile_no; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public void updateArrivalInfo(RouteItem item) {
        this.locationNo1 = item.locationNo1;
        this.locationNo2 = item.locationNo2;
        this.predictTime1 = item.predictTime1;
        this.predictTime2 = item.predictTime2;
    }


}
