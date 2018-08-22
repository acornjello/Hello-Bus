package test.sks.com.busapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by acornjello on 2017-09-12.
 */

public class AlertServiceThread extends Thread {
    Handler handler;
    boolean isRun = true;
    
    private String stationName, routeName, predictTime, locationNo, alertPassed;
    private AlertItem alertItem;
    private ArrivalAlarmItem alarmItem;
    private DBHelper accessDB;
    private NotificationItem notiItem = new NotificationItem();
    private String serviceKey = "1234567890", url;
    private int stationIdSrch, routeIdSrch;
    private int cnt_run = 0;
    private boolean bSet = false, isPass = false;
    private Context context;

    private enum TagType {
        None,
        LocationNo1,
        PredictTime1,
        ResultCode
    }

    public AlertServiceThread(Handler handler) {
        this.handler = handler;
    }

    public void setContext(Context context) {
        this.context = context;
        accessDB = DBHelper.getInstance(context);
        alertItem = accessDB.selectAlert();
        alarmItem = accessDB.selectArrivalAlarm();

        if(alertItem != null) {
            routeName = alertItem.getRouteName();
            stationName = alertItem.getStationName();
            routeIdSrch = alertItem.getRouteId();
            stationIdSrch = alertItem.getStationId();
        } else {
            routeName = alarmItem.getRouteName();
            stationName = alarmItem.getStationName();
            routeIdSrch = alarmItem.getRouteId();
            stationIdSrch = alarmItem.getStationId();
        }
        notiItem.setRouteName(routeName);
        notiItem.setStationName(stationName);
    }

    public void stopForever() {
        synchronized (this) {
            this.isRun = false;
        }
    }

    public void run() {

        while(isRun) {
            try{

                Log.e("ALERT SERVICE", "run service" + cnt_run);

                url = "http://openapi.gbis.go.kr/ws/rest/busarrivalservice?serviceKey="+serviceKey+"&routeId="+routeIdSrch+"&stationId="+stationIdSrch;
                Log.d("#url", url.toString());
                new findPredictTimeTask().execute(url.toString());

                Thread.sleep(6200);
            }catch (Exception e) {}

        }
    }

    private class findPredictTimeTask extends AsyncTask<String, Void, String> { //url에 대한 웹문서 다운로드

        public findPredictTimeTask() {
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String) downloadUrl((String) urls[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        protected void onPostExecute(String result) { //다운로드 결과를 실행
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();
                TagType tagType = TagType.None;
                boolean bBreak = false;
                while (eventType != XmlPullParser.END_DOCUMENT) { // 현재 이벤트 타입이 end_document를 만날때까지 처리 반복.

                    if (eventType == XmlPullParser.START_DOCUMENT) {
                    } else if (eventType == XmlPullParser.START_TAG) {

                        String tag_name = xpp.getName();

                        if (tag_name.equals("locationNo1")) tagType = TagType.LocationNo1;
                        else if (tag_name.equals("predictTime1")) tagType = TagType.PredictTime1;
                        else if (tag_name.equals("resultCode")) tagType = TagType.ResultCode;

                    } else if (eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();

                        switch (tagType) {
                            case ResultCode:
                                if (content.equals("4")) {
                                    long now = System.currentTimeMillis();
                                    Date date = new Date(now);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    String getTime = sdf.format(date);

                                    alertPassed = getTime + "에 지나감";
                                    locationNo = "";
                                    predictTime = "지나감";

                                    bSet = false;
                                    isPass = true;
                                }
                                break;

                            case LocationNo1:
                                // 이미 지나간 알림이 되었을 때 (더이상 1번째 전이 아니나, 그 전이 1번째 전이었을 때)
                                if (bSet == true && !content.equals("1")) {
                                    locationNo = "";
                                    bSet = false;
                                    isPass = true;
                                    break;
                                }
                                // 곧 도착할 버스일 때 표시해두기
                                if (content.equals("1")) bSet = true;

                                locationNo = content + "번 째전";
                                break;

                            case PredictTime1:
                                if (bSet == true) {
                                    predictTime = "곧 도착";
                                } else if (isPass == true) {
                                    predictTime = "지나감";
                                    bSet=false;
                                    //isPass = false;
                                } else {
                                    predictTime = content + "분 후 도착";
                                }

                                ///notiItem.setPredictTime(predictTime);
                                break;

                            default:
                                break;
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {
                        tagType = TagType.None;
                    }
                    eventType = xpp.next();
                }
                if(isPass == true) {
                    isPass = false;
                    Log.e("BUS EVENT", "값 보냄] 지나가서 널값 보내야함");
                }
                notiItem.setLocationNo(locationNo);
                notiItem.setPredictTime(predictTime);

                Log.e("BUS EVENT", "값 보냄] 정류소:" + notiItem.getStationName() + "버스: " + notiItem.getRouteName() + " " + notiItem.getLocationNo() + " " + notiItem.getPredictTime());
                BusProvider.getInstance().post(notiItem);
                handler.sendEmptyMessage(0);  //BUS로 보낸다음 쓰레드에 있는 핸들러에게 메세지를 보냄


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private String downloadUrl(String myurl) throws IOException {

            HttpURLConnection conn = null;
            try {
                URL url = new URL(myurl);
                conn = (HttpURLConnection) url.openConnection();
                BufferedInputStream buf = new BufferedInputStream(conn.getInputStream());
                BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf, "utf-8"));
                String line = null;
                String page = "";
                while ((line = bufreader.readLine()) != null) {
                    page += line;
                }
                return page;
            } finally {
                conn.disconnect();
            }
        }
    }
}
