package test.sks.com.busapp;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;


public class Driver extends Fragment {
    private DBHelper accessDB;
    private TextView txtRouteName, txtPlateNo, txtStationName, txtNextStationName, txtPassengerNum, txt_info;
    private TextView txtDisplayStaName, txtDisplayPsgNum;
    private ImageView layoutDisplay;

    private int cnt_run = 0;
    private int routeId = 0, stationId = 0, stationSeq = 0, nextStationId = 0;
    private String routeName = "", plateNo = "", city = "", stationName = "", nextStationName = "", nextStationMobileNo = "";
    private String startStationName = "";
    private String serviceKey = "1234567890";
    private String url1, serviceUrl1, serviceUrl2, strSrch1, strSrch2;
    private URL url2;

    Timer timer;
    TimerTask timerTask;


    private enum TagType {
        None,
        StationId,
        StationSeq,
        busArrivalList,
        PlateNo,
        MobileNo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_driver, container, false);

        accessDB = DBHelper.getInstance(getActivity().getApplicationContext());
        txtStationName = (TextView) view.findViewById(R.id.txt_station_name);
        txtDisplayStaName = (TextView) view.findViewById(R.id.txt_display_sta_name);
        txtDisplayPsgNum = (TextView) view.findViewById(R.id.txt_display_psg_num);
        layoutDisplay = (ImageView) view.findViewById(R.id.layout_display);
        txt_info = (TextView) view.findViewById(R.id.txt_info);


//        try {
//            ringtone.wait((long) 0);
//        } catch (Exception e) {
//        }

        routeName = accessDB.selectDriverRouteName();
        city = accessDB.selectDriverCity();
        startStationName = accessDB.selectStartStationByRouteCity(routeName, city);
        routeId = accessDB.selectRouteId(routeName, startStationName);
        plateNo = accessDB.selectDriverPlateNo();

        layoutDisplay.setBackground(Driver.this.getResources().getDrawable(R.drawable.greensignal));
        if (!(routeName.equals("") || plateNo.equals(""))) {
            txt_info.setText("  [" + plateNo + "] 현재 " + routeName + "번 버스 운행 중");

            serviceUrl1 = "http://openapi.gbis.go.kr/ws/rest/buslocationservice";
            strSrch1 = String.valueOf(routeId);
            url1 = serviceUrl1 + "?serviceKey=" + serviceKey + "&keyword=" + strSrch1;
            Log.d("#url1", url1.toString());

            startBackgroundPerform();

        } else
            txt_info.setText("  버스 정보가 입력되지 않았습니다.");
//        txtRouteName.setText( routeName );
//        txtRouteId.setText( routeId+"" );
//        txtPlateNo.setText( plateNo );


        serviceUrl1 = "http://openapi.gbis.go.kr/ws/rest/buslocationservice";
        strSrch1 = String.valueOf(routeId);
        url1 = serviceUrl1 + "?serviceKey=" + serviceKey + "&keyword=" + strSrch1;
        Log.d("#url1", url1.toString());

        startBackgroundPerform();


        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

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
                            Log.d("@@DRIVER@@", "run" + ++cnt_run);

                            serviceUrl1 = "http://openapi.gbis.go.kr/ws/rest/buslocationservice";
                            strSrch1 = routeId + "";
                            url1 = serviceUrl1 + "?serviceKey=" + serviceKey + "&routeId=" + strSrch1;
                            Log.d("#url1", url1.toString());
                            findBusLocationTask performBackgroundTask1 = new findBusLocationTask();
                            performBackgroundTask1.execute(url1.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 5000);
    }

    private class findBusLocationTask extends AsyncTask<String, Void, String> { //url에 대한 웹문서 다운로드

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String) downloadUrl((String) urls[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            boolean bSet1 = false, bSet2 = false;
            Log.e("#", "운전자 버스위치찾기");
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();

                TagType tagType = TagType.None;

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_DOCUMENT) {
                    } else if (eventType == XmlPullParser.START_TAG) { // 시작 태그일 경우

                        String tag_name = xpp.getName();

                        if (tag_name.equals("stationId")) tagType = TagType.StationId;
                        else if (tag_name.equals("stationSeq")) tagType = TagType.StationSeq;
                        else if (tag_name.equals("plateNo")) tagType = TagType.PlateNo;
                        else if (tag_name.equals("busArrivalList"))
                            tagType = TagType.busArrivalList;

                    } else if (eventType == XmlPullParser.TEXT) {

                        String content = xpp.getText();

                        switch (tagType) {
                            case StationId:
                                if (bSet1 == true) {
                                    if (nextStationId == Integer.parseInt(content)) {
                                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Alert");
                                        DatabaseReference stationRef = rootRef.child(String.valueOf(nextStationId));
                                        DatabaseReference routeRef = stationRef.child(routeName);

                                        routeRef.setValue(0);
                                    }
                                    stationId = Integer.parseInt(content);
                                    bSet1 = false;
                                }
                                break;
                            case StationSeq:
                                if (bSet2 == true) {
                                    Log.e("FIND BUS LOCATION", "STATION_ID : " + stationId + "/ STAITON_SEQ : " + stationSeq);
                                    stationSeq = Integer.parseInt(content);
                                    bSet2 = false;
                                }
                                break;
                            case PlateNo:
                                if (content.equals(plateNo)) {
                                    Log.e("FIND PLATE_NO", content);
                                    bSet1 = true;
                                    bSet2 = true;
                                }
                                break;
                            default:
                                break;
                        }
                        tagType = TagType.None;
                    } else if (eventType == XmlPullParser.END_TAG) {
                    }
                    eventType = xpp.next();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            stationName = accessDB.selectStationName(routeId, stationSeq);
            nextStationName = accessDB.selectStationName(routeId, stationSeq + 1);
            nextStationMobileNo = accessDB.selectMobileNo(routeId, stationSeq + 1);

            updateText();

            try {
                serviceUrl2 = "http://openapi.gbis.go.kr/ws/rest/busstationservice";
                strSrch2 = URLEncoder.encode(nextStationName, "UTF-8");
                url2 = new URL(serviceUrl2 + "?serviceKey=" + serviceKey + "&keyword=" + strSrch2);
                Log.d("#url2", url2.toString());
                new findNextStationIdTask().execute(url2.toString());
            } catch (Exception e) {
            }

        }

        private String downloadUrl(String myurl) throws IOException { //url에 대한 웹문서 다운로드

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

    private class findNextStationIdTask extends AsyncTask<String, Void, String> { //url에 대한 웹문서 다운로드

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String) downloadUrl((String) urls[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            boolean bSet1 = false, bSet2 = false;
            Log.e("#", "다음정류소 STATION_ID찾기");
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();

                factory.setNamespaceAware(true);
                xpp.setInput(new StringReader(result));

                int eventType = xpp.getEventType();
                boolean bSet = false;
                TagType tagType = TagType.None;

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_DOCUMENT) {
                    } else if (eventType == XmlPullParser.START_TAG) { // 시작 태그일 경우

                        String tag_name = xpp.getName(); //태그 이름을 추출

                        if (tag_name.equals("mobileNo")) tagType = TagType.MobileNo;
                        else if (tag_name.equals("stationId")) tagType = TagType.StationId;

                        //if(tagType != TagType.None) Log.d("#TAGTYPE", tagType.toString());

                    } else if (eventType == XmlPullParser.TEXT) { // text 인 경우에
                        String content = xpp.getText();

                        switch (tagType) {
                            case MobileNo:
                                Log.e("#MobileNo", content);
                                if (content.equals(" " + nextStationMobileNo)) {
                                    bSet = true;
                                    Log.d("#MobileNo", content);
                                }
                                break;
                            case StationId:
//                                Log.e("#StationId", content);
                                if (bSet == true) {
                                    nextStationId = Integer.parseInt(content);
                                    bSet = false;
                                    Log.d("#NextStationId", nextStationId + "");
                                }
                                break;
                            default:
                                break;
                        }
                        tagType = TagType.None;
                    } else if (eventType == XmlPullParser.END_TAG) { // 이벤트 타입이 끝 태그일 경우
                    }
                    eventType = xpp.next();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            updateText();

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Alert");
            DatabaseReference stationRef = rootRef.child(String.valueOf(nextStationId));
            DatabaseReference routeRef = stationRef.child(routeName);

            FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Long value = dataSnapshot.getValue(Long.class);
//                    Integer value = Integer.parseInt(dataSnapshot.getValue().toString());
                    Integer value;
                    Log.e("@@FIREBASE@@", "UPDATE");

                    if (dataSnapshot.child("Alert").child(String.valueOf(nextStationId)).child(routeName).getValue() == null)
                        value = 0;
                    else
                        value = Integer.parseInt(dataSnapshot.child("Alert").child(String.valueOf(nextStationId)).child(routeName).getValue().toString());
                    if (value != null) {
//                        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_NOTIFICATION);
//                        Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                        final Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        if( !txtDisplayPsgNum.getText().equals(value.toString()) ) {
//                            try {
//                                ringtone.wait((long)0);
//                            } catch (Exception e) {}
//                           ringtone.play();
                            vibrator.vibrate(1000);
                        }
                        txtDisplayPsgNum.setText(value + "");
                        Log.e("몇명?", value + "");
                        Activity activity = getActivity();
                        if (activity != null) {
                            if (value == 0) {
                                Log.e("알림", "다음에 없음.");
//                                ringtone.stop();
                                layoutDisplay.setBackground(Driver.this.getResources().getDrawable(R.drawable.greensignal));
                            } else {
                                Log.e("알림", "다음에 있음.");
                                layoutDisplay.setBackground(Driver.this.getResources().getDrawable(R.drawable.redsignal));

                            }
                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        private String downloadUrl(String myurl) throws IOException { //url에 대한 웹문서 다운로드

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


    public void updateText() {
//        txtStationOrder.setText(stationSeq+"");
//        txtStationId.setText(stationId+"");
        txtStationName.setText("현재 : " + stationName);
//        txt_info.setText("  ["+plateNo+"] 현재 "+ routeName +"번 버스 운행 중");
//        txtNextStationId.setText(nextStationId+"");
//        txtNextStationName.setText( nextStationName );
//        txtNextStationMobileNo.setText( nextStationMobileNo );
        txtDisplayStaName.setText("다음 : " + nextStationName);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (timer != null) timer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }


    @Override
    public void onResume() {
        super.onResume();

        routeName = accessDB.selectDriverRouteName();
        city = accessDB.selectDriverCity();
        startStationName = accessDB.selectStartStationByRouteCity(routeName, city);
        routeId = accessDB.selectRouteId(routeName, startStationName);
        plateNo = accessDB.selectDriverPlateNo();

        if (!(routeName.equals("") || plateNo.equals(""))) {
            txt_info.setText("  [" + plateNo + "] 현재 " + routeName + "번 버스 운행 중");
            startBackgroundPerform();
        } else {
            txt_info.setText("  버스 정보가 입력되지 않았습니다.");
        }
        new findBusLocationTask().execute(url1);
    }
}