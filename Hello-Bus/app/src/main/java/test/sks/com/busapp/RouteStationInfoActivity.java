package test.sks.com.busapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.squareup.otto.Subscribe;

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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class RouteStationInfoActivity extends AppCompatActivity {
    private ImageButton btnFavoriteStation;
    private int rcvRouteId, rcvStationId;
    private String rcvRouteName, rcvStationName, rcvMobileNo;
    private DBHelper accessDB;
    private PredictTimeRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private String serviceKey="1234567890", strSrch1, strSrch2;
    private String positionX, positionY;
    ArrayList<RouteItem> routeItems;
    ArrayList<RouteItem> alertSavedRouteItems;
    FavoriteRouteItem favoriteItem;
    private RouteItem routeItem;
    private String serviceUrl1, serviceUrl2;
    String url1, url2;
    private int cnt_run = 0;
    private Timer timer;
    private TimerTask timerTask;
    public Intent intent;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    LinearLayout expandedLayout;
    int oldOffset = 0;

    private TextView txtMobileNo, txtStationName;
    private enum TagType {
        None,
        RouteId,
        MobileNo,
        StationId,
        StationName,
        LocationNo1,
        LocationNo2,
        PredictTime1,
        PredictTime2,
        X,
        Y;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_station_info);

        rcvRouteId = (int) getIntent().getExtras().get("routeId");
        rcvRouteName = (String) getIntent().getExtras().get("routeName");
        rcvStationId = (int) getIntent().getExtras().get("stationId");
        rcvMobileNo = (String) getIntent().getExtras().get("mobileNo");
        rcvStationName = (String) getIntent().getExtras().get("stationName");
        setTitle(rcvMobileNo + " " + rcvStationName + "정류소");
        initInstancesDrawer();

        txtMobileNo = (TextView) findViewById(R.id.text_mobile_no);
        txtStationName = (TextView) findViewById(R.id.text_station_name);
        btnFavoriteStation = (ImageButton) findViewById(R.id.btn_favorite_station);

        txtMobileNo.setText(rcvMobileNo);
        txtStationName.setText(rcvStationName);

        accessDB = DBHelper.getInstance(getApplicationContext());
        accessDB.connect();

        routeItems = new ArrayList<>();
        routeItems.add(new RouteItem(rcvRouteId, rcvRouteName));
        routeItems.get(0).setMobile_no(rcvMobileNo);
        routeItems.get(0).setStation_nm(rcvStationName);
        alertSavedRouteItems = routeItems;

        serviceKey = "1234567890"; // 오픈 api 인증키

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recyclerView);
        mAdapter = new PredictTimeRecyclerAdapter(routeItems, 0, RouteStationInfoActivity.this);

        favoriteItem = accessDB.selectFavoriteStation(rcvStationName, rcvMobileNo);
        if(favoriteItem == null)
            btnFavoriteStation.setImageResource(R.drawable.star_blank2);
        else
            btnFavoriteStation.setImageResource(R.drawable.star2);

        BusProvider.getInstance().register(this);

        for(RouteItem item : routeItems) {
            item.setStation_id(rcvStationId);
            item.setStation_nm(rcvStationName);
            item.setMobile_no(rcvMobileNo);
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Alert");
        DatabaseReference stationRef = rootRef.child( String.valueOf(rcvStationId) );

        for(RouteItem item : routeItems) {
            DatabaseReference routeRef = stationRef.child(item.getRoute_nm());
            routeRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Integer value = mutableData.getValue(Integer.class);

                    if( value == null ) {
                        Log.e("@@FIREBASE", " ROUTE SET");
                        value = 0;
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

        routeItem = new RouteItem();

        url1 = "http://openapi.gbis.go.kr/ws/rest/busstationservice?serviceKey="+serviceKey+"&keyword="+rcvMobileNo;

        serviceUrl2 = "http://openapi.gbis.go.kr/ws/rest/busarrivalservice";
        url2 = serviceUrl2+"?serviceKey="+serviceKey+"&routeId="+rcvRouteId+"&stationId="+rcvStationId;

        new findPositionTask().execute(url1);

        startBackgroundPerform();
    }

    private void initInstancesDrawer() {

        txtMobileNo = (TextView) findViewById(R.id.text_mobile_no);
        txtStationName = (TextView) findViewById(R.id.text_station_name);
        expandedLayout = (LinearLayout) findViewById(R.id.expanded_layout);

        txtMobileNo.setText(rcvMobileNo);
        txtStationName.setText(rcvStationName);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbarLayout.setTitle("");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.colorPrimaryText));
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {

                if ((offset) >= -150) {
                    expandedLayout.setVisibility(View.VISIBLE);
                    collapsingToolbarLayout.setTitle("");

                } else if ((offset) < -150 && (offset) >= -240) {
                    expandedLayout.setVisibility(View.INVISIBLE);
                    collapsingToolbarLayout.setTitle("");
                } else {
                    collapsingToolbarLayout.setTitle(rcvStationName);
                    collapsingToolbarLayout.setContentDescription(rcvStationName +" 정류소");
                }

                oldOffset = offset;
            }
        });

    }
    public void btnGoHome(View v) {
        Intent intent = new Intent(this, ModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public void btnAddMap(View v) {

        Intent intent = new Intent(this, WhereIsStation.class);

        intent.putExtra("positionX", positionX);
        intent.putExtra("positionY", positionY);
        intent.putExtra("rcvMobileNo", rcvMobileNo);
        intent.putExtra("rcvStationName", rcvStationName);
        this.startActivity(intent);
    }
    public void btnAddFavoriteStation(View v) {
        favoriteItem = accessDB.selectFavoriteStation(rcvStationName, rcvMobileNo);
        if(favoriteItem != null) {
            BusProvider.getInstance().post(favoriteItem);
            btnFavoriteStation.setImageResource(R.drawable.star_blank2);
            Toast.makeText(getApplicationContext(), rcvStationName + "정류소 즐겨찾기를 해제 하였습니다.", Toast.LENGTH_LONG).show();
        }
        else{
            accessDB.insertFavoriteStation(rcvStationId, rcvStationName, rcvMobileNo);
            btnFavoriteStation.setImageResource(R.drawable.star2);
            Toast.makeText(getApplicationContext(), rcvStationName + "정류소을 즐겨찾기 하였습니다.", Toast.LENGTH_LONG).show();
        }
    }
    public void stopBackgroundPerform() {
        if(timer != null) timer.cancel();
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
                            Log.d("RouteStationActivity", "run" + cnt_run);

                                findPredictTimeTask performBackgroundTask2 = new findPredictTimeTask(getApplicationContext());
                                performBackgroundTask2.execute(url2.toString());
                                Log.d("#url2", url2.toString());


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 10000);
    }



    private class findPredictTimeTask extends AsyncTask<String, Void, String> { //url에 대한 웹문서 다운로드
        private Context context;
        public findPredictTimeTask() {

        }
        public findPredictTimeTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String)downloadUrl((String)urls[0]);
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
                boolean bContent = false;

                TagType tagType = TagType.None;
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if(eventType == XmlPullParser.START_DOCUMENT) {
                    } else if(eventType == XmlPullParser.START_TAG) {
                        bContent = false;
                        String tag_name = xpp.getName();

                        // 정류소 ID검색에 필요
                        if(tag_name.equals("locationNo1"))          tagType = TagType.LocationNo1;
                        else if(tag_name.equals("locationNo2"))     tagType = TagType.LocationNo2;
                        else if(tag_name.equals("predictTime1"))    tagType = TagType.PredictTime1;
                        else if(tag_name.equals("predictTime2"))    tagType = TagType.PredictTime2;
                        else if(tag_name.equals("routeId"))         tagType = TagType.RouteId;

                        else if(tag_name.equals("mobileNo"))        tagType = TagType.MobileNo;
                        else if(tag_name.equals("stationId"))       tagType = TagType.StationId;

                    } else if(eventType == XmlPullParser.TEXT) {
                        bContent = true;
                        String content = xpp.getText();

                        switch (tagType) {
                            case LocationNo1:
                                routeItem.setLocationNo1(content); break;
                            case LocationNo2:
                                routeItem.setLocationNo2(content); break;
                            case PredictTime1:
                                routeItem.setPredictTime1(content); break;
                            case PredictTime2:
                                routeItem.setPredictTime2(content); break;
                            case RouteId:
                                int route_id = Integer.parseInt(content);
                                int idx = getIndexByRouteId( route_id );
                                routeItem.setRoute_id(route_id);
                                routeItems.get(idx).updateArrivalInfo(routeItem);
                                break;
                            default: break;
                        }

                    } else if(eventType == XmlPullParser.END_TAG) {
                        if( bContent == false ) {
                            switch (tagType) {
                                case LocationNo2:
                                    routeItem.setLocationNo2(null);
                                    break;
                                case PredictTime2:
                                    routeItem.setPredictTime2(null);
                                    break;
                                default: break;
                            }
                        }
                        tagType = TagType.None;
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            int cnt = 0;
            for(RouteItem item : routeItems) {
                int routeId = accessDB.selectRouteId(item.getRoute_nm(), rcvStationName);
                String routeName = item.getRoute_nm();
                String startStationName = accessDB.selectStartStationName( routeId );
                String city = accessDB.selectRouteCity(routeName, startStationName);

                item.setRoute_id( routeId );
                item.setCity( city );
                item.setAlert(alertSavedRouteItems.get(cnt).isAlert());
                cnt++;
            }

            mRecyclerView.setAdapter(mAdapter);

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
                while((line = bufreader.readLine()) != null) {
                    page += line;
                }
                return page;
            } finally {
                conn.disconnect();
            }
        }

        private int getIndexByRouteId(int routeId) {
            int cnt = 0;
            for(RouteItem item : routeItems) {
                if (item.getRoute_id() == routeId) return cnt;
                cnt++;
            }
            System.err.println("routeID에 해당하는 아이템 못찾음");
            return -1;
        }
    }

    private class findPositionTask extends AsyncTask<String, Void, String> { //url에 대한 웹문서 다운로드

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String)downloadUrl((String)urls[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        @Override
        protected void onPostExecute(String result) { //다운로드 결과를 실행
            boolean bSet = false;

            Log.i("#", "요청");
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); // XML Pull Parser를 만들기 위한 XmlPullParserFactory의 인스턴스 생성
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();

                TagType tagType = TagType.None;

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if(eventType == XmlPullParser.START_DOCUMENT) {
                    } else if(eventType == XmlPullParser.START_TAG) {

                        String tag_name = xpp.getName();
                        if( tag_name.equals("stationName") )       tagType = TagType.StationName;
                        else if(tag_name.equals("x"))               tagType = TagType.X;
                        else if(tag_name.equals("y"))               tagType = TagType.Y;

                        //if(tagType != TagType.None) Log.d("#TAGTYPE", tagType.toString());

                    } else if(eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();
                        Log.e("parsing content",content);
                        switch (tagType) {
                            case StationName:
                                if(content.equals(rcvStationName))
                                    bSet = true;
                                break;
                            case X:
                                if( bSet == true ) {
                                    positionX = content;
                                    Log.e("#positionX", positionX);
                                }
                                break;
                            case Y:
                                if( bSet == true ) {
                                    positionY = content;
                                    Log.e("#positionY", positionY);
                                    bSet = false;
                                }
                                break;
                            default:
                                break;
                        }
                        tagType = TagType.None;
                    } else if(eventType == XmlPullParser.END_TAG) { // 이벤트 타입이 끝 태그일 경우
                    }
                    eventType = xpp.next(); //parser를 다음 이벤트 타입으로 옮긴 후, 이벤트 타입 반환.

                }
            } catch (Exception e) {
                e.printStackTrace();
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
                while((line = bufreader.readLine()) != null) {
                    page += line;
                }
                return page;
            } finally {
                conn.disconnect();
            }
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        startBackgroundPerform();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("RouteStationInfo", "Destroy");
        //if(BusProvider.getInstance() != null) BusProvider.getInstance().unregister(this);
        stopBackgroundPerform();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("RouteStationInfo", "BACK Pressed");
        //stopBackgroundPerform();
        finish();
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//
//        if(timer!=null) timer.cancel();
//
//        finish();
//    }

    @Subscribe
    public void FinishLoad(Object mPushEvent) { //PushEvent

        if(mPushEvent instanceof AlertItem && ((AlertItem)mPushEvent).getActivityName().equals("RouteStationInfoActivity")) {
            /** Service 실행 **/
            Log.e("BUS PROVIDER", "ALERT SERVICE 시작");
//            Toast.makeText(getApplicationContext(),"Service 시작",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RouteStationInfoActivity.this, AlertService.class);
            startService(intent);
            mAdapter.setAlertStatus(AlertStatus.ALARM);
            Log.e("RouteStation", "ALERT BUTTON CLICK ALARM");
            mAdapter.getmHolder().mBtnAlert.performClick();
        } else if(mPushEvent instanceof AlertStatus && accessDB.selectAlert().getActivityName().equals("RouteStationInfoActivity")) {
            if (mPushEvent == AlertStatus.CANCEL) {
                mAdapter.setAlertStatus(AlertStatus.CANCEL);
                Log.e("RouteStation", "ALERT BUTTON CLICK CANCEL");
                mAdapter.getmHolder().mBtnAlert.performClick();
            }
        } else if(mPushEvent instanceof ArrivalAlarmItem && ((ArrivalAlarmItem)mPushEvent).getActivityName().equals("RouteStationInfoActivity")) {
            Log.e("BUS RouteStation", "ARRIVAL ALARM SERVICE 시작");
//            Toast.makeText(getApplicationContext(),"ARRIVAL ALARM SERVICE 시작",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RouteStationInfoActivity.this, AlertService.class);

            startService(intent);

        } else if(mPushEvent instanceof ArrivalAlarmStatus && accessDB.selectArrivalAlarm().getActivityName().equals("RouteStationInfoActivity")) {
            if (mPushEvent == ArrivalAlarmStatus.CANCEL) {
                mAdapter.setArrivalAlarmStatus(ArrivalAlarmStatus.CANCEL);
                Log.e("StationInfo", "ARRIVAL ALARM BUTTON CLICK CANCEL");
                mAdapter.getmHolder().mBtnArrivalAlarm.performClick();
            }
        }

    }

}
