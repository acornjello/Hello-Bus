package test.sks.com.busapp;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.ArrayList;


public class FavoriteStation extends Fragment {

    private DBHelper accessDB = null;
    public FavoriteRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<FavoriteRouteItem> favoriteRouteItems;
    private enum TagType {
        X,
        Y,
        None;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_favorite_station, container, false);
        BusProvider.getInstance().register(this);
        accessDB = DBHelper.getInstance(getContext());
        accessDB.connect();
        favoriteRouteItems = accessDB.selectAllFavoriteStation();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recyclerView);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new FavoriteRecyclerAdapter(favoriteRouteItems, 0, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public static FavoriteStation newInstance() {
        Bundle args = new Bundle();

        FavoriteStation fragment = new FavoriteStation();
        fragment.setArguments(args);
        return fragment;
    }


    @Subscribe
    public void FinishLoad(Object mPushEvent) {
        favoriteRouteItems = accessDB.selectAllFavoriteStation();
        mAdapter.updateItems(favoriteRouteItems);
        if(mPushEvent instanceof FavoriteRouteItem && ((FavoriteRouteItem) mPushEvent).getStation_name() != null ) {
            FavoriteRouteItem favoriteRouteItem = (FavoriteRouteItem) mPushEvent;
            Log.e("FavoriteInfo", favoriteRouteItem.getRoute_name() + " " + favoriteRouteItem.getStation_name() + " " + favoriteRouteItem.getMobile_no() );

            int idx = 0;
            for(FavoriteRouteItem item : favoriteRouteItems) {
                Log.e("FavoriteInfo", item.getRoute_id() + " " + item.getRoute_name() + " " + item.getStation_name() + " " + item.getMobile_no() );
                if(favoriteRouteItem.getRoute_name() == null) {
                    if (item.getRoute_name() == null && item.getMobile_no().equals(favoriteRouteItem.getMobile_no())
                            && item.getStation_name().equals(favoriteRouteItem.getStation_name())) {
                        break;
                    }
                } else if( item.getRoute_name() != null ) {
                    if ( item.getRoute_name().equals(favoriteRouteItem.getRoute_name()) && item.getMobile_no().equals(favoriteRouteItem.getMobile_no())
                            && item.getStation_name().equals(favoriteRouteItem.getStation_name())) {
                        break;
                    }
                }
                idx++;
            }

            favoriteRouteItems.remove(idx);
            mAdapter.removeItem2(idx);
            accessDB.deleteFavorite(favoriteRouteItem.getId());
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.e("fav station", "onResume");
        favoriteRouteItems = accessDB.selectAllFavoriteStation();
        mAdapter.updateItems(favoriteRouteItems);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("fav station", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("fav station", "onDestroy");
    }

}
