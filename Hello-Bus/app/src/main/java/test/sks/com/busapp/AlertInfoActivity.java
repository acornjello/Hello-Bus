package test.sks.com.busapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

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

public class AlertInfoActivity extends AppCompatActivity {

    private TextView txtStationName, txtRouteName, txtPredictTime, txtLocationNo;
    //private LinearLayout layoutAlertPassed;
    private DBHelper accessDB;
    private AlertItem alertItem;
    private String serviceKey = "1234567890", url;
    private int stationIdSrch, routeIdSrch;
    private Timer timer;
    private TimerTask timerTask;
    private int cnt_run = 0;
    private boolean bSet = false, isPass = false;
    private enum TagType {
        None,
        LocationNo1,
        PredictTime1
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("탑승 요청 정보");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("탑승 요청 정보");
        txtStationName = (TextView) findViewById(R.id.text_station_name);
        txtRouteName = (TextView) findViewById(R.id.text_route_name);
        txtLocationNo = (TextView) findViewById(R.id.text_route_locationNo1);
        txtPredictTime = (TextView) findViewById(R.id.text_route_predict1);


        accessDB = DBHelper.getInstance(getApplicationContext());
        accessDB.connect();

        alertItem = accessDB.selectAlert();

        if(alertItem != null) {
            txtStationName.setText( alertItem.getStationName() );
            txtRouteName.setText( alertItem.getRouteName() +"번" );
            startBackgroundPerform();
        } else {
            Toast.makeText(getApplicationContext(), "요청한 탑승 알람이 없습니다.", Toast.LENGTH_LONG).show();
        }

    }

    public void btnGoHome(View v) {
        Intent intent = new Intent(this, ModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void startBackgroundPerform() {
        final android.os.Handler handler = new android.os.Handler();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("ALERT BACKGROUND", "run" + cnt_run);

                            routeIdSrch = alertItem.getRouteId();
                            stationIdSrch = alertItem.getStationId();
                            url = "http://openapi.gbis.go.kr/ws/rest/busarrivalservice?serviceKey="+serviceKey+"&routeId="+routeIdSrch+"&stationId="+stationIdSrch;
                            Log.d("#url", url.toString());
                            new findPredictTimeTask().execute(url.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 6000);
    }

    public void btnCancelAlert(View view) {
        txtStationName.setText("요청한 탑승 알림이 없습니다.");
        txtRouteName.setText("");
        txtLocationNo.setText("");
        txtPredictTime.setText("");

        btnClick();
        if(alertItem != null) {
            routeIdSrch = 0;
            stationIdSrch = 0;
            accessDB.deleteAllAlert();
//            BusProvider.getInstance().post(AlertStatus.CANCEL2);
        }
        if(timer != null) timer.cancel();

        Intent intent = new Intent(this, AlertService.class);
        NotificationManager Notifi_M = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        Notifi_M.cancel(777);
        stopService(intent);

        Toast.makeText(getApplicationContext(), "탑승 요청 알림을 취소하였습니다.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(timer != null) timer.cancel();
        finish();
    }

    private class findPredictTimeTask extends AsyncTask<String, Void, String> { //url에 대한 웹문서 다운로드

        public findPredictTimeTask() { }

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

                while (eventType != XmlPullParser.END_DOCUMENT) { // 현재 이벤트 타입이 end_document를 만날때까지 처리 반복.

                    if (eventType == XmlPullParser.START_DOCUMENT) {
                    } else if (eventType == XmlPullParser.START_TAG) {
                        String tag_name = xpp.getName();

                        if (tag_name.equals("locationNo1")) tagType = TagType.LocationNo1;
                        else if (tag_name.equals("predictTime1")) tagType = TagType.PredictTime1;
                    } else if (eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();

                        switch (tagType) {
                            case LocationNo1:
                                // 이미 지나간 알림이 되었을 때
                                if(bSet == true && !content.equals("1")) {

                                    /** 시간 **/
                                    long now = System.currentTimeMillis();
                                    Date date = new Date(now);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    String getTime = sdf.format(date);


                                    txtStationName.setText("현재 요청한 탑승 정보가 없습니다.");
                                    txtRouteName.setText("");
                                    txtLocationNo.setText("");

                                    if(alertItem != null) {
                                        routeIdSrch = 0;
                                        stationIdSrch = 0;
                                        accessDB.deleteAllAlert();
                                    }
                                    if(timer != null) timer.cancel();

                                    bSet = false;
                                    isPass = true;
                                    break;
                                }

                                // 곧 도착할 버스일 때 표시해두기
                                if( content.equals("1") ) bSet = true;

                                txtLocationNo.setText(content + "번 째전");
                                break;
                            case PredictTime1:
                                if(bSet == true) {
                                    txtPredictTime.setText("곧 도착 예정");
                                } else if (isPass == true) {
                                    txtPredictTime.setText("");
                                    isPass = false;
                                } else {
                                    txtPredictTime.setText(content + "분 후 도착예정");
                                }
                                break;
                            default:
                                break;
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {
                        tagType = TagType.None;
                    }
                    eventType = xpp.next();
                }
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
                while((line = bufreader.readLine()) != null) {
                    page += line;
                }
                return page;
            } finally {
                conn.disconnect();
            }
        }
    }


    public void btnClick() {
        FirebaseDatabase.getInstance().getReference().child("Alert")
                .child(String.valueOf(alertItem.getStationId()))
                .child(alertItem.getRouteName())
                .runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer value = mutableData.getValue(Integer.class);

                if (value != null) {
                    if (value > 0) value--;
                }
                mutableData.setValue(value);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d("$$", ":onComplete:" + databaseError);
            }

        });
    }





}
