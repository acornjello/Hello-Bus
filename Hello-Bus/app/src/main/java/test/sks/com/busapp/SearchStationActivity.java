package test.sks.com.busapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class SearchStationActivity extends AppCompatActivity {

    private EditText editTxt;
    private DBHelper accessDB = null;
    private StationRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<StationItem> stationItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_station);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("정류소 검색");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("정류소 검색");
        editTxt = (EditText) findViewById(R.id.editText);

        accessDB = DBHelper.getInstance(getApplicationContext());
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recyclerView);  // 리사이클러뷰

    }

    public void btnStationSearch(View v) {

        if(stationItems != null) stationItems.clear();
        stationItems = accessDB.selectStation(editTxt.getText().toString());

        mAdapter = new StationRecyclerAdapter(stationItems, 0, this);          // 리사이클러뷰 안의 리스트 형태
        mRecyclerView.setAdapter(mAdapter);

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
