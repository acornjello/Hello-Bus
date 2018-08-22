package test.sks.com.busapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by acornjello on 2017-07-22.
 */

class PeripheralAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<PeripheralItem> peripheralItems;
    private int itemLayout;
    private Activity context;

    public PeripheralAdapter(ArrayList<PeripheralItem> items, int itemLayout, Activity context) {
        this.peripheralItems = items;
        this.itemLayout = itemLayout;
        this.context = context;
    }

    public void updateItems(ArrayList<PeripheralItem> newItems) {
        peripheralItems.clear();
        peripheralItems.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItem(PeripheralItem viewModel) {
        peripheralItems.add(viewModel);
        notifyDataSetChanged();
    }


    public static class PeripheralItemViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextMobileNo;
        public TextView mTextStationName;

        public PeripheralItemViewHolder(View view) {
            super(view);

            mTextMobileNo = (TextView) view.findViewById(R.id.text_mobile_no);
            mTextStationName = (TextView) view.findViewById(R.id.text_station_name);

        }
    }

    public static class PeripheralWithBusItemViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextMobileNo;
        public TextView mTextStationName;
        public ImageView mImgBusLocation;

        public PeripheralWithBusItemViewHolder(View view) {
            super(view);

            mTextMobileNo = (TextView) view.findViewById(R.id.text_mobile_no);
            mTextStationName = (TextView) view.findViewById(R.id.text_station_name);
            mImgBusLocation = (ImageView) view.findViewById(R.id.img_bus_location);

        }
    }

    @Override
    public int getItemViewType(int position) {

        if ( context.getLocalClassName().equals("PeripheralStation")) return 1;
        else  return 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 1 :
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station, parent, false);
                return new PeripheralItemViewHolder(view);
            case 2 :
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station_with_bus, parent, false);
                return new PeripheralWithBusItemViewHolder(view);

            default: return null;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        PeripheralItem item = peripheralItems.get(position);

        if(holder instanceof PeripheralItemViewHolder) {   // SearchStationActivity
            ((PeripheralItemViewHolder) holder).mTextMobileNo.setText(item.getMobile_no());
            ((PeripheralItemViewHolder) holder).mTextStationName.setText(item.getStation_nm());
            ((PeripheralItemViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeripheralItem selectedItem = peripheralItems.get(position);
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
            ((PeripheralWithBusItemViewHolder) holder).mTextMobileNo.setText(item.getMobile_no());
            ((PeripheralWithBusItemViewHolder) holder).mTextStationName.setText(item.getStation_nm());
            ((PeripheralWithBusItemViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeripheralItem selectedItem = peripheralItems.get(position);
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

            if (item.isExist())
                ((PeripheralWithBusItemViewHolder) holder).mImgBusLocation.setImageResource(R.drawable.bus_icon2);
            else
                ((PeripheralWithBusItemViewHolder) holder).mImgBusLocation.setImageResource(R.drawable.line2);
        }

    }

    @Override
    public int getItemCount() {
        return peripheralItems.size();
    }


}
