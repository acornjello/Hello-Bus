package test.sks.com.busapp;

public class PeripheralItem {
    private String station_nm;
    private String mobile_no;
    private boolean isExist = false;

    public PeripheralItem(String station_nm, String mobile_no) {
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
