package test.sks.com.busapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;


/**
 * Created by acornjello on 2017-07-22.
 */

class RouteRecyclerAdapter extends RecyclerView.Adapter<RouteRecyclerAdapter.RouteItemViewHolder> {
    //private ArrayList<RouteItem> routeItems;
    private ArrayList<RouteItem> routeItems;
    private int itemLayout;
    private Activity context;
    private DBHelper accessDB = null;


    public RouteRecyclerAdapter(ArrayList<RouteItem> items, int itemLayout, Activity context) {
        this.routeItems = items;
        this.itemLayout = itemLayout;
        this.context = context;
    }

    public void updateItems(ArrayList<RouteItem> viewModels) {
        routeItems.clear();
        routeItems.addAll(viewModels);
        notifyDataSetChanged();
    }
    public void addItem(RouteItem viewModel) {
        //routeItems.add(position, viewModel);
        routeItems.add(viewModel);
        notifyDataSetChanged();
        //notifyItemInserted(position);
    }

    public void removeItem(int position) {
        routeItems.remove(position);
        notifyItemRemoved(position);
    }


    public static class RouteItemViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextRouteCity;
        public TextView mTextRouteName;

        public RouteItemViewHolder(View view) {
            super(view);
            mTextRouteName = (TextView) view.findViewById(R.id.text_route_name);
            mTextRouteCity = (TextView) view.findViewById(R.id.text_route_city);
        }

    }

    @Override
    public RouteRecyclerAdapter.RouteItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteItemViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RouteItemViewHolder holder, final int position) {
        RouteItem item = routeItems.get(position);

        holder.mTextRouteName.setText(item.getRoute_nm());
        holder.mTextRouteCity.setText(item.getCity());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteItem selectedItem = routeItems.get(position);

                Intent routeDetailIntent = new Intent(context, RouteInfoActivity.class);

                routeDetailIntent.putExtra("routeID", selectedItem.getRoute_id());
                routeDetailIntent.putExtra("routeName", selectedItem.getRoute_nm());
                routeDetailIntent.putExtra("activity", "SearchRouteActivity");

                context.startActivity(routeDetailIntent);

            }
        });

    }
    @Override
    public int getItemCount() {
        return routeItems.size();
    }


}
