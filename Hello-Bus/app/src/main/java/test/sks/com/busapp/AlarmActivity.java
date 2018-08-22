package test.sks.com.busapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class AlarmActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //GoogleMap mymap ;
    GoogleApiClient googleApiClient ;
    LocationRequest locationRequest ;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    TextView tv;
    String now_stationId;
    String station_id, route_name, activityName;
    int position;


    DBHelper accessDB;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sample);
        //now_stationId = "201000264";
        Log.e("AlarmActivity","알람 액티비티 시작");
        //tv = (TextView) findViewById(R.id.data);
        setTitle("");
        Intent intent = getIntent();
        station_id = intent.getStringExtra("station_id");
        route_name = intent.getStringExtra("route_name");
        position = intent.getIntExtra("position", -1);
        activityName = intent.getStringExtra("activityName");
        if(position == -1) {
            Log.e("position오류", "postion : " + position);
        }

//        Log.e("알림",station_id);
//        Log.e("알림",route_name);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) { //앱 권한이 있는 경우
//            Log.e("build","시작 전");

            isNetWork();
            accessDB = DBHelper.getInstance(getApplicationContext());
            accessDB.connect();

        } else { // 앱 권한이 없는 경우 PackageManager.PERMISSION_GDENIED를 반환.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION); //권한요청. 비동기식 작동.
            // 즉각 반환. 사용자가 대화상자에 응답한 후에 시스템은 그 결과를 가지고 앱의 콜백 메서드 호출
        }


    }

    protected synchronized void buildGoogleApiClient(){

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("onlocationChanged","시작");
        mLastLocation = location ;
        if(mCurrLocationMarker != null){
            mCurrLocationMarker.remove();
        }


        //LatLng mLatLng = new LatLng(location.getLatitude(), location.getLongitude()) ;
        double latitude = 37.4992833;
        double longitude = 126.75635;

        //LatLng mLatLng = new LatLng(latitude, longitude) ;

        Log.e("위도:",location.getLatitude()+"");
        Log.e("경도:",location.getLongitude()+"");
//
//        Log.e("현재 latitude : ",latitude+" ");
//        Log.e("현재 longitude : ",longitude+"\n");
        //LatLng mLatLng = new LatLng(latitude, longitude) ;
        //tv.append(latitude + " ");
        //tv.append(longitude + "\n");
        //mymap.addMarker(new MarkerOptions().position(mLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        //move map camera
        //mymap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15f));

        parsing(latitude, longitude);

        //stop location updates
        if(googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,  this);
        }
    }
    public void parsing(double latitude, double longitude) {
        String serviceUrl = "http://openapi.gbis.go.kr/ws/rest/busstationservice/searcharound"; //공공 db의 정류소 정보 조회서비스 요청 주소
        String serviceKey = "lDqQj%2BQAXch3eVfO6GFHbKVDZORd6lqUv2nvqvMx7CvVlP8%2FOyxgz%2BcDVmAWbGRDATLsFp1Ikds1h06c9Oezig%3D%3D"; // 오픈 api 인증키
        String strUrl = serviceUrl + "?serviceKey=" + serviceKey + "&x=" + longitude + "&y=" + latitude;

        new DownloadWebpageTask().execute(strUrl);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String)downloadUrl((String)urls[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        protected void onPostExecute(String result) {
            displayStationpos(result);
        } //백그라운드로 다운로드 작업이 완료되면 결과 실행

        private String downloadUrl(String myurl) throws IOException {

            HttpURLConnection conn = null;
            try {
                URL url = new URL(myurl);
                conn = (HttpURLConnection) url.openConnection();
                BufferedInputStream buf = new BufferedInputStream(conn.getInputStream());
                BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf, "utf-8"));
                String line = null;
                String page = "";
                while((line = bufreader.readLine()) != null) {
                    page += line;
                }

                return page;
            } finally {
                conn.disconnect();
            }
        }

        private void displayStationpos(String result) {
            String stationName = "";
            String gpsX    = "";
            String gpsY    = "";
            String stationId = "";
            boolean bSet_stationName = false;
            boolean bSet_gpsX    = false;
            boolean bSet_gpsY    = false;
            boolean bSet_stationId = false;
            boolean success = false;

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_DOCUMENT) {

                    } else if(eventType == XmlPullParser.START_TAG) {
                        String tag_name = xpp.getName();

                        if(tag_name.equals("stationName")) bSet_stationName = true;
                        else if(tag_name.equals("stationId")) bSet_stationId = true;
                        else if(tag_name.equals("x")) bSet_gpsX = true;
                        else if(tag_name.equals("y")) bSet_gpsY = true;


                    } else if(eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();

                        if (bSet_gpsX) {
                            gpsX = content;
                            bSet_gpsX = false;
                        }
                        else if (bSet_gpsY) {
                            gpsY = content;
                            bSet_gpsY = false;
                        }
                        else if (bSet_stationName) {
                            stationName = content;
                            bSet_stationName = false;
                        }
                        else if (bSet_stationId) {
                            stationId = content;
                            bSet_stationId = false;
                        }

                    } else if(eventType == XmlPullParser.END_TAG) {

                        String end_tag_name = xpp.getName();
                        if (end_tag_name.equals("busStationAroundList")) {
                            if (stationId.equals(station_id)) {
                                success = true;
                            }
                            if(success ==  true) break;
                        }
                    }


                    eventType = xpp.next();


                }
                if(success) {
                    Log.e("AlarmAcitivity정류소 번호","일치");

                    int route_id = accessDB.selectRouteId(route_name, stationName);
                    accessDB.insertAlert(Integer.parseInt(stationId), stationName, route_id, route_name, position, activityName);

                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Alert");
                    DatabaseReference stationRef = rootRef.child(station_id);
                    DatabaseReference routeRef = stationRef.child(route_name);

                    onNotificationButtonClicked(routeRef);
                    Toast.makeText(getApplicationContext(), route_name+"번 버스에 탑승 요청을 하였습니다.", Toast.LENGTH_LONG).show();
                    Log.e("AlarmAcitivity","탑승 요청");
                    BusProvider.getInstance().post(new AlertItem(1, Integer.parseInt(stationId), stationName, route_id, route_name, position, activityName));

                } else {
                    Log.e("AlarmAcitivity정류소 번호","불일치");
                    Toast.makeText(getApplicationContext(), "탑승 요청에 실패하였습니다. 해당 정류소로 이동해주십시오.", Toast.LENGTH_LONG).show();
                    accessDB.deleteAllAlert();
                    finish();
                }
                success = false;
                Log.e("AlarmActivity","종료");
            } catch (Exception e) {
            }


        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("onConnected","시작");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000) ;
        locationRequest.setFastestInterval(1000) ;
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY) ;
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this) ;
        }
    }
    private void onNotificationButtonClicked(final DatabaseReference routeRef) {
        routeRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                long value;
                if(mutableData.getValue() == null) {
                    value = 0;
                    Log.e("Alarm value is null", value+"");
                } else {
                    value = (long) mutableData.getValue();
                }
                value++;

                Log.e("Alarm value", value + "");

                mutableData.setValue(value);

                finish();
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d("$$", ":onComplete:" + databaseError);
            }

        });

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            }
            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) { //권한이 부여되었는지 여부를 확인
        Log.e("알림","주변정류소앱 onRequestPermissionsResult");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) { //앱 권한이 있는 경우
//            Log.e("build","시작 전");
                        isNetWork();
                        accessDB = DBHelper.getInstance(getApplicationContext());
                        accessDB.connect();
                    } else { // 앱 권한이 없는 경우 PackageManager.PERMISSION_GDENIED를 반환.
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION); //권한요청. 비동기식 작동.
                        // 즉각 반환. 사용자가 대화상자에 응답한 후에 시스템은 그 결과를 가지고 앱의 콜백 메서드 호출
                    }
                } else { finish();

                }
            }
        }
    }

    private void isLocation() {
        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //gps 꺼져있을 때
            new AlertDialog.Builder(AlarmActivity.this)
                    .setMessage("GPS가 꺼져있습니다.\n ‘위치 서비스’에서 ‘Google 위치 서비스’를 체크해주세요")
                    .setPositiveButton("설정",new DialogInterface.OnClickListener() {
                        // 설정 창을 띄운다
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            }).show();
        }
        buildGoogleApiClient();

    }
    private void isNetWork() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        Log.e("This is","isNewWork method");
        if ((isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect)){
            Log.e("알림","데이터 가능");
            isLocation();
        }else{
            new AlertDialog.Builder(AlarmActivity.this)
                    .setTitle("데이터가 꺼져 있음")
                    .setMessage("데이터를 켜거나 Wi-Fi를 사용하십시오.")
                    .setPositiveButton("설정",new DialogInterface.OnClickListener() {
                        // 설정 창을 띄운다
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            }).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e("Log","Pause");
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e("Log","onResume");


    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("Log","onStart");
        // The activity is about to become visible.
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Log","onStop");
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("Log","onRestart");
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) { //앱 권한이 있는 경우
//            Log.e("build","시작 전");
            isNetWork();

            accessDB = DBHelper.getInstance(getApplicationContext());
            accessDB.connect();
        } else { // 앱 권한이 없는 경우 PackageManager.PERMISSION_GDENIED를 반환.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION); //권한요청. 비동기식 작동.
            // 즉각 반환. 사용자가 대화상자에 응답한 후에 시스템은 그 결과를 가지고 앱의 콜백 메서드 호출
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
