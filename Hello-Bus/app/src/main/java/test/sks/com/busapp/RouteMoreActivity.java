package test.sks.com.busapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class RouteMoreActivity extends AppCompatActivity {
    private TextView txtCity, txtRouteName, txtStartStation, txtEndStation, txtWeekdaysInterval, txtWeekendInterval, txtUpTime, txtDownTime;
    private String rcvRouteName, rcvStartStationName;
    private RouteMoreItem routeMoreItem;
    private DBHelper accessDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_more);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("버스 운행 정보");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("버스 운행 정보");
//        txtRouteName = (TextView) findViewById(R.id.txt_route_name);
        txtCity = (TextView) findViewById(R.id.txt_route_city);
        txtUpTime = (TextView) findViewById(R.id.txt_up_time);
        txtDownTime = (TextView) findViewById(R.id.txt_down_time);
        txtWeekdaysInterval = (TextView) findViewById(R.id.txt_weekdays_interval);
        txtWeekendInterval = (TextView) findViewById(R.id.txt_weekend_interval);

        rcvRouteName = (String) getIntent().getExtras().get("routeName");
        rcvStartStationName = (String) getIntent().getExtras().get("startStationName");

        accessDB = DBHelper.getInstance(getApplicationContext());
        accessDB.connect();

        routeMoreItem = accessDB.selectRouteMore(rcvRouteName, rcvStartStationName);

        txtCity.setText(routeMoreItem.getCity());
        txtUpTime.setText( routeMoreItem.getUp_start_time() + " ~ " + routeMoreItem.getUp_end_time());
        txtUpTime.setText( routeMoreItem.getUp_start_time() + "시작. " + routeMoreItem.getUp_end_time() + " 종료.");
        txtDownTime.setText( routeMoreItem.getDown_start_time() + " ~ " + routeMoreItem.getDown_end_time());
        txtUpTime.setText( routeMoreItem.getDown_start_time() + "시작. " + routeMoreItem.getDown_end_time() + " 종료.");
        txtWeekdaysInterval.setText(routeMoreItem.getWeekdays_interval());
        txtWeekdaysInterval.setContentDescription(routeMoreItem.getWeekdays_interval().replaceAll("~", "에서"));
        txtWeekendInterval.setText(routeMoreItem.getWeekend_interval());
        txtWeekdaysInterval.setContentDescription(routeMoreItem.getWeekend_interval().replaceAll("~", "에서"));

    }

    public void btnGoHome(View v) {
        Intent intent = new Intent(this, ModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
