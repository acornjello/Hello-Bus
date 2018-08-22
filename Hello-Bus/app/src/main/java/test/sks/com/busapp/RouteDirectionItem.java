package test.sks.com.busapp;

/**
 * Created by acornjello on 2017-09-02.
 */

public class RouteDirectionItem {

    private int endStationSeq, curStationSeq, lastStationSeq;
    private String startStationName, endStationName, lastStationName;

    public RouteDirectionItem(int endStationSeq, int curStationSeq, int lastStationSeq, String startStationName, String endStationName, String lastStationName) {
        this.endStationSeq = endStationSeq;
        this.curStationSeq = curStationSeq;
        this.lastStationSeq = lastStationSeq;
        this.startStationName = startStationName;
        this.endStationName = endStationName;
        this.lastStationName = lastStationName;
    }

    public int getEndStationSeq() {
        return endStationSeq;
    }

    public void setEndStationSeq(int endStationSeq) {
        this.endStationSeq = endStationSeq;
    }

    public int getCurStationSeq() {
        return curStationSeq;
    }

    public void setCurStationSeq(int curStationSeq) {
        this.curStationSeq = curStationSeq;
    }

    public int getLastStationSeq() {
        return lastStationSeq;
    }

    public void setLastStationSeq(int lastStationSeq) {
        this.lastStationSeq = lastStationSeq;
    }

    public String getStartStationName() {
        return startStationName;
    }

    public void setStartStationName(String startStationName) {
        this.startStationName = startStationName;
    }

    public String getEndStationName() {
        return endStationName;
    }

    public void setEndStationName(String endStationName) {
        this.endStationName = endStationName;
    }

    public String getLastStationName() {
        return lastStationName;
    }

    public void setLastStationName(String lastStationName) {
        this.lastStationName = lastStationName;
    }
}
