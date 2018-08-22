package test.sks.com.busapp;

import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by acorn on 2017-09-20.
 */

public class TabPagerAdapter extends FragmentStatePagerAdapter {
    // Count number of tabs
    private int tabCount;
    private ArrayList<FavoriteRouteItem> favoriteItems;
    private ArrayList<Fragment> fragments;
    public TabPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;

    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:

                return FavoriteRoute.newInstance();
            case 1:
                return FavoriteStation.newInstance();
//            case 2:
//                TabFragment3 tabFragment3 = new TabFragment3();
//                return tabFragment3;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "버스";
            case 1:
                return "정류소";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}
