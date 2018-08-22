package test.sks.com.busapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;


public class FavoriteInfoActivity extends AppCompatActivity {
    private DBHelper accessDB = null;
    //public FavoriteRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<FavoriteRouteItem> favoriteRouteItems;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager mViewPager;
    TabPagerAdapter mAdapter;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_info);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("즐겨찾기");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("즐겨찾기");
        initViewPager();
    }
    private void initViewPager() {


        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new FavoriteRoute());
        fragments.add(new FavoriteStation());

        mViewPager = (ViewPager) findViewById(R.id.tab_pager);
        mAdapter = new TabPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapter);


        tabLayout = (TabLayout) findViewById(R.id.my_tablayout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabTextColors(Color.LTGRAY, getResources().getColor(R.color.colorPrimaryText));

    }
    public void btnGoHome(View v) {
        Intent intent = new Intent(this, ModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("FavoriteInfo", "Restart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("FavoriteInfo", "Destroy");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("FavoriteInfo", "Back Pressed");
        finish();
    }

}
