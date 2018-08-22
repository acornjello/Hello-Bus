package test.sks.com.busapp;

/**
 * Created by acornjello on 2017-07-22.
 */


public class StationItem {
    private int route_id = -1;
    private String route_nm;
    private int sta_order = -1;
    private String station_nm;
    private String mobile_no;
    private boolean isExist = false;

    public StationItem(int route_id, String route_nm, int sta_order, String station_nm, String mobile_no) {
        this.route_id = route_id;
        this.route_nm = route_nm;
        this.sta_order = sta_order;
        this.station_nm = station_nm;
        this.mobile_no = mobile_no;
    }

    public String getStation_nm() {
        return station_nm;
    }

    public void setStation_nm(String station_nm) {
        this.station_nm = station_nm;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public boolean isExist() { return isExist; }

    public void setExist(boolean exist) { isExist = exist; }
}

