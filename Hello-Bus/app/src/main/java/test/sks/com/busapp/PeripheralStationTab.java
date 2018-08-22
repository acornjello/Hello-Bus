package test.sks.com.busapp;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TabHost;

public class PeripheralStationTab extends TabActivity  {
    TabHost mTabHost;
    FrameLayout mFrameLayout;

    /** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("여기는","탭메인");
        setContentView(R.layout.peripheral_tab);
        mTabHost = getTabHost();


        mTabHost.addTab(getTabHost().newTabSpec("tab1").setIndicator("Map").setContent(new Intent(this, PeripheralStation.class)));
        mTabHost.addTab(getTabHost().newTabSpec("tab2").setIndicator("List").setContent(new Intent(this, PeripheralStationList.class)));
        //.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))); 클릭할 때마다 리플레쉬
//        TabHost.TabSpec tabSpec = mTabHost.newTabSpec("tab_test1");
//        tabSpec.setIndicator("Map");
//        Context ctx = this.getApplicationContext();
//        Intent i = new Intent(ctx, PeripheralStationList.class);
//        tabSpec.setContent(i);
//        mTabHost.addTab(tabSpec);

//        Context ctx2 = this.getApplicationContext();
//        Intent i2 = new Intent(ctx2, PeripheralStation.class);
//        mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("List").setContent(i2));
//        mTabHost.setCurrentTab(0);
//        Log.e("알림","탭 메인 끝");
    }
}