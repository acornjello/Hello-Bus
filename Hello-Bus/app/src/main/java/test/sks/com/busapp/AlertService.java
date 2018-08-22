package test.sks.com.busapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;


/**
 * Created by acornjello on 2017-09-11.
 */

public class AlertService extends Service {
    NotificationManager Notifi_M;
    AlertServiceThread thread;
    Notification Notifi ;
    NotificationItem notiItem = new NotificationItem();
    DBHelper accessDB;
    AlertItem alertItem;
    ArrivalAlarmItem arrivalItem;




    boolean bArribal = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("SERVICE", "서비스의 onCreate");

        accessDB = DBHelper.getInstance(getApplicationContext());
        accessDB.connect();

        alertItem = accessDB.selectAlert();
        arrivalItem = accessDB.selectArrivalAlarm();


        if(alertItem != null) {
            notiItem.setStationName(alertItem.getStationName());
            notiItem.setRouteName(alertItem.getRouteName());
        } else {
            notiItem.setStationName(arrivalItem.getStationName());
            notiItem.setRouteName(arrivalItem.getRouteName());
        }


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("SERVICE", "서비스의 onStartCommand");

        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new AlertServiceThread(handler);
        thread.setContext(this);

        long[] vibrate = {500,1000};

        alertItem = accessDB.selectAlert();
        arrivalItem = accessDB.selectArrivalAlarm();


        Intent intent2;
        if(alertItem != null) {
            alertItem = accessDB.selectAlert();
            intent2 = new Intent(AlertService.this, AlertInfoActivity.class);
            Log.e("AlertService", "StationInfo");
        }
        else {
            Log.e("AlertService", "RouteStation");
            arrivalItem = accessDB.selectArrivalAlarm();

            intent2 = new Intent(AlertService.this, RouteStationInfoActivity.class);

            intent2.putExtra("routeId", arrivalItem.getRouteId());
            intent2.putExtra("routeName", arrivalItem.getRouteName());
            intent2.putExtra("stationId", arrivalItem.getStationId());
            intent2.putExtra("mobileNo", arrivalItem.getMobile_no());
            intent2.putExtra("stationName", arrivalItem.getStationName());

        }
        PendingIntent pendingIntent = PendingIntent.getActivity(AlertService.this, 0, intent2,PendingIntent.FLAG_UPDATE_CURRENT);


        Notifi = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(notiItem.getRouteName()+"번 버스 상단바 도착 알림")
                .setContentTitle(notiItem.getStationName() + "정류소 " + notiItem.getRouteName() + "번 버스 도착 알림")
                .setContentText(notiItem.getLocationNo() + " " + notiItem.getPredictTime())
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setVibrate(vibrate)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setOngoing(true)
                .setNumber(777)
                .build();

        Notifi_M.notify( 777 , Notifi);
        try {
            thread.sleep(1500);
        } catch (Exception e) {

        }
        thread.start();
        return START_STICKY;

        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AlertService", "서비스의 onDestroy");

        alertItem = null;
        arrivalItem = null;
        accessDB.deleteAllAlert();
        accessDB.deleteAllArrivalAlarm();
        BusProvider.getInstance().unregister(this);
        if(thread != null) thread.stopForever();
        thread = null;
    }

    class myServiceHandler extends Handler {

        public myServiceHandler() {
            super();
            BusProvider.getInstance().register(this);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent;
            long[] vibrate = {500,1000};
            PendingIntent pendingIntent = null;

            if(alertItem != null) {
                intent = new Intent(AlertService.this, AlertInfoActivity.class);
                pendingIntent = PendingIntent.getActivity(AlertService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            else if(arrivalItem != null){
                intent = new Intent(AlertService.this, RouteStationInfoActivity.class);
                intent.putExtra("routeId", arrivalItem.getRouteId());
                intent.putExtra("routeName", arrivalItem.getRouteName());
                intent.putExtra("mobileNo", arrivalItem.getMobile_no());
                intent.putExtra("stationId", arrivalItem.getStationId());
                intent.putExtra("stationName", arrivalItem.getStationName());

                pendingIntent = PendingIntent.getActivity(AlertService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                //PendingIntent pendingIntent = PendingIntent.getActivity(AlertService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                onDestroy();
            }

//            PendingIntent pendingIntent = PendingIntent.getActivity(AlertService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if(notiItem.getLocationNo().equals("") && bArribal == true) {

                Notifi = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setTicker(notiItem.getRouteName()+"번 버스 상단바 도착 알림")
                        .setContentTitle(notiItem.getStationName() + "정류소 " + notiItem.getRouteName() + "번 버스")
                        .setContentText("지나감")
                        .setWhen(System.currentTimeMillis())
                        .setVibrate(vibrate)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .setNumber(777)
                        .build();
//.setDefaults(Notification.DEFAULT_VIBRATE)
                Notifi_M.notify( 777 , Notifi);
                bArribal = false;


                if(alertItem != null) {
                    Toast.makeText(AlertService.this, notiItem.getRouteName() + "번 버스가 지나가 탑승 요청을 종료합니다.", Toast.LENGTH_LONG).show();
                    BusProvider.getInstance().post(AlertStatus.CANCEL);
                }
                else {
                    Toast.makeText(AlertService.this, notiItem.getRouteName() + "번 버스가 지나가 도착알림을 종료합니다.", Toast.LENGTH_LONG).show();
                    BusProvider.getInstance().post(ArrivalAlarmStatus.CANCEL);
                }
                Log.e("SERVICE BUS EVENT", "서비스 종료");
                onDestroy();
            }
            else if (notiItem.getLocationNo().equals("1번 째전") && notiItem.getPredictTime().equals("곧 도착") || bArribal == true) {
                if(bArribal == true) {
                    Notifi = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setTicker(notiItem.getRouteName() + "번 버스 상단바 도착 알림")
                            .setContentTitle(notiItem.getStationName() + "정류소 " + notiItem.getRouteName() + "번 버스")
                            .setContentText("곧 도착")
                            .setWhen(System.currentTimeMillis())
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setOngoing(true)
                            .setNumber(777)
                            .build();


                    Notifi_M.notify(777, Notifi);
                }
                else {
                    Notifi = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setTicker(notiItem.getRouteName() + "번 버스 상단바 도착 알림")
                            .setContentTitle(notiItem.getStationName() + "정류소 " + notiItem.getRouteName() + "번 버스")
                            .setContentText("곧 도착")
                            .setWhen(System.currentTimeMillis())
                            .setContentIntent(pendingIntent)
                            .setVibrate(vibrate)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setAutoCancel(true)
                            .setOngoing(true)
                            .setNumber(777)
                            .build();

                    Notifi_M.notify(777, Notifi);
                    bArribal = true;
                }
//                Toast.makeText(AlertService.this, "곧 도착 업데이트", Toast.LENGTH_LONG).show();
                Log.e("SERVICE", "곧 도착 업데이트");
            }
            else {
                Notifi = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setTicker("탑승알림을 요청한 "+ notiItem.getRouteName()+"번 버스 상단바 승차알람")
                        .setContentTitle(notiItem.getStationName() + "정류소 " + notiItem.getRouteName() + "번 버스")
                        .setContentText(notiItem.getLocationNo() + " " + notiItem.getPredictTime())
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setNumber(777)
                        .build();

                Notifi_M.notify( 777 , Notifi);

//                Toast.makeText(AlertService.this, "상단바 업데이트", Toast.LENGTH_LONG).show();
                Log.e("SERVICE", "받은 값으로 상단바 업데이트");
            }

            //소리추가
//            Notifi.defaults = Notification.DEFAULT_SOUND;

            //알림 소리를 한번만 내도록
//            Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;



            //확인하면 자동으로 알림이 제거 되도록
//            Notifi.flags = Notification.FLAG_AUTO_CANCEL;

        }

        @Subscribe
        public void FinishLoad(Object mPushEvent) { //PushEvent

            if(mPushEvent != null && mPushEvent instanceof NotificationItem) {
                notiItem = (NotificationItem) mPushEvent;
                Log.e("BUS EVENT", "값 받음 " + notiItem.getStationName() + " " + notiItem.getRouteName() + " " + notiItem.getLocationNo() + " " + notiItem.getPredictTime());
            }
        }


    };

}
