package test.sks.com.busapp;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by acornjello on 2017-07-21.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String PACKAGE_NAME = "test.sks.com.busapp";
    private static final String DB_PATH = "/data/data/" + PACKAGE_NAME  + "/databases";
    private static final String DB_NAME = "BusDB.sqlite";
    private static Context mContext;

    private static DBHelper instance = null;
    private static SQLiteDatabase myDatabase;
    //private Cursor cursor;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }
    public static DBHelper getInstance(Context context) {
        if(instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    public static void connect () {
        if ( myDatabase == null ) {
            String myPath = DB_PATH + "/" + DB_NAME;
            myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            Log.e("DB OPENED", "DONE");
        }
    }

    public void close () {
        try {
            if(myDatabase != null && myDatabase.isOpen()) {
                myDatabase.close();
                instance = null;
                Log.e("DB CLOSED", "DONE");
            }
        } catch (Exception e) {}
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS");
    }

    /** ROUTE **/
    // 버스이름으로 버스 정보(id, 이름, 기점정류소번호 받아오기) -> 기점 정류소은 RouteInfo_M의 버스들을 구분하는데 쓰임
    public ArrayList<RouteItem> selectRouteName(String ROUTE_NM) {
        ArrayList<RouteItem> arr = new ArrayList<>();
        Cursor cursor = myDatabase.rawQuery("select ROUTE_ID, ROUTE_NM, STATION_NM from GGD_RouteStationInfo_M where ROUTE_NM LIKE '" + ROUTE_NM + "%' and STA_ORDER = 1 ORDER BY ROUTE_NM", null);
        while(cursor.moveToNext()){
            RouteItem item = new RouteItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            arr.add(item);
            Log.d("selectRouteName", String.valueOf(cursor.getInt(0))+"/"+ cursor.getString(1) + "/");
        }
        cursor.moveToFirst();
        cursor.close();
        return arr;
    }

    public int selectRouteId(String ROUTE_NM) {
        int route_id = 0;
        Cursor cursor = myDatabase.rawQuery("select DISTINCT ROUTE_ID from GGD_RouteStationInfo_M where ROUTE_NM = '" + ROUTE_NM +"'", null);
        while(cursor.moveToNext()) {
            route_id = cursor.getInt(0);
            break;
        }
        cursor.close();
        return route_id;
    }

    public int selectRouteId(String ROUTE_NM, String STATION_NM) {
        int route_id = 0;
        Cursor cursor = myDatabase.rawQuery("select DISTINCT ROUTE_ID from GGD_RouteStationInfo_M where ROUTE_NM = '" + ROUTE_NM +"' and STATION_NM = '" + STATION_NM + "';", null);
        while(cursor.moveToNext())
            route_id = cursor.getInt(0);
        cursor.close();
        return route_id;
    }

    public ArrayList<StationItem> selectRouteInfoByRouteID(int ROUTE_ID) {
        ArrayList<StationItem> arr = new ArrayList<>();
        Cursor cursor = myDatabase.rawQuery("select * from GGD_RouteStationInfo_M where ROUTE_ID = " + ROUTE_ID, null);
        while(cursor.moveToNext()) {
            arr.add(new StationItem(cursor.getInt(1),cursor.getString(2),cursor.getInt(3), cursor.getString(4), cursor.getString(5)));
        }
        cursor.moveToFirst();
        cursor.close();
        return arr;
    }

    public ArrayList<RouteItem> selectAllRoute() {
        ArrayList<RouteItem> arr = new ArrayList<>();
        Cursor cursor = myDatabase.rawQuery("select DISTINCT ROUTE_ID, ROUTE_NM from GGD_RouteStationInfo_M", null);
        while(cursor.moveToNext()) {
            Log.d("AllRouteStationInfo", cursor.getString(1));
            arr.add(new RouteItem(cursor.getInt(0), cursor.getString(1)));
        }
        cursor.close();
        return arr;
    }

    /** Station **/

    public String selectStationName(int ROUTE_ID, int STA_ORDER) {
        String stationName = "";
        Cursor cursor = myDatabase.rawQuery("select DISTINCT STATION_NM from GGD_RouteStationInfo_M where ROUTE_ID = " + ROUTE_ID +" and STA_ORDER = " + STA_ORDER, null);
        while(cursor.moveToNext())
            stationName = cursor.getString(0);
        cursor.close();
        return stationName;
    }

    public String selectMobileNo(int ROUTE_ID, int STA_ORDER) {
        String mobileNo = "";
        Cursor cursor = myDatabase.rawQuery("select DISTINCT MOBILE_NO from GGD_RouteStationInfo_M where ROUTE_ID = " + ROUTE_ID +" and STA_ORDER = " + STA_ORDER, null);
        while(cursor.moveToNext())
            mobileNo = cursor.getString(0);
        cursor.close();
        return mobileNo;
    }

    public ArrayList<StationItem> selectStation(String MOBILE_OR_NAME) {
        ArrayList<StationItem> arr = new ArrayList<>();
        Cursor cursor = myDatabase.rawQuery("select DISTINCT STATION_NM, MOBILE_NO from GGD_RouteStationInfo_M where MOBILE_NO = '" + MOBILE_OR_NAME + "' or STATION_NM LIKE '" + MOBILE_OR_NAME + "%';", null);
        while(cursor.moveToNext()) {
            arr.add(new StationItem(-1, null, -1, cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return arr;
    }

    public ArrayList<RouteItem> selectStationInfobyMobileNo(String MOBILE_NO) {
        ArrayList<RouteItem> arr = new ArrayList<>();
        Cursor cursor = myDatabase.rawQuery("select * from GGD_RouteStationInfo_M where MOBILE_NO = '" + MOBILE_NO + "'", null);
        while(cursor.moveToNext()) {
            Log.d("@@selectStationInfo", cursor.getInt(1) + " " + cursor.getString(2));
            arr.add(new RouteItem(cursor.getInt(1),cursor.getString(2)));
        }
        cursor.close();
        return arr;
    }

    /** RouteInfo **/
    public String selectStartStationName(int ROUTE_ID) {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select STATION_NM from GGD_RouteStationInfo_M where ROUTE_ID = " + ROUTE_ID + " and STA_ORDER = 1", null);
        while(cursor.moveToNext()){
            str = cursor.getString(0);
        }
        cursor.close();
        return str;
    }
    public String selectEndStationName(String ROUTE_NM, String START_STATION) {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select END_STATION from GGD_RouteInfo_M where ROUTE_NM = '" + ROUTE_NM + "' and START_STATION = '" + START_STATION + "'", null);
        while(cursor.moveToNext()){
            str = cursor.getString(0);
        }

        cursor.close();
        return str;
    }

    public int selectEndStationSeq(String ROUTE_NM, String STATION_NM) {
        int arr = 0;
        Cursor cursor = myDatabase.rawQuery("select STA_ORDER from GGD_RouteStationInfo_M where ROUTE_NM = '" + ROUTE_NM + "' and STATION_NM = '" + STATION_NM + "';", null);
        while(cursor.moveToNext()){
            arr = cursor.getInt(0);
        }
        cursor.close();
        return arr;
    }

    public int selectCurStationSeq(String ROUTE_NM, String MOBILE_NO) {
        int arr = 0;
        Cursor cursor = myDatabase.rawQuery("select STA_ORDER from GGD_RouteStationInfo_M where ROUTE_NM = '" + ROUTE_NM + "' and MOBILE_NO = '" + MOBILE_NO + "';", null);

        while (cursor.moveToNext() ) {
            arr = cursor.getInt(0);
        }
        cursor.close();
        return arr;
    }

    public String selectLastStationName(int ROUTE_ID) {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select STATION_NM from GGD_RouteStationInfo_M where ROUTE_ID = " + ROUTE_ID, null);
        while(cursor.moveToNext()) {
            str = cursor.getString(0);
        }
        cursor.close();
        return str;
    }

    public int selectLastStationSeq(int ROUTE_ID) {
        int arr = 0;
        Cursor cursor = myDatabase.rawQuery("select STA_ORDER from GGD_RouteStationInfo_M where ROUTE_ID = " + ROUTE_ID, null);
        while(cursor.moveToNext()) {
            arr = cursor.getInt(0);
        }
        cursor.close();
        return arr;
    }

    public RouteMoreItem selectRouteMore(String ROUTE_NM, String START_STATION) {
        RouteMoreItem str = null;
        Cursor cursor = myDatabase.rawQuery("select * from GGD_RouteInfo_M where ROUTE_NM = '" + ROUTE_NM + "' and START_STATION = '" + START_STATION + "'", null);
        cursor.moveToNext();
        str = new RouteMoreItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10), cursor.getString(11));
        cursor.close();
        return str;
    }

    public String selectRouteCity(String ROUTE_NM, String START_STATION) {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select CITY from GGD_RouteInfo_M where ROUTE_NM = '" + ROUTE_NM + "' and START_STATION = '" + START_STATION + "';", null);
        while(cursor.moveToNext()) {
            str = cursor.getString(0);
        }
        cursor.close();
        return str;
    }

    /** CITY띄어서 검색 **/
    public String selectStartStationByRouteCity(String ROUTE_NM, String CITY) {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select START_STATION from GGD_RouteInfo_M where ROUTE_NM = '" + ROUTE_NM + "' and CITY = ' " + CITY + "';", null);
        while(cursor.moveToNext()){
            str = cursor.getString(0);
            break;
        }
        cursor.close();
        return str;
    }

    /** MyFavorite **/

    public ArrayList<FavoriteRouteItem> selectAllFavorite() {
        ArrayList<FavoriteRouteItem> arr = new ArrayList<>();
        Cursor cursor = myDatabase.rawQuery("select * from MyFavorite", null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                arr.add(new FavoriteRouteItem(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6)));
                 Log.e("SELECT ALL FAVORITE",  cursor.getInt(0) + " " +  cursor.getInt(1) + " " + cursor.getString(2)+ " " + cursor.getString(3)+ " " + cursor.getString(4)+ " " + cursor.getString(5));

            }
            cursor.close();
        }
        return arr;
    }
    public ArrayList<FavoriteRouteItem> selectAllFavoriteRoute() {
        ArrayList<FavoriteRouteItem> arr = new ArrayList<>();
        Cursor cursor = myDatabase.rawQuery("select * from MyFavorite where STATION_NM is null", null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                arr.add(new FavoriteRouteItem(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6)));
                Log.e("SELECT ALL FAV_ROUTE",  cursor.getInt(0) + " " +  cursor.getInt(1) + " " + cursor.getString(2)+ " " + cursor.getString(3)+ " " + cursor.getString(4)+ " " + cursor.getString(5));

            }
            cursor.close();
        }
        return arr;
    }
    public ArrayList<FavoriteRouteItem> selectAllFavoriteStation() {
        ArrayList<FavoriteRouteItem> arr = new ArrayList<>();
        Cursor cursor = myDatabase.rawQuery("select * from MyFavorite where ROUTE_NAME is null or (ROUTE_NAME is not null and STATION_NM is not null) ", null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                arr.add(new FavoriteRouteItem(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6)));
                Log.e("SELECT ALL FAV_STATION",  cursor.getInt(0) + " " +  cursor.getInt(1) + " " + cursor.getString(2)+ " " + cursor.getString(3)+ " " + cursor.getString(4)+ " " + cursor.getString(5));

            }
            cursor.close();
        }
        return arr;
    }

    public FavoriteRouteItem selectFavoriteRoute(int ROUTE_ID, String ROUTE_NAME) {
        FavoriteRouteItem item = null;
        Log.e("SELECT FAVORITE", "select _id, ROUTE_ID, ROUTE_NAME from MyFavorite where STATION_NM is null and ROUTE_NAME = '" + ROUTE_NAME + "';");
        Cursor cursor = myDatabase.rawQuery("select _id, ROUTE_ID, ROUTE_NAME from MyFavorite where STATION_NM is null and ROUTE_NAME = '" + ROUTE_NAME + "';", null);
        while (cursor.moveToNext()) {
            item = new FavoriteRouteItem(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), null, -1, null, null);
            Log.e("SEARCH FAV ROUTE", cursor.getInt(0) + " / " + cursor.getString(1) + " / " + cursor.getString(2));
        }
        cursor.close();
        return item;
    }

    // TODO int에 null이 안되서 확인용으로 -1을 넣음
    public FavoriteRouteItem selectFavoriteStation(String STATION_NM, String MOBILE_NO) {
        FavoriteRouteItem item = null;
        Cursor cursor = myDatabase.rawQuery("select _id, STATION_ID, STATION_NM, MOBILE_NO from MyFavorite where ROUTE_ID is null and ROUTE_NAME is null and STATION_NM = '" + STATION_NM + "' and MOBILE_NO = '" + MOBILE_NO + "';", null);
        Log.e("SELECT FAVORITE", "select _id, STATION_ID, STATION_NM, MOBILE_NO from MyFavorite where ROUTE_NAME is null and STATION_NM = '" + STATION_NM + "' and MOBILE_NO = '" + MOBILE_NO + "';");
        while (cursor.moveToNext()) {
            item = new FavoriteRouteItem(cursor.getInt(0), -1, null, null, cursor.getInt(1), cursor.getString(2), cursor.getString(3));
            Log.e("SEARCH FAV STAION", cursor.getInt(0) + " / " + cursor.getString(1) + " / " + cursor.getString(2));
        }
        cursor.close();
        return item;
    }

    public FavoriteRouteItem selectFavoriteRouteStation(String ROUTE_NAME, String STATION_NM, String MOBILE_NO) {
        Cursor cursor = myDatabase.rawQuery("select * from MyFavorite where ROUTE_NAME = '" + ROUTE_NAME + "' and STATION_NM = '" + STATION_NM + "' and MOBILE_NO = '" + MOBILE_NO + "';", null);
        while( cursor.moveToNext() ) {
            FavoriteRouteItem arr = new FavoriteRouteItem(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6));
            Log.e("@@SEARCH FAVORITE@@", cursor.getInt(0) + " / " + cursor.getInt(1) + " / " + cursor.getString(2) + " / " + cursor.getString(3) + " / " + cursor.getString(4) + " / " + cursor.getString(5));
            return arr;
        }
        cursor.close();
        return null;
    }

    public void insertFavoriteRoute(int ROUTE_ID, String ROUTE_NAME, String ROUTE_CITY) {
        myDatabase.execSQL("insert into MyFavorite values ( null, " + ROUTE_ID + ", '" + ROUTE_NAME + "', '" + ROUTE_CITY + "', null, null, null)");
        Log.e("INSERT FAV ROUTE", "insert into MyFavorite values ( null, null, null, null, "+ ROUTE_ID + ", '"+ ROUTE_NAME + "', '" + ROUTE_CITY + "')");
    }
    public void insertFavoriteRouteStation(int ROUTE_ID, String ROUTE_NAME, String ROUTE_CITY, int STATION_ID, String STATION_NM, String MOBILE_NO) {
        myDatabase.execSQL("insert into MyFavorite values ( null, " + ROUTE_ID + ", '" + ROUTE_NAME + "', '" + ROUTE_CITY + "', " + STATION_ID + ", '" + STATION_NM + "', '" + MOBILE_NO + "');");
        Log.e("INSERT FAV ROUTESTATION", "insert into MyFavorite values ( null, "+ ROUTE_ID + ", " + ROUTE_NAME + ", " + STATION_ID + ", '"+ STATION_NM + "', '" + MOBILE_NO + "')");
    }

    public void insertFavoriteStation(int STATION_ID, String STATION_NM, String MOBILE_NO) {
        myDatabase.execSQL("insert into MyFavorite values ( null, null, null, null, "+ STATION_ID + ", '" + STATION_NM + "', '" + MOBILE_NO + "')");
        Log.e("INSERT FAV STATION", "insert into MyFavorite values ( null, null, null, null, "+ STATION_ID + ", '"+ STATION_NM + "', '" + MOBILE_NO + "')");
    }

    public void deleteFavorite(int _id) {
        myDatabase.execSQL("delete from MyFavorite where _id = " + _id);
        Log.e("#DELETE FAVORITE##", "id : " + _id);
    }

    public void deleteFavorite2(int ROUTE_ID, String ROUTE_NAME, String ROUTE_CITY, String STATION_NM, String MOBILE_NO) {
        if(STATION_NM == null) {  // 버스
            myDatabase.execSQL("delete from MyFavorite where ROUTE_ID =" + ROUTE_ID + " and ROUTE_NAME = '" + ROUTE_NAME + "' and STATION_NM is null and MOBILE_NO is null;");
            Log.e("#DEL1 FAV BUS##", ROUTE_NAME + " " + ROUTE_CITY);
        } else if (ROUTE_NAME == null) { // 정류소
            myDatabase.execSQL("delete from MyFavorite where ROUTE_ID is null and ROUTE_NAME is null and STATION_NM = '" + STATION_NM + "' and MOBILE_NO = '" + MOBILE_NO + "';");
            Log.e("#DEL2 FAV STATION##", STATION_NM + " " + MOBILE_NO);
        } else { // 특정 버스
            myDatabase.execSQL("delete from MyFavorite where ROUTE_ID = " + ROUTE_ID + " and ROUTE_NAME = '" + ROUTE_NAME + "' and STATION_NM = '" + STATION_NM + "' and MOBILE_NO = '" + MOBILE_NO + "';");
            Log.e("#DEL3 FAV BUS_STA##", "  ROUTE_ID = " + ROUTE_ID + "  ROUTE_NAME = '" + ROUTE_NAME + "' and STATION_NM = '" + STATION_NM + "' MOBILE_NO = '" + MOBILE_NO +"'");
        }
    }
    // STATION_NM == null => 정류소 or 버스
    //=> ROUTE_ID가 있다 (버스), 없다 정류소

    // 경우1. ROUTE_ID 또는 ROUTE_NAME이 없거나 경우 => 정류소 즐겨찾기
    // 경우2. ROUTE_ID 또는 ROUTE_NAME이 있고, STATION_NM 또는 MOBILE_NO이 없는 경우 => 버스 즐겨찾기
    // 경우3. 모두 있는 경우 => 특정 정류소 즐겨찾기

    /** MyAlert **/

    public AlertItem selectAlert() {
        AlertItem item = null;
        Cursor cursor = myDatabase.rawQuery("select * from MyAlert", null);

        while(cursor.moveToNext()) {
            item = new AlertItem(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6));
        }
        cursor.close();
        return item;
    }

    public void insertAlert(int STATION_ID, String STATION_NAME, int ROUTE_ID, String ROUTE_NAME, int POSITION, String ACTIVITY_NAME) {
        myDatabase.execSQL("insert into MyAlert values ( null, " + STATION_ID + ", '" + STATION_NAME + "', " + ROUTE_ID +", '" + ROUTE_NAME +"', " + POSITION +", '"+ ACTIVITY_NAME +"');");
        Log.e("##INSERT ALERT##", STATION_NAME + " / " + ROUTE_NAME + " / " + ACTIVITY_NAME);
        // TODO(5) TOAST in the other activity
//        Toast.makeText(StationInfoActivity.this, STATION_NAME + "번 버스에 알림요청을 하였습니다.", Toast.LENGTH_LONG).show();
    }

    public void deleteAllAlert() {
        myDatabase.execSQL("delete from MyAlert");
        Log.e("#DELETE ALERT#", "All");
    }

    /** MyArrivalAlarm **/
    public ArrivalAlarmItem selectArrivalAlarm() {
        ArrivalAlarmItem item = null;
        Cursor cursor = myDatabase.rawQuery("select * from MyArrivalAlarm", null);

        while(cursor.moveToNext()) {
            item = new ArrivalAlarmItem(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
        }
        cursor.close();
        return item;
    }

    public void insertArrivalAlarm(int _id, int ROUTE_ID, String ROUTE_NAME, int STATION_ID, String STATION_NM, String MOBILE_NO, String ACTIVITY_NAME) {
        myDatabase.execSQL("insert into MyArrivalAlarm values ( null, " + ROUTE_ID + ", '" + ROUTE_NAME + "', " + STATION_ID + ", '" + STATION_NM +"', '" + MOBILE_NO +"', '"+ ACTIVITY_NAME +"');");
        Log.e("#INSERT ARRIVAL ALARM#", STATION_NM + " / " + ROUTE_NAME + " / " + ACTIVITY_NAME);
    }

    public void deleteAllArrivalAlarm() {
        myDatabase.execSQL("delete from MyArrivalAlarm");
        Log.e("#DELETE ARRIVAL ALARM#", "All");
    }

    /** User **/

    public String selectUserType() {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select USER_TYPE from User where _id = 1;", null);
        while(cursor.moveToNext())
            str = cursor.getString(0);
        cursor.close();
        return str;
    }
    public String selectDriverRouteName() {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select ROUTE_NAME from User where _id = 1;", null);
        while(cursor.moveToNext())
            str = cursor.getString(0);
        cursor.close();
        return str;
    }
    public String selectDriverPlateNo() {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select PLATE_NO from User where _id = 1;", null);
        while(cursor.moveToNext())
            str = cursor.getString(0);
        cursor.close();
        return str;
    }
    public String selectDriverCity() {
        String str = "";
        Cursor cursor = myDatabase.rawQuery("select CITY from User where _id = 1;", null);
        while(cursor.moveToNext())
            str = cursor.getString(0);
        cursor.close();
        return str;
    }

    public void updateUserType(String USER_TYPE) {
        myDatabase.execSQL("update USER set USER_TYPE='" + USER_TYPE + "' where _id= 1;");
    }
    public void updateDriverInfo(String ROUTE_NAME, String PLATE_NO, String CITY) {
        myDatabase.execSQL("update USER set ROUTE_NAME = " + ROUTE_NAME + ", PLATE_NO = '" + PLATE_NO + "', CITY = '" + CITY + "' where _id= 1;");
    }
}