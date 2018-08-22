package test.sks.com.busapp;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchRouteActivity extends AppCompatActivity {

    private EditText editTxt;
    private Button searchButton;
    private DBHelper accessDB = null;
    private RouteRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<RouteItem> routeItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_route);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("버스 검색");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("버스 검색");

        editTxt = (EditText) findViewById(R.id.editText);
        searchButton = (Button) findViewById(R.id.btn_search);

        accessDB = DBHelper.getInstance(getApplicationContext());
        accessDB.connect();
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recyclerView);  // 리사이클러뷰
        routeItems = new ArrayList<>();
        mAdapter = new RouteRecyclerAdapter(routeItems, 0, this);          // 리사이클러뷰 안의 리스트 형태
        mRecyclerView.setAdapter(mAdapter);
    }

    public void btnRouteSearch(View v) {
        if(routeItems != null) routeItems.clear();

        routeItems = accessDB.selectRouteName(editTxt.getText().toString());
        for(RouteItem item : routeItems) {
            item.setCity( accessDB.selectRouteCity(item.getRoute_nm(), item.getStation_nm()) );
        }

        mAdapter.updateItems(routeItems);
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
