package test.sks.com.busapp;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by acornjello on 2017-07-22.
 */

class StationRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<StationItem> stationItems;
    private int itemLayout;
    private Activity context;
    private DBHelper accessDB;
    String startStationName, endStationName;
    int endStationSeq;

    public StationRecyclerAdapter(ArrayList<StationItem> items, int itemLayout, Activity context) {
        this.stationItems = items;
        this.itemLayout = itemLayout;
        this.context = context;

        accessDB = DBHelper.getInstance(context);
        accessDB.connect();

        if(context instanceof RouteInfoActivity) {
            int routeId = ((RouteInfoActivity) context).getRcvRouteId();
            String routeName = ((RouteInfoActivity) context).getRcvRouteName();

            startStationName = accessDB.selectStartStationName(routeId);
            endStationName = accessDB.selectEndStationName(routeName, startStationName);
            endStationSeq = accessDB.selectEndStationSeq(routeName, endStationName);
        }
    }

    public void updateItems(ArrayList<StationItem> newItems) {
        stationItems.clear();
        stationItems.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItem(StationItem viewModel) {
        stationItems.add(viewModel);
        notifyDataSetChanged();
    }


    public static class StationItemViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextMobileNo;
        public TextView mTextStationName;

        public StationItemViewHolder(View view) {
            super(view);

            mTextMobileNo = (TextView) view.findViewById(R.id.text_mobile_no);
            mTextStationName = (TextView) view.findViewById(R.id.text_station_name);

        }
    }

    public static class StationWithBusItemViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextMobileNo;
        public TextView mTextStationName;
        public ImageView mImgBusLocation;

        public StationWithBusItemViewHolder(View view) {
            super(view);

            mTextMobileNo = (TextView) view.findViewById(R.id.text_mobile_no);
            mTextStationName = (TextView) view.findViewById(R.id.text_station_name);
            mImgBusLocation = (ImageView) view.findViewById(R.id.img_bus_location);

        }
    }

    @Override
    public int getItemViewType(int position) {
        // SearchStationActivity
        if ( context.getLocalClassName().equals("SearchStationActivity")) return 1;
        // RouteInfoActivity
        else  return 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 1 :
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station, parent, false);
                return new StationItemViewHolder(view);
            case 2 :
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station_with_bus, parent, false);
                return new StationWithBusItemViewHolder(view);

            default: return null;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        StationItem item = stationItems.get(position);


        if(holder instanceof StationItemViewHolder) {   // SearchStationActivity
            ((StationItemViewHolder) holder).mTextMobileNo.setText(item.getMobile_no());
            ((StationItemViewHolder) holder).mTextStationName.setText(item.getStation_nm());
            ((StationItemViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StationItem selectedItem = stationItems.get(position);
                    if (selectedItem.getMobile_no() == null) {
                        Toast.makeText(context, selectedItem.getStation_nm() + "은 미정차 구간입니다.", Toast.LENGTH_LONG).show();

                    } else {

                        Intent routeDetailIntent = new Intent(context, StationInfoActivity.class);

                        routeDetailIntent.putExtra("mobileNo", selectedItem.getMobile_no());
                        routeDetailIntent.putExtra("stationName", selectedItem.getStation_nm());

                        context.startActivity(routeDetailIntent);

                    }
                }
            });
        }
        else {  // RouteInfoActivity
            ((StationWithBusItemViewHolder) holder).mTextMobileNo.setText(item.getMobile_no());
            ((StationWithBusItemViewHolder) holder).mTextStationName.setText(item.getStation_nm());
            ((StationWithBusItemViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StationItem selectedItem = stationItems.get(position);
                    if (selectedItem.getMobile_no() == null) {
                        Toast.makeText(context, selectedItem.getStation_nm() + "은 미정차 구간입니다.", Toast.LENGTH_LONG).show();

                    } else {

                        Intent routeDetailIntent = new Intent(context, StationInfoActivity.class);

                        routeDetailIntent.putExtra("mobileNo", selectedItem.getMobile_no());
                        routeDetailIntent.putExtra("stationName", selectedItem.getStation_nm());

                        context.startActivity(routeDetailIntent);
                    }
                }
            });


            if (item.isExist()) {
                ((StationWithBusItemViewHolder) holder).itemView.setContentDescription("버스 경유 중. " + item.getMobile_no() + " " + item.getStation_nm());
                if (position < endStationSeq-1) {
                    ((StationWithBusItemViewHolder) holder).mImgBusLocation.setImageResource(R.drawable.bus1);
                } else {
                    ((StationWithBusItemViewHolder) holder).mImgBusLocation.setImageResource(R.drawable.bus2);
                }
            }
            else {
                ((StationWithBusItemViewHolder) holder).itemView.setContentDescription(item.getMobile_no() + " " + item.getStation_nm());
                if (position < endStationSeq-1) {
                    ((StationWithBusItemViewHolder) holder).mImgBusLocation.setImageResource(R.drawable.line1);
                } else {
                    ((StationWithBusItemViewHolder) holder).mImgBusLocation.setImageResource(R.drawable.line2);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return stationItems.size();
    }


}
