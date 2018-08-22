package test.sks.com.busapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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
import java.util.ArrayList;


public class PeripheralStationList extends AppCompatActivity implements
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
    String station_id;
    String route_name;
    private PeripheralAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<PeripheralItem> peripheralItems;
    private PeripheralItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_peripheral_list);
        //now_stationId = "201000264";
        Log.e("정류소리스트","정류소리스트 액티비티 시작");
        //tv = (TextView) findViewById(R.id.data);

        mRecyclerView = (RecyclerView) findViewById(R.id.ph_recyclerView);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){

                buildGoogleApiClient();
                //mymap.setMyLocationEnabled(true);
                Log.e("oncreate","권한있음");
            }
        }
        else{
            buildGoogleApiClient();
            //mymap.setMyLocationEnabled(true);
            Log.e("oncreate","권한없음");
        }
        Log.e("oncreate","권한 실패");
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

        mLastLocation = location ;
        if(mCurrLocationMarker != null){
            mCurrLocationMarker.remove();
        }

        //LatLng mLatLng = new LatLng(location.getLatitude(), location.getLongitude()) ;
        double latitude = 37.2881167;
        double longitude = 127.0242833;

        //LatLng mLatLng = new LatLng(latitude, longitude) ;

//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();

        Log.e("현재 latitude : ",latitude+" ");
        Log.e("현재 longitude : ",longitude+"\n");
        //LatLng mLatLng = new LatLng(latitude, longitude) ;
        //tv.append(latitude + " ");
        //tv.append(longitude + "\n");
        //mymap.addMarker(new MarkerOptions().position(mLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
/*
        MarkerOptions mMarkerOptions = new MarkerOptions() ;
        mMarkerOptions.position(mLatLng) ;
        mMarkerOptions.title("Your Current Location") ;
        mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) ;
*/
        //move map camera
        //mymap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15f));

        parsing(latitude, longitude);

        //stop location updates
        if(googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,  this);
        }
    }
    public void parsing(double latitude, double longitude) {
        peripheralItems = new ArrayList<>();
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
            String mobileNo = "";
            boolean bSet_stationName = false;
            boolean bSet_gpsX    = false;
            boolean bSet_gpsY    = false;
            boolean bSet_mobileNo = false;

            //tv.append("=== 파싱결과 ===\n");

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_DOCUMENT) {
                        ;
                    } else if(eventType == XmlPullParser.START_TAG) {
                        String tag_name = xpp.getName();
                        if (tag_name.equals("stationName"))
                            bSet_stationName = true;
                        if (tag_name.equals("mobileNo"))
                            bSet_mobileNo = true;
                        if (tag_name.equals("x"))
                            bSet_gpsX = true;
                        if (tag_name.equals("y"))
                            bSet_gpsY = true;
                    } else if(eventType == XmlPullParser.TEXT) {
                        /*
                        if(bSet_gpsX || bSet_gpsY || bSet_stationName) {
                            Log.e("CONTENT", xpp.getText());
                        }
                        **/
                        if (bSet_gpsX) {
                            gpsX = xpp.getText();
                            bSet_gpsX = false;
                        }
                        if (bSet_gpsY) {
                            gpsY = xpp.getText();
                            bSet_gpsY = false;
                        }
                        if (bSet_stationName) {
                            stationName = xpp.getText();
                            item = new PeripheralItem(null,null);
                            bSet_stationName = false;
                        }
                        if (bSet_mobileNo) {
                            mobileNo = xpp.getText();
                            bSet_mobileNo = false;
                        }

                    } else if(eventType == XmlPullParser.END_TAG) {

                        String end_tag_name = xpp.getName();
                        if (end_tag_name.equals("busStationAroundList")) {
                            item.setStation_nm(stationName);
                            item.setMobile_no(mobileNo);
                            Log.e("확인 name", item.getStation_nm());
                            Log.e("mobileNo", item.getMobile_no());
                            peripheralItems.add(item);
                            Log.e("CONTENT name", stationName);
                            Log.e("mobileNo",mobileNo);
                            Log.e("CONTENT x" ,  gpsX);
                            Log.e("CONTENT y", gpsY);
                            //displayMap(gpsX,gpsY,stationName);
                            //tv.append(gpsX + " ");
                            //tv.append(gpsY + " ");
                            //tv.append(stationName + "\n");

                        }
                    }
                    eventType = xpp.next();
                    mAdapter = new PeripheralAdapter(peripheralItems, 0, PeripheralStationList.this);
                    mRecyclerView.setAdapter(mAdapter);
                }

            } catch (Exception e) {
                ;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000) ;
        locationRequest.setFastestInterval(1000) ;
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY) ;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this) ;
        }
    }
    private void onNotificationButtonClicked(final DatabaseReference routeRef) {
        routeRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long value = (long) 0;
                String str = null;
                if(mutableData.getValue() != null) {
                    str = (String) mutableData.getValue();
                    Log.e("22222222222", str);
                    value = Long.parseLong(str);
                }
                value++;
                str = Long.toHexString(value);
                Log.e("value", str);

                mutableData.setValue(str);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_LOCATION :{

                // If request is cancelled, the result arrays are empty.

                if(grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    // permission was granted. Do the
                    // contacts-related task you need to do.

                    if(ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED){

                        if(googleApiClient == null){

                            buildGoogleApiClient();
                        }
                        //mymap.setMyLocationEnabled(true);
                    }
                }
                else{
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return ;
            }
        }
    }
}

