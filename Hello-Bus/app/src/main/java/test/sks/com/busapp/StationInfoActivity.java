package test.sks.com.busapp;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
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

public class StationInfoActivity extends AppCompatActivity {
    private DBHelper accessDB;
    private LinearLayoutManager layoutManager;
    private PredictTimeRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView txtMobileNo, txtStationName;
    private ImageButton btnFavoriteStation, btnMap;
    private String rcvMobileNo, rcvStationName;
    private String stationId ="", serviceKey="1234567890", strSrch1, strSrch2, serviceUrl1, serviceUrl2, url2;
    private URL url1;
    private ArrayList<RouteItem> routeItems, alertSavedRouteItems;
    private RouteItem routeItem;
    private FavoriteRouteItem favoriteItem;
    private int cnt_run = 0;
    private String positionX, positionY;
    private Timer timer;
    private TimerTask timerTask;
    private Parcelable recyclerViewState;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    LinearLayout expandedLayout;
    int oldOffset = 0;
    private enum TagType {
        None,
        MobileNo,
        RouteId,
        StationId,
        StationName,
        X,
        Y,
        LocationNo1,
        LocationNo2,
        PredictTime1,
        PredictTime2,

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
        setContentView(R.layout.activity_station_info);


        rcvMobileNo = (String) getIntent().getExtras().get("mobileNo");
        rcvStationName = (String) getIntent().getExtras().get("stationName");
        setTitle(rcvMobileNo + " " + rcvStationName + "정류소");
        //Toast.makeText(getApplicationContext(), rcvMobileNo + " " + rcvStationName + "정류소", Toast.LENGTH_SHORT).show();
        initInstancesDrawer();

        btnFavoriteStation = (ImageButton) findViewById(R.id.btn_favorite_station);
        btnMap = (ImageButton) findViewById(R.id.btn_map);

        accessDB = DBHelper.getInstance(getApplicationContext());
        accessDB.connect();
        routeItems = new ArrayList<>();
        rcvMobileNo = rcvMobileNo.replaceAll(" ", "");
        routeItems = accessDB.selectStationInfobyMobileNo(rcvMobileNo);

        Log.e("Log","stationinfo");
        Log.e("rcvMobile",rcvMobileNo);
        Log.e("Log",accessDB.selectStationInfobyMobileNo(rcvMobileNo)+"");
        Log.e("size",routeItems.size()+"");
        for(RouteItem item : routeItems) {
            Log.e("Log","db start");
            int routeId = accessDB.selectRouteId(item.getRoute_nm(), rcvStationName);
            Log.e("start routeId",routeId+"");
            String routeName = item.getRoute_nm();
            String startStationName = accessDB.selectStartStationName(routeId);
            String city = accessDB.selectRouteCity(routeName, startStationName);

            item.setRoute_id(routeId);
            Log.e("item-routeId",item.getRoute_id()+"");
            item.setCity(city);
            item.setStation_nm(rcvStationName);
            item.setMobile_no(rcvMobileNo);

        }
        alertSavedRouteItems = routeItems;
        //accessDB.selectAllFavorite();
        favoriteItem = accessDB.selectFavoriteStation(rcvStationName, rcvMobileNo);
        if (favoriteItem == null) {
            btnFavoriteStation.setImageResource(R.drawable.star_blank2);
        }
        else {
            btnFavoriteStation.setImageResource(R.drawable.star2);
        }


        //==============================================================================

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PredictTimeRecyclerAdapter(routeItems, 0, StationInfoActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        startBackgroundPerform();

        BusProvider.getInstance().register(this);
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
                Log.e("StaitonInfo", offset+"");
                if ((offset) >= -180) {
                    expandedLayout.setVisibility(View.VISIBLE);
                    collapsingToolbarLayout.setTitle("");

                } else if ((offset) < -180 && (offset) >= -240) {
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

    public void btnAddFavoriteStation(View v) {
        favoriteItem = accessDB.selectFavoriteStation(rcvStationName, rcvMobileNo);
        if(favoriteItem != null) {
            BusProvider.getInstance().post(favoriteItem);
            //accessDB.deleteFavorite2(-1, null, null, rcvStationName, rcvMobileNo);  // 앞에 세 인자는 의미없음
            btnFavoriteStation.setImageResource(R.drawable.star_blank2);
            Toast.makeText(getApplicationContext(), rcvStationName + "정류소 즐겨찾기를 해제 하였습니다.", Toast.LENGTH_LONG).show();
        }
        else{
            accessDB.insertFavoriteStation(Integer.parseInt(stationId), rcvStationName, rcvMobileNo);
            btnFavoriteStation.setImageResource(R.drawable.star2);
            Toast.makeText(getApplicationContext(), rcvStationName + "정류소을 즐겨찾기 하였습니다.", Toast.LENGTH_LONG).show();
        }
    }
    public void btnAddMap(View v) {

        Intent intent = new Intent(this, WhereIsStation.class);
        intent.putExtra("positionX", positionX);
        intent.putExtra("positionY", positionY);
        intent.putExtra("rcvMobileNo", rcvMobileNo);
        intent.putExtra("rcvStationName", rcvStationName);
        this.startActivity(intent);
    }
    public void btnGoHome(View v) {
        Intent intent = new Intent(this, ModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

                            if(cnt_run++ != 0) {
                                Log.e("StationInfo", "PredictTime run" + cnt_run + " " + url2.toString());
                                recyclerViewState = layoutManager.onSaveInstanceState();
                                findPredictTimeTask performBackgroundTask2 = new findPredictTimeTask(getApplicationContext());
                                performBackgroundTask2.execute(url2.toString());
                            }
                            else {

                                serviceUrl1 = "http://openapi.gbis.go.kr/ws/rest/busstationservice";
                                strSrch1 = URLEncoder.encode(rcvStationName, "UTF-8");
                                url1 = new URL(serviceUrl1+"?serviceKey="+serviceKey+"&keyword="+strSrch1);
                                Log.e("StationInfo", "StationId run" + cnt_run + " " + url1.toString());
                                findStationIdTask  performBackgroundTask1 = new findStationIdTask();
                                performBackgroundTask1.execute(url1.toString());

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 6000);
    }

    private class findStationIdTask extends AsyncTask<String, Void, String> { //url에 대한 웹문서 다운로드

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
            boolean bSet = false, bBreak = false;
            boolean bSetX = false, bSetY = false;

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

                        if(tag_name.equals("mobileNo"))             tagType = TagType.MobileNo;
                        else if(tag_name.equals("stationId"))       tagType = TagType.StationId;
                        else if(tag_name.equals("x"))               tagType = TagType.X;
                        else if(tag_name.equals("y"))               tagType = TagType.Y;

                        if(tagType != TagType.None) Log.d("#TAGTYPE", tagType.toString());

                    } else if(eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();
                        Log.e("parsing content",content);
                        switch (tagType) {
                            case MobileNo:
//                                Log.e("content is","mobile");
//                                Log.e("rcvMobile",rcvMobileNo);

                                if( content.equals(String.valueOf(" " + rcvMobileNo))) {
                                    bSet = true;
                                    bSetX = true;
                                    bSetY = true;
                                    Log.e("#MobileNo", content);
                                }
                                break;
                            case StationId:
                                if( bSet == true ) {
                                    stationId = content;
                                    bSet = false;
                                    Log.d("#StationId", stationId.toString());
                                }
                                break;
                            case X:
                                if( bSetX == true ) {
                                    positionX = content;

                                    bSetX = false;
                                    Log.e("#positionX", positionX);
                                }
                                break;
                            case Y:
                                if( bSetY == true ) {
                                    positionY = content;

                                    bSetY = false;
                                    Log.e("#positionY", positionY);
                                }
                                break;
                            default:
                                Log.e("switch","default");
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

            for(RouteItem item : routeItems) {
                if(!stationId.equals("")) {
                    item.setStation_id(Integer.parseInt(stationId));
                }
            }

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Alert");
            DatabaseReference stationRef = rootRef.child(stationId);

            for(RouteItem item : routeItems) {
                DatabaseReference routeRef = stationRef.child(item.getRoute_nm());
                routeRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer value = mutableData.getValue(Integer.class);

                        if( value == null ) {
//                            Log.e("@@FIREBASE", " ROUTE SET");
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
            Log.e("Log-sationid",stationId+" ");
            serviceUrl2 = "http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station"; //공공 db의 정류소 정보 조회서비스 요청 주소
            strSrch2 = stationId;
            url2 = serviceUrl2+"?serviceKey="+serviceKey+"&stationId="+strSrch2;

            Log.d("#url2", url2);
            findPredictTimeTask task = new findPredictTimeTask();
            task.execute(url2);
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
            Log.e("#", "요청2");
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); // XML Pull Parser를 만들기 위한 XmlPullParserFactory의 인스턴스 생성
                factory.setNamespaceAware(true);                // factory에 의해 생성될 parser를 만들 때, XML namespace 지원여부를 설정
                XmlPullParser xpp = factory.newPullParser();    // XML Pull Parser의 인스턴스 생성.

                xpp.setInput(new StringReader(result));         // 데이터 리소스에 대한 input stream 설정
                int eventType = xpp.getEventType();             // parser의 현재 이벤트 타입을 반환. (start tag 나 end tag나 text 등)
                boolean bContent = false;

                TagType tagType = TagType.None;
                while (eventType != XmlPullParser.END_DOCUMENT) { // 현재 이벤트 타입이 end_document를 만날때까지 처리 반복.

                    if(eventType == XmlPullParser.START_DOCUMENT) {
                    } else if(eventType == XmlPullParser.START_TAG) {   // 시작 태그일 경우
                        bContent = false;
                        String tag_name = xpp.getName();                // 태그 이름을 추출

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
                        String content;
//                        String content = xpp.getText();
//                        Log.e("content",content);
                        switch (tagType) {
                            case LocationNo1:
                                content = xpp.getText();
                                routeItem.setLocationNo1(content); break;
                            case LocationNo2:
                                content = xpp.getText();
                                routeItem.setLocationNo2(content); break;
                            case PredictTime1:
                                content = xpp.getText();
                                routeItem.setPredictTime1(content); break;
                            case PredictTime2:
                                content = xpp.getText();
                                routeItem.setPredictTime2(content); break;
                            case RouteId:
                                content = xpp.getText();
                                int route_id = Integer.parseInt(content);
//                                Log.e("route_id",route_id+" ");
                                int idx = getIndexByRouteId( route_id );
//                                Log.e("idx",idx+"");
                                routeItem.setRoute_id(route_id);
                                if(idx>=0) routeItems.get(idx).updateArrivalInfo(routeItem);
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
                item.setAlert(alertSavedRouteItems.get(cnt).isAlert());
                cnt++;
            }
//            mRecyclerView.setAdapter(mAdapter);
            mAdapter.updateItems(routeItems);

            mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
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
                //Log.e("Route_id() vs routeId", item.getRoute_id() +" " + routeId+" ");
                if (item.getRoute_id() == routeId) return cnt;
                cnt++;
            }
            System.err.println("routeID에 해당하는 아이템 못찾음");
            return -1;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mAdapter.updateItems2();
        startBackgroundPerform();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("StationInfo", "Destroy");
        if(BusProvider.getInstance() != null) BusProvider.getInstance().unregister(this);
        stopBackgroundPerform();
        setTitle("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("StationInfo", "BACK Pressed");
        //stopBackgroundPerform();
        finish();
    }

    // 이벤트가 발생한뒤 수행할 작업
    @Subscribe
    public void FinishLoad(Object mPushEvent) { //PushEvent

        if(mPushEvent instanceof AlertItem && ((AlertItem)mPushEvent).getActivityName().equals("StationInfoActivity")) {
            /** Service 실행 **/
            Log.e("BUS PROVIDER", "ALERT SERVICE 시작");
//            Toast.makeText(getApplicationContext(),"Service 시작",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(StationInfoActivity.this, AlertService.class);
            startService(intent);
            mAdapter.setAlertStatus(AlertStatus.ALARM);
            Log.e("StationInfo", "ALERT BUTTON CLICK ALARM");
            mAdapter.getmHolder().mBtnAlert.performClick();
        } else if(mPushEvent instanceof AlertStatus) {
            if (mPushEvent == AlertStatus.CANCEL) {
                mAdapter.setAlertStatus(AlertStatus.CANCEL);
                Log.e("StationInfo", "ALERT BUTTON CLICK CANCEL");
                mAdapter.getmHolder().mBtnAlert.performClick();
            }
        } else if(mPushEvent instanceof ArrivalAlarmItem && ((ArrivalAlarmItem)mPushEvent).getActivityName().equals("StationInfoActivity")) {
            Log.e("BUS PROVIDER", "ARRIVAL ALARM SERVICE 시작");
//            Toast.makeText(getApplicationContext(),"ARRIVAL ALARM SERVICE 시작",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(StationInfoActivity.this, AlertService.class);

            startService(intent);

        } else if(mPushEvent instanceof ArrivalAlarmStatus) {
            if (mPushEvent == ArrivalAlarmStatus.CANCEL) {
                mAdapter.setArrivalAlarmStatus(ArrivalAlarmStatus.CANCEL);
                Log.e("StationInfo", "ARRIVAL ALARM BUTTON CLICK CANCEL");
                mAdapter.getmHolder().mBtnArrivalAlarm.performClick();
            }
        }

    }



}

