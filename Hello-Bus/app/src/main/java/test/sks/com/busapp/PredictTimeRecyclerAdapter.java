package test.sks.com.busapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


/**
 * Created by acornjello on 2017-07-22.
 */

class PredictTimeRecyclerAdapter extends RecyclerView.Adapter<PredictTimeRecyclerAdapter.RouteItemViewHolder> {
    private int itemLayout;
    Activity context;
    private int count = 0;
    private DBHelper accessDB;
    private ArrayList<RouteItem> routeItems;
    private AlertItem alertItem;
    private AlertStatus alertStatus = AlertStatus.NONE;
    private ArrivalAlarmItem arrivalItem;
    private ArrivalAlarmStatus arrivalAlarmStatus = ArrivalAlarmStatus.NONE;
    private ArrayList<FavoriteRouteItem> favoriteItems; // 처음에만 각 routeItem이 즐겨찾기 되어있는지 확인하도록
    private ArrayList<RouteDirectionItem> routeDirectionItems;
    Bundle bundle = new Bundle();
    private PredictTimeRecyclerAdapter.RouteItemViewHolder mHolder;

    public PredictTimeRecyclerAdapter(ArrayList<RouteItem> items, int itemLayout, Activity context) {
        this.routeItems = items;
        this.itemLayout = itemLayout;
        this.context = context;

        accessDB = DBHelper.getInstance(context);
        alertItem = accessDB.selectAlert();
        arrivalItem = accessDB.selectArrivalAlarm();
        favoriteItems = new ArrayList<>();
        routeDirectionItems = new ArrayList<>();

        for (RouteItem item : routeItems) {
            /** 즐겨찾기 **/
            FavoriteRouteItem favoriteRouteItem = accessDB.selectFavoriteRouteStation(item.getRoute_nm(), item.getStation_nm(), item.getMobile_no());
            favoriteItems.add(favoriteRouteItem);
            //Log.e("favorite", item.getRoute_id() + " " + item.getRoute_nm() + " " + item.getStation_nm() + " " + item.getMobile_no());

            /** 버스 방향 **/
            String startStationName = accessDB.selectStartStationName(item.getRoute_id());
            String endStationName = accessDB.selectEndStationName(item.getRoute_nm(), startStationName);
            String lastStationName = accessDB.selectLastStationName(item.getRoute_id());
            int endStationSeq = accessDB.selectEndStationSeq(item.getRoute_nm(), endStationName);
            int curStationSeq = accessDB.selectCurStationSeq(item.getRoute_nm(), item.getMobile_no());
            int lastStationSeq = accessDB.selectLastStationSeq(item.getRoute_id());

            routeDirectionItems.add(new RouteDirectionItem(endStationSeq, curStationSeq, lastStationSeq, startStationName, endStationName, lastStationName));

        }

    }

    //public AlertStatus getAlertStatus() { return alertStatus; }
    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }

    public void setArrivalAlarmStatus(ArrivalAlarmStatus alarmStatus) {
        this.arrivalAlarmStatus = alarmStatus;
    }

    public void updateItems(ArrayList<RouteItem> viewModels) {
        alertItem = accessDB.selectAlert();
        for (int i = 0; i < routeItems.size(); i++)
            routeItems.set(i, viewModels.get(i));
        notifyDataSetChanged();
    }
    public void updateItems2() {
        int idx = 0;
        for (RouteItem item : routeItems) {
            FavoriteRouteItem favoriteRouteItem = accessDB.selectFavoriteRouteStation(item.getRoute_nm(), item.getStation_nm(), item.getMobile_no());
            favoriteItems.set(idx, favoriteRouteItem);
            idx++;
        }
        notifyDataSetChanged();
    }

    public static class RouteItemViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextRouteName;
        public TextView mTextRouteDirection;
        public TextView mTextPredictTime1;
        public TextView mTextPredictTime2;
        public TextView mTextLocationNo1;
        public TextView mTextLocationNo2;
        public Button mBtnRouteFavorite, mBtnArrivalAlarm, mBtnAlert;

        public RouteItemViewHolder(View view) {
            super(view);
            mTextRouteName = (TextView) view.findViewById(R.id.text_route_name);
            mTextRouteDirection = (TextView) view.findViewById(R.id.text_route_direction);
            mTextPredictTime1 = (TextView) view.findViewById(R.id.text_route_predict1);
            mTextPredictTime2 = (TextView) view.findViewById(R.id.text_route_predict2);
            mTextLocationNo1 = (TextView) view.findViewById(R.id.text_route_locationNo1);
            mTextLocationNo2 = (TextView) view.findViewById(R.id.text_route_locationNo2);
            mBtnRouteFavorite = (Button) view.findViewById(R.id.btn_route_favorite);
            mBtnArrivalAlarm = (Button) view.findViewById(R.id.btn_arrival_alarm);
            mBtnAlert = (Button) view.findViewById(R.id.btn_alert);


        }

    }

    @Override
    public PredictTimeRecyclerAdapter.RouteItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station_predict, parent, false);
        return new RouteItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RouteItemViewHolder holder, final int position) {
        //mHolder = holder;
        final RouteItem routeItem = routeItems.get(position);
        final FavoriteRouteItem favoriteItem = favoriteItems.get(position);
        final RouteDirectionItem routeDirectionItem = routeDirectionItems.get(position);
//        String curStationName = accessDB.selectStationName(routeItem.getRoute_id(), routeDirectionItem.getCurStationSeq()+1);
        holder.mTextRouteName.setText(routeItem.getRoute_nm());

        if (routeDirectionItem.getCurStationSeq() == routeDirectionItem.getLastStationSeq()) {
            holder.mTextRouteDirection.setText("종점");
        } else if (routeDirectionItem.getCurStationSeq() == routeDirectionItem.getEndStationSeq()) {
            holder.mTextRouteDirection.setText("회차지점");
        } else if (routeDirectionItem.getCurStationSeq() < routeDirectionItem.getEndStationSeq()) {
            holder.mTextRouteDirection.setText(routeDirectionItem.getEndStationName() + " 방면");
        } else if (routeDirectionItem.getCurStationSeq() > routeDirectionItem.getEndStationSeq()) {
            holder.mTextRouteDirection.setText(routeDirectionItem.getLastStationName() + " 방면");
        }

        if (routeItem.getLocationNo1() == null) {
            holder.mTextLocationNo1.setText("");
            holder.mTextPredictTime1.setText("도착정보 없음");
        } else if (routeItem.getLocationNo1().equals("1")) {
            holder.mTextLocationNo1.setText("(" + routeItem.getLocationNo1() + "번 째전)");
            holder.mTextPredictTime1.setText("곧 도착");
            routeItem.setLocationNo1(null);
            routeItem.setPredictTime1(null);
        } else {
            holder.mTextLocationNo1.setText("(" + routeItem.getLocationNo1() + "번 째전)");
            holder.mTextPredictTime1.setText(routeItem.getPredictTime1() + "분");
        }

        if (routeItem.getLocationNo2() == null) {
            holder.mTextLocationNo2.setText("");
            holder.mTextPredictTime2.setText("도착정보 없음");
        } else {
            holder.mTextLocationNo2.setText("(" + routeItem.getLocationNo2() + "번 째전)");
            holder.mTextPredictTime2.setText(routeItem.getPredictTime2() + "분");
        }

        if (favoriteItem == null) {
            Drawable img = context.getResources().getDrawable(R.drawable.star_blank);
            holder.mBtnRouteFavorite.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        } else {
            Drawable img = context.getResources().getDrawable(R.drawable.star);
            holder.mBtnRouteFavorite.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        }

        if (arrivalItem == null) {
            Drawable img = context.getResources().getDrawable(R.drawable.arrival_alarm_blank);
            holder.mBtnArrivalAlarm.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        } else if (routeItem.getRoute_nm().equals(arrivalItem.getRouteName()) && routeItem.getStation_id() == arrivalItem.getStationId()) {
//            Log.e("Predict~", "도착알람 ON ICON으로 바꿈");
            Drawable img = context.getResources().getDrawable(R.drawable.arrival_alarm);
            holder.mBtnArrivalAlarm.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        }


        if (alertItem != null && alertItem.getRouteName().equals(routeItem.getRoute_nm()) && alertItem.getStationName().equals(routeItem.getStation_nm())) {
            mHolder = holder;
            Drawable img = context.getResources().getDrawable(R.drawable.alarm);
            holder.mBtnAlert.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        } else {
            Drawable img = context.getResources().getDrawable(R.drawable.alarm_blank);
            holder.mBtnAlert.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        }

        holder.mBtnRouteFavorite.setContentDescription(routeItem.getStation_nm()+"정류소. " + routeItem.getRoute_nm() + "번 즐겨찾기");
        holder.mBtnRouteFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int routeId = routeItem.getRoute_id(), stationId = routeItem.getStation_id();
                String routeName = routeItem.getRoute_nm(), routeCity = routeItem.getCity();
                String stationName = routeItem.getStation_nm(), mobileNo = routeItem.getMobile_no();
                Log.e("PredictTime~", "POSITION " + position);
                if (accessDB.selectFavoriteRouteStation(routeName, stationName, mobileNo) == null) {
                    Drawable img = context.getResources().getDrawable(R.drawable.star);
                    holder.mBtnRouteFavorite.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                    accessDB.insertFavoriteRouteStation(routeId, routeName, routeCity, stationId, stationName, mobileNo);
                    favoriteItems.set(position, accessDB.selectFavoriteRouteStation(routeName, stationName, mobileNo));
                    Toast.makeText(context, stationName + "정류소의 " + routeName + "번 버스를 즐겨찾기 하였습니다.", Toast.LENGTH_LONG).show();

                } else {

                    Drawable img = context.getResources().getDrawable(R.drawable.star_blank);
                    holder.mBtnRouteFavorite.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                    Toast.makeText(context, stationName + "정류소의 " + routeName + "번 버스 즐겨찾기를 해제하였습니다.", Toast.LENGTH_LONG).show();
                    int idx = 0;

                    BusProvider.getInstance().post(favoriteItem);
                    favoriteItems.set(position, null);
//                    accessDB.deleteFavorite2(routeId, routeName, routeCity, stationName, mobileNo);
//                    if (context instanceof RouteStationInfoActivity) {
//                        //int pos = (int) context.getIntent().getExtras().get("pos");
                    Log.d("PredictTime~", "POSITION" + position);
//
//                        //accessDB.deleteFavorite2(routeId, routeName, routeCity, stationName, mobileNo);
//
//
//
//                        //EventBus.getDefault().post(pos);
//
//                    } else {
//                        //accessDB.deleteFavorite2(routeId, routeName, routeCity, stationName, mobileNo);
//                        favoriteItems.set(position, null);
//                    }

                }


            }
        });

        holder.mBtnArrivalAlarm.setContentDescription(routeItem.getRoute_nm() + "번 첫 번째 버스 도착 알람 요청");
        holder.mBtnArrivalAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String routeName = routeItem.getRoute_nm();
                int stationId = routeItem.getStation_id();
                arrivalItem = accessDB.selectArrivalAlarm();

                if(alertItem != null) {
                    Toast.makeText(context, "도착 알람 실패. " + alertItem.getRouteName() + "번 탑승 요청을 취소해주세요.", Toast.LENGTH_LONG).show();
                } else if(arrivalAlarmStatus == ArrivalAlarmStatus.CANCEL){
//                    Log.e("PredictTime~", position + " " + routeName + " " + arrivalItem.getRouteName() + "알람 ICON OFF 바꿈");
//                    Toast.makeText(context, arrivalItem.getRouteName() + "번 버스가 지나가 도착알림을 종료합니다.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, AlertService.class);

                    context.stopService(intent);

                    accessDB.deleteAllArrivalAlarm();
                    arrivalItem = null;

                    Drawable img = context.getResources().getDrawable( R.drawable.arrival_alarm_blank );
                    holder.mBtnArrivalAlarm.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);

                }  else if (arrivalItem != null) {
                    if(routeItem.getRoute_nm().equals(arrivalItem.getRouteName()) && routeItem.getStation_id() == arrivalItem.getStationId()) {
//                        Log.e("PredictTime~", position + " " + routeName + " " + arrivalItem.getRouteName() + "알람 ICON OFF 바꿈");
                        Toast.makeText(context, arrivalItem.getRouteName() + "번 도착 알람을 취소하였습니다.", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(context, AlertService.class);
                        NotificationManager Notifi_M = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        Notifi_M.cancel(777);
                        context.stopService(intent);

                        accessDB.deleteAllArrivalAlarm();
                        arrivalItem = null;

                        Drawable img = context.getResources().getDrawable(R.drawable.arrival_alarm_blank);
                        holder.mBtnArrivalAlarm.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                    } else {
                        Toast.makeText(context, "도착 알람 실패. " + arrivalItem.getRouteName() + "번 도착 알람을 취소해주세요.", Toast.LENGTH_LONG).show();
                    }

                } else if (holder.mTextLocationNo1.getText() == "") {
                    Toast.makeText(context, "도착 알람 실패. "+ routeName + "번 도착 정보가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                } else if ( arrivalItem == null ){
                    accessDB.insertArrivalAlarm( -1, routeItem.getRoute_id(), routeItem.getRoute_nm(), routeItem.getStation_id(), routeItem.getStation_nm(), routeItem.getMobile_no(), context.getLocalClassName());
                    arrivalItem = accessDB.selectArrivalAlarm();
                    if(routeName.equals(arrivalItem.getRouteName()) && stationId == arrivalItem.getStationId() ) {
                        mHolder = holder;
                        Drawable img = context.getResources().getDrawable(R.drawable.arrival_alarm);
                        holder.mBtnArrivalAlarm.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                        BusProvider.getInstance().post(arrivalItem);
                    }
                }
                arrivalAlarmStatus = ArrivalAlarmStatus.NONE;
            }
        });


        holder.mBtnAlert.setContentDescription(routeItem.getRoute_nm() + "번 첫 번째 버스에 탑승 요청");
        holder.mBtnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String station_id = String.valueOf(routeItem.getStation_id());
                String route_name = routeItem.getRoute_nm();
                DatabaseReference routeRef = FirebaseDatabase.getInstance().getReference().child("Alert").child(station_id).child(route_name);

                alertItem = accessDB.selectAlert();
                arrivalItem = accessDB.selectArrivalAlarm();
                if(arrivalItem != null) {
                    Toast.makeText(context, "탑승 요청 실패. " + arrivalItem.getRouteName() + "번 도착 알람을 취소해주세요.", Toast.LENGTH_LONG).show();
                }
                else if(alertStatus == AlertStatus.ALARM) {
                    if(routeItem.getRoute_nm().equals(alertItem.getRouteName())) {
                        Drawable img = context.getResources().getDrawable( R.drawable.alarm );
                        holder.mBtnAlert.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                        Log.e("PredictTime~", position +" " + routeItem.getRoute_nm() + " " + alertItem.getRouteName() +"알람 ICON ON 바꿈");

                        alertStatus = AlertStatus.NONE;
                    }
                } else {

                    if (alertStatus != AlertStatus.CANCEL && holder.mTextLocationNo1.getText() == "") {
                        Toast.makeText(context, route_name + "번 버스의 도착정보가 존재하지 않아 탑승 요청을 할 수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                    else if (alertItem == null ||
                            (alertItem.getRouteName().equals(route_name) && alertItem.getStationId() == Integer.valueOf(station_id))) {
                        if(alertItem == null) mHolder = holder;
                        if(alertItem != null) {
                            Log.e("PredictTime~","알람 ICON OFF 바꿈");
                            Drawable img = context.getResources().getDrawable( R.drawable.alarm_blank );
                            holder.mBtnAlert.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                        }
                        onAlertButtonClicked(routeRef, position);


                    } else
                        Toast.makeText(context, "이미 " + alertItem.getRouteName() + "번 버스에 탑승 요청을 하였습니다.", Toast.LENGTH_LONG).show();
                }

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent routeDetailIntent = new Intent(context, RouteInfoActivity.class);
                routeDetailIntent.putExtra("routeID", routeItem.getRoute_id());
                routeDetailIntent.putExtra("routeName", routeItem.getRoute_nm());
                routeDetailIntent.putExtra("activity", "StationInfoActivity");
                if(context instanceof StationInfoActivity) ((StationInfoActivity) context).stopBackgroundPerform();
                else ((RouteStationInfoActivity) context).stopBackgroundPerform();
                context.startActivity(routeDetailIntent);
            }
        });


    }

    private void onAlertButtonClicked(final DatabaseReference routeRef, final int position) {
        routeRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer value = mutableData.getValue(Integer.class);
                final RouteItem routeItem = routeItems.get(position);

                if (value != null) {
                    if (alertItem == null) {
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                String station_id = String.valueOf(routeItems.get(position).getStation_id());
                                String route_name = routeItems.get(position).getRoute_nm();
                                Log.e("얻어온 정류소번호", station_id);

                                Intent intent = new Intent(context, AlarmActivity.class);

                                intent.putExtra("station_id", station_id);
                                intent.putExtra("route_name", route_name);
                                intent.putExtra("position", position);
                                intent.putExtra("activityName", context.getLocalClassName());
                                //Log.e("PredictTime~", context.getPackageName() + " " + context.getLocalClassName() + " " + context.getCallingPackage());
                                context.startActivity(intent);
                            }
                        });

                        alertItem = accessDB.selectAlert();
                        if (alertItem != null) {
                            routeItem.setAlert(true);
                        }

                    } else {
                        if (alertStatus != AlertStatus.CANCEL) {
                            context.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(context, routeItem.getRoute_nm() + "번 버스 탑승알림요청을 취소하였습니다.", Toast.LENGTH_LONG).show();
                                }
                            });

                            Intent intent = new Intent(context, AlertService.class);
                            NotificationManager Notifi_M = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            Notifi_M.cancel(777);
                            context.stopService(intent);

                            routeItem.setAlert(false);
                            if (value > 0) value--;
                        }
                        Log.e("PREDICT SERVICE", "알림 취소 SERVICE 종료");
                        accessDB.deleteAllAlert();
                        alertItem = accessDB.selectAlert();
                        alertStatus = AlertStatus.NONE;
                    }

                }
                mutableData.setValue(value);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d("$$", ":onComplete:" + databaseError);
            }

        });
    }

    @Override
    public int getItemCount() {
        return routeItems.size();
    }

    public PredictTimeRecyclerAdapter.RouteItemViewHolder getmHolder() {
        return mHolder;
    }

    public Bundle getData() {
        return bundle;
    }

}
