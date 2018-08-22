package test.sks.com.busapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;


public class ModeActivity extends AppCompatActivity {
    Fragment fr;
    private View header;
    private DBHelper accessDB;
    private Switch sw;
    FragmentManager fm;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_mode);

        accessDB = DBHelper.getInstance(getApplicationContext());
        accessDB.connect();

        header = getLayoutInflater().inflate(R.layout.switch_button, null, false);
        sw = (Switch) header.findViewById(R.id.sw1);
        if ( accessDB.selectUserType().equals("승객") ) {
            Log.e("승객","on");
            sw.setChecked(true);
            setTitle("경기버스 (승객 메뉴)");
            fr = new Passenger();

        } else if ( accessDB.selectUserType().equals("운전자") ) {
            Log.e("운전자","off");
            sw.setChecked(false);
            setTitle("경기버스 (운전자 메뉴)");

            fr = new Driver();
        }
        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_place, fr);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        item.setActionView(header);

        Switch actionView = (Switch) item.getActionView();

        actionView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Start or stop your Service
                if(isChecked) {
                    accessDB.updateUserType("승객");
                    setTitle("경기버스(승객모드)");
                    fr = new Passenger();
                }
                else {
                    accessDB.updateUserType("운전자");
                    setTitle("경기버스(운전자 모드)");
                    fr = new Driver();
                }
                fm = getFragmentManager();
                fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                Log.e("교체","교체");
                fragmentTransaction.commit();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void searchRoute(View v) {
        Intent detailIntent = new Intent(this, SearchRouteActivity.class);

        this.startActivity(detailIntent);
    }

    public void searchStation(View v) {
        Intent detailIntent = new Intent(this, SearchStationActivity.class);

        this.startActivity(detailIntent);
    }

    public void PeripheralStation(View v) {
        Intent detailIntent = new Intent(this, PeripheralStation.class);
        //        Intent detailIntent = new Intent(this, PeripheralStationTab.class);

        this.startActivity(detailIntent);
    }

    public void viewFavoriteList(View v) {
        Intent detailIntent = new Intent(this, FavoriteInfoActivity.class);

        this.startActivity(detailIntent);
    }

    public void btnGoAlertActivity(View view) {
        Intent detailIntent = new Intent(this, AlertInfoActivity.class);
        this.startActivity(detailIntent);
    }

    public void btnUpdateDriverInfo(View view) {
        Intent detailIntent = new Intent(this, DriverInfoActivity.class);
        this.startActivity(detailIntent);
    }

    public void btnGoInformationActivity(View view) {
        Intent detailIntent = new Intent(this, InformationActivity.class);
        this.startActivity(detailIntent);
    }

}
