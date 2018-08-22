package test.sks.com.busapp;

import android.content.Context;
import android.net.Uri;
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
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class FavoriteRoute extends Fragment {
    private DBHelper accessDB = null;
    public FavoriteRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<FavoriteRouteItem> favoriteRouteItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_favorite_route, container, false);
        accessDB = DBHelper.getInstance(getContext());
        accessDB.connect();
        BusProvider.getInstance().register(this);
        favoriteRouteItems = accessDB.selectAllFavoriteRoute();

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
    public static FavoriteRoute newInstance() {
        Bundle args = new Bundle();

        FavoriteRoute fragment = new FavoriteRoute();
        fragment.setArguments(args);
        return fragment;
    }

    @Subscribe
    public void FinishLoad(Object mPushEvent) {

        favoriteRouteItems = accessDB.selectAllFavoriteRoute();
        mAdapter.updateItems(favoriteRouteItems);

        if(mPushEvent instanceof FavoriteRouteItem && ((FavoriteRouteItem) mPushEvent).getStation_name() == null) {
            FavoriteRouteItem favoriteRouteItem = (FavoriteRouteItem) mPushEvent;
            //Log.e("FavoriteInfo", ((FavoriteRouteItem) mPushEvent).getStation_id()+"");
            int idx = 0;
            for(FavoriteRouteItem item : favoriteRouteItems) {
//                if(favoriteRouteItem.getRoute_name() == null) {
//                    if (item.getRoute_name() == null && item.getMobile_no().equals(favoriteRouteItem.getMobile_no())) {
//                        break;
//                    }
//                }
                if( favoriteRouteItem.getStation_name() == null ) {
                    if ( item.getStation_name() == null && item.getRoute_id() == favoriteRouteItem.getRoute_id() ) {
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
        Log.e("fav route", "onResume");
        favoriteRouteItems = accessDB.selectAllFavoriteRoute();
        mAdapter.updateItems(favoriteRouteItems);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("fav route", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("fav route", "onDestroy");
    }
}
