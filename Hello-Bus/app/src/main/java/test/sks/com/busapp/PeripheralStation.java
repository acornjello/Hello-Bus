package test.sks.com.busapp;

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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class PeripheralStation extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient; //fused location provider 와 Google Places API에 연결
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int DEFAULT_ZOOM = 15;
    private static final LatLng mDefaultLocation = new LatLng(37.56, 126.97);
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    double longitude;
    double latitude;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private PeripheralAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<PeripheralItem> peripheralItems;
    private PeripheralItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("주변 정류소");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("주변 정류소");

        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        Log.e("알림","주변정류소앱 ONCREATE");

        // 저장된 인스턴스 상태에서 위치 및 카메라 위치를 검색
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // 네트워크 연결 관리자 핸들 얻기

        isNetWork();
        isLocation();

        mRecyclerView = (RecyclerView) findViewById(R.id.ph_recyclerView);

        // 권한
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) { //앱 권한이 있는 경우
            mLocationPermissionGranted = true;
            build();


        } else { // 앱 권한이 없는 경우 PackageManager.PERMISSION_GDENIED를 반환.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION); //권한요청. 비동기식 작동.
            // 즉각 반환. 사용자가 대화상자에 응답한 후에 시스템은 그 결과를 가지고 앱의 콜백 메서드 호출
        }

    }
    public void btnGoHome(View v) {
        Intent intent = new Intent(this, ModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    private void build() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API).build();
        mGoogleApiClient.connect();
    }
    private void isLocation() {
        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //gps 꺼져있을 때
            new AlertDialog.Builder(PeripheralStation.this)
                    .setMessage("GPS가 꺼져있습니다.\n ‘위치 서비스’에서 ‘Google 위치 서비스’를 체크해주세요")
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
    private void isNetWork() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        Log.e("This is","isNewWork method");
        if ((isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect)){
            Log.e("알림","데이터 가능");
        }else{
            new AlertDialog.Builder(PeripheralStation.this)
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
    public void onMapReady(GoogleMap map) {
        Log.e("알림","주변정류소앱 onMapReady");
        mMap = map;

        updateLocationUI();
        getDeviceLocation();
    }

    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() { //권한이 있을 때만 UI가 보이도록.
        Log.e("알림","주변정류소앱 updateLocationUI");
        if (mMap == null) return;
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    //    @Override public void onResume() {
//        super.onResume();
//
//
//        //isNetWork();
//        //isLocation();
//
//        // 권한
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) { //앱 권한이 있는 경우
//            mLocationPermissionGranted = true;
//            updateLocationUI();
//            getDeviceLocation();
//
//
//        } else { // 앱 권한이 없는 경우 PackageManager.PERMISSION_GDENIED를 반환.
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION); //권한요청. 비동기식 작동.
//            // 즉각 반환. 사용자가 대화상자에 응답한 후에 시스템은 그 결과를 가지고 앱의 콜백 메서드 호출
//        }
//
//    }
    @SuppressWarnings("MissingPermission")
    private void getDeviceLocation() {
        Log.e("알림","주변정류소앱 getDeviceLocation");
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }
        latitude = 37.2881167;
        longitude = 127.0242833;

        parsing(latitude, longitude);

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                    new LatLng(mLastKnownLocation.getLatitude(),
//                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude,
                            longitude), DEFAULT_ZOOM));
        } else {
            //Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
                            displayMap(gpsX,gpsY,stationName);
                            //tv.append(gpsX + " ");
                            //tv.append(gpsY + " ");
                            //tv.append(stationName + "\n");

                        }
                    }
                    eventType = xpp.next();
                    mAdapter = new PeripheralAdapter(peripheralItems, 0, PeripheralStation.this);
                    mRecyclerView.setAdapter(mAdapter);
                }

            } catch (Exception e) {
                ;
            }
        }
    }


        private void displayMap(String gpsX, String gpsY, String stationName) {
            double b_latitude= Double.parseDouble(gpsY);
            double b_longitude  = Double.parseDouble(gpsX);
            final LatLng LOC = new LatLng(b_latitude, b_longitude);

            Marker mk = mMap.addMarker(new MarkerOptions()
                    .position(LOC)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title(stationName));
            mk.showInfoWindow();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e("알림","주변정류소앱 onConnected");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) { //권한이 부여되었는지 여부를 확인
        Log.e("알림","주변정류소앱 onRequestPermissionsResult");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;

                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .enableAutoManage(this,this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .addApi(Places.GEO_DATA_API)
                            .addApi(Places.PLACE_DETECTION_API).build();
                    mGoogleApiClient.connect();


                } else { finish();

                }
            }
        }
        updateLocationUI();
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
        Log.e("Log","Resume");
        if(mMap != null){ //prevent crashing if the map doesn't exist yet (eg. on starting activity)
            mMap.clear();

            // add markers from database to the map
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
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
}



//https://developers.google.com/maps/documentation/android-api/current-places-tutorial?hl=ko