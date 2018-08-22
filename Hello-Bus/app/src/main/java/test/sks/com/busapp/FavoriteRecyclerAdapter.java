package test.sks.com.busapp;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by acornjello on 2017-08-12.
 */

public class FavoriteRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<FavoriteRouteItem> favoriteRouteItems;
    private int itemLayout;
    private Activity context;
    private DBHelper accessDB;
    private RecyclerView.ViewHolder mHolder;
    private RequestRemoval requestRemovalType =  RequestRemoval.NONE;
    enum RequestRemoval {
        ROUTE,
        STATION,
        ROUTESTATION,
        NONE;
    };
    private FavoriteRouteItem requestRemovalItem;
    public FavoriteRecyclerAdapter(ArrayList<FavoriteRouteItem> items, int itemLayout, Activity context) {
        this.favoriteRouteItems = items;
        this.itemLayout = itemLayout;
        this.context = context;
        accessDB = DBHelper.getInstance(context);
        accessDB.connect();
    }

    public void updateItems(ArrayList<FavoriteRouteItem> viewModels) {
        favoriteRouteItems.clear();
        favoriteRouteItems.addAll(viewModels);
        notifyItemRangeChanged(0, favoriteRouteItems.size());
    }
    public void removeItem2(int position) {
        if(favoriteRouteItems.size() != 0) favoriteRouteItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, favoriteRouteItems.size());
    }
    public void removeItem(int position, RecyclerView.ViewHolder holder) {
        FavoriteRouteItem item = favoriteRouteItems.get(position);
        accessDB.deleteFavorite(item.getId());

        int newPosition = holder.getAdapterPosition();
        favoriteRouteItems.remove(newPosition);

        notifyItemRemoved(newPosition);
        notifyItemRangeChanged(newPosition, favoriteRouteItems.size());


        Log.e("AFTER", favoriteRouteItems.size() + "");
    }
    public RecyclerView.ViewHolder getmHolder() { return mHolder; }
    public void setRequestRemovalType( RequestRemoval requestRemoval ) { this.requestRemovalType = requestRemoval; }
    public void setRequestRemovalItem ( FavoriteRouteItem requestRemovalItem ) { this.requestRemovalItem = requestRemovalItem; }

    public static class RouteItemViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextRouteName;
        public TextView mTextRouteCity;
        public ImageButton mBtnDeleteFavorite;

        public RouteItemViewHolder(View view) {
            super(view);

            mTextRouteName = (TextView) view.findViewById(R.id.text_route_name);
            mTextRouteCity = (TextView) view.findViewById(R.id.text_route_city);
            mBtnDeleteFavorite = (ImageButton) view.findViewById(R.id.btn_delete_favorite);
        }
    }

    public static class RouteStationItemViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextRouteName;
        public TextView mTextRouteCity;
        public TextView mTextMobileNo;
        public TextView mTextStationName;
        public ImageButton mBtnDeleteFavorite;

        public RouteStationItemViewHolder(View view) {
            super(view);

            mTextRouteName = (TextView) view.findViewById(R.id.text_route_name);
            mTextRouteCity = (TextView) view.findViewById(R.id.text_route_city);
            mTextMobileNo = (TextView) view.findViewById(R.id.text_mobile_no);
            mTextStationName = (TextView) view.findViewById(R.id.text_station_name);
            mBtnDeleteFavorite = (ImageButton) view.findViewById(R.id.btn_delete_favorite);

        }
    }

    public static class StationItemViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextStationName;
        public TextView mTextMobileNo;
        public ImageButton mBtnDeleteFavorite;

        public StationItemViewHolder(View view) {
            super(view);

            mTextStationName = (TextView) view.findViewById(R.id.text_station_name);
            mTextMobileNo = (TextView) view.findViewById(R.id.text_mobile_no);
            mBtnDeleteFavorite = (ImageButton) view.findViewById(R.id.btn_delete_favorite);
        }
    }

    @Override
    public int getItemViewType(int position) {
        FavoriteRouteItem item = favoriteRouteItems.get(position);

        if (item.getMobile_no() == null) {        // 버스
            return 1;
        } else if (item.getRoute_name() != null) {   // 특정 정류소가 지정된 버스
            return 2;
        } else {                                            // 정류소
            return 3;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_route, parent, false);
                return new FavoriteRecyclerAdapter.RouteItemViewHolder(view);
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_route_station, parent, false);
                return new FavoriteRecyclerAdapter.RouteStationItemViewHolder(view);
            case 3:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_station, parent, false);
                return new FavoriteRecyclerAdapter.StationItemViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        mHolder = holder;
        final int pos = holder.getAdapterPosition();
        final FavoriteRouteItem item = favoriteRouteItems.get(pos);

        if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            if (holder instanceof RouteItemViewHolder) {    // 그냥 버스
                ((RouteItemViewHolder) holder).mTextRouteName.setText(item.getRoute_name());
                ((RouteItemViewHolder) holder).mTextRouteCity.setText(item.getRoute_city());
                ((RouteItemViewHolder) holder).mBtnDeleteFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, item.getRoute_name()+"번 버스 즐겨찾기를 삭제하였습니다.", Toast.LENGTH_LONG).show();
                        removeItem(pos, holder);
                    }
                });

            } else if (holder instanceof RouteStationItemViewHolder) { // 특정 정류소 버스
                ((RouteStationItemViewHolder) holder).mTextRouteName.setText(item.getRoute_name());
                ((RouteStationItemViewHolder) holder).mTextMobileNo.setText("("+item.getMobile_no() + ")");
                ((RouteStationItemViewHolder) holder).mTextStationName.setText(item.getStation_name());
//                ((RouteStationItemViewHolder) holder).mBtnDeleteFavorite.setContentDescription(item.getStation_name()+"정류소"+item.getRoute_name()+"번 즐겨찾기 삭제");
                ((RouteStationItemViewHolder) holder).mBtnDeleteFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, item.getStation_name()+"정류소의 " + item.getRoute_name() + "번 버스 즐겨찾기를 삭제하였습니다.", Toast.LENGTH_LONG).show();
                        removeItem(pos, holder);
                    }
                });

                ((RouteStationItemViewHolder) holder).mTextMobileNo.setContentDescription("정류소 번호" + item.getMobile_no());


            } else {    // 정류소
                ((StationItemViewHolder) holder).mTextStationName.setText(item.getStation_name());
                ((StationItemViewHolder) holder).mTextMobileNo.setText("("+item.getMobile_no() + ")");
                ((StationItemViewHolder) holder).mBtnDeleteFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, item.getStation_name()+"정류소 즐겨찾기를 삭제하였습니다.", Toast.LENGTH_LONG).show();
                        removeItem(pos, holder);
                    }
                });

                ((StationItemViewHolder) holder).mTextMobileNo.setContentDescription("정류소 번호" + item.getMobile_no());
            }

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (item.getMobile_no() == null) { // 그냥 버스
                    Intent routeDetailIntent = new Intent(context, RouteInfoActivity.class);

                    routeDetailIntent.putExtra("routeID", item.getRoute_id());
                    routeDetailIntent.putExtra("routeName", item.getRoute_name());
                    routeDetailIntent.putExtra("activity", "FavoriteInfoActivity");

                    context.startActivity(routeDetailIntent);
                } else if (item.getRoute_name() == null) {  // 정류소 즐겨찾기인 경우
                    Intent stationDetailIntent = new Intent(context, StationInfoActivity.class);

                    stationDetailIntent.putExtra("mobileNo", item.getMobile_no());
                    stationDetailIntent.putExtra("stationName", item.getStation_name());
                    stationDetailIntent.putExtra("activity", "FavoriteInfoActivity");

                    context.startActivity(stationDetailIntent);

                } else {    // 특정 정류소 버스 즐겨찾기인 경우
                    RouteStationInfoActivity activity = new RouteStationInfoActivity();
                    Intent routeDetailIntent = new Intent(context, activity.getClass());

                    routeDetailIntent.putExtra("routeId", item.getRoute_id());
                    routeDetailIntent.putExtra("routeName", item.getRoute_name());
                    routeDetailIntent.putExtra("stationId", item.getStation_id());
                    routeDetailIntent.putExtra("stationName", item.getStation_name());
                    routeDetailIntent.putExtra("mobileNo", item.getMobile_no());

                    context.startActivity(routeDetailIntent);
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return favoriteRouteItems.size();
    }


}
