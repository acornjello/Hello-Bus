package test.sks.com.busapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class RouteInfoActivity extends AppCompatActivity {
    private DBHelper accessDB;
    private StationRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private TextView txtRouteCity, txtRouteName;
    private ImageButton btnFavoriteRoute;
    private ArrayList<StationItem> stationItems;
    FavoriteRouteItem favoriteItem;
    private String startStationName, endStationName, routeCity;
    private int rcvRouteId, endStationSeq;
    private String rcvRouteName, rcvActivity, plateNo = "", stationName = "";
    private String serviceKey="1234567890", url1, serviceUrl1, strSrch1;
    private Parcelable recyclerViewState;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    LinearLayout expandedLayout;

    Timer timer;
    TimerTask timerTask;
    int oldOffset = 0;

    private enum TagType {
        None,
        StationSeq,
        busArrivalList,
        PlateNo,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_info);
        accessDB = DBHelper.getInstance(getApplicationContext());
        accessDB.connect();


        rcvRouteId = (int) getIntent().getExtras().get("routeID");
        rcvRouteName = (String) getIntent().getExtras().get("routeName");
        rcvActivity = (String) getIntent().getExtras().get("activity");

        setTitle(rcvRouteName+"번 버스 정보");

        stationItems = accessDB.selectRouteInfoByRouteID(rcvRouteId);
        startStationName = accessDB.selectStartStationName(rcvRouteId);
        endStationName = accessDB.selectEndStationName(rcvRouteName, startStationName);
        endStationSeq = accessDB.selectEndStationSeq(rcvRouteName, endStationName);
        routeCity = accessDB.selectRouteCity(rcvRouteName, startStationName);

        initInstancesDrawer();

        txtRouteCity = (TextView) findViewById(R.id.text_route_city);
        txtRouteName = (TextView) findViewById(R.id.text_route_name);
        btnFavoriteRoute = (ImageButton) findViewById(R.id.btn_favorite);
        txtRouteCity.setText(routeCity);
        txtRouteName.setText(rcvRouteName);

        favoriteItem = accessDB.selectFavoriteRoute(rcvRouteId, rcvRouteName);
        if(favoriteItem == null) btnFavoriteRoute.setImageResource(R.drawable.star_blank2);
        else btnFavoriteRoute.setImageResource(R.drawable.star2);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recyclerView);
        mAdapter = new StationRecyclerAdapter(stationItems, 0, this);

        mRecyclerView.setLayoutManager(layoutManager);

        serviceUrl1 = "http://openapi.gbis.go.kr/ws/rest/buslocationservice";
        strSrch1 = rcvRouteId+"";
        url1 = serviceUrl1+"?serviceKey="+serviceKey+"&routeId="+strSrch1;
        Log.d("#url1", url1.toString());
        startBackgroundPerform();

        mRecyclerView.setNestedScrollingEnabled(true);
    }
    private void initInstancesDrawer() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        expandedLayout = (LinearLayout) findViewById(R.id.expanded_layout);

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
                    collapsingToolbarLayout.setTitle(rcvRouteName+"번");
                    collapsingToolbarLayout.setContentDescription(rcvRouteName+"번");
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
    public void btnAddFavoriteRoute(View v) {
        favoriteItem = accessDB.selectFavoriteRoute(rcvRouteId, rcvRouteName);

        if(favoriteItem != null) {
            if(rcvActivity.equals("FavoriteInfoActivity"))
                BusProvider.getInstance().post(favoriteItem);
            else
                accessDB.deleteFavorite2(rcvRouteId, rcvRouteName, routeCity, null, null);
            btnFavoriteRoute.setImageResource(R.drawable.star_blank2);
            Toast.makeText(getApplicationContext(), rcvRouteName + "번 버스 즐겨찾기를 해제 하였습니다.", Toast.LENGTH_LONG).show();
        }
        else{
            accessDB.insertFavoriteRoute(rcvRouteId, rcvRouteName, routeCity);
            btnFavoriteRoute.setImageResource(R.drawable.star2);
            Toast.makeText(getApplicationContext(), rcvRouteName+"번을 즐겨찾기 하였습니다.", Toast.LENGTH_LONG).show();
        }

    }

    public void btnMoveToUp(View view) {

//        mRecyclerView.smoothScrollToPosition(0);
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(0);
                Log.e("RouteInfo", "MOVE UP");
            }
        });


    }

    public void btnMoveToDown(View view) {
//        layoutManager.scrollToPositionWithOffset(endStationSeq-1, 0);   // 회차

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                layoutManager.scrollToPositionWithOffset(endStationSeq-1, 0);
                Log.e("RouteInfo", "MOVE DOWN");
            }
        });

    }

    public void btnRouteMore(View view) {
        Intent routeDetailIntent = new Intent(this, RouteMoreActivity.class);

        routeDetailIntent.putExtra("routeName", rcvRouteName);
        routeDetailIntent.putExtra("startStationName", startStationName);

        startActivity(routeDetailIntent);
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
                            recyclerViewState = layoutManager.onSaveInstanceState();
                            for(StationItem item : stationItems) {
                                item.setExist(false);
                            }
                            new findBusLocationTask().execute(url1.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 10000);
    }

    private class findBusLocationTask extends AsyncTask<String, Void, String> { //url에 대한 웹문서 다운로드

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String)downloadUrl((String)urls[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("#", "버스위치찾기");
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();

                TagType tagType = TagType.None;

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if(eventType == XmlPullParser.START_DOCUMENT) {
                    } else if(eventType == XmlPullParser.START_TAG) { // 시작 태그일 경우

                        String tag_name = xpp.getName();

                        if(tag_name.equals("stationSeq"))      tagType = TagType.StationSeq;
                        else if(tag_name.equals("plateNo"))         tagType = TagType.PlateNo;
                        else if(tag_name.equals("busArrivalList"))  tagType = TagType.busArrivalList;

                    } else if(eventType == XmlPullParser.TEXT) {

                        String content = xpp.getText();

                        switch (tagType) {
                            case StationSeq:
                                stationItems.get( Integer.parseInt(content)-1 ).setExist(true);
                                break;
                            default: break;
                        }
                        tagType = TagType.None;
                    } else if(eventType == XmlPullParser.END_TAG) {
                    }
                    eventType = xpp.next();

                }


            } catch (Exception e) {
                e.printStackTrace();
            }

//            mAdapter.updateItems(stationItems);
            mRecyclerView.setAdapter(mAdapter);
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
    }

    public int getRcvRouteId() {
        return rcvRouteId;
    }
    public String getRcvRouteName() {
        return rcvRouteName;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("RouteInfo", "Stop");
        stopBackgroundPerform();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("RouteInfo", "Destroy");
        stopBackgroundPerform();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.e("RouteInfo", "Back Pressed");
        //stopBackgroundPerform();
        finish();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("RouteInfo", "Restart");
        startBackgroundPerform();
    }
}


