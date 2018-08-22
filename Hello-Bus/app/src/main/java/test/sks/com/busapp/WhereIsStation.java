package test.sks.com.busapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class WhereIsStation extends AppCompatActivity implements OnMapReadyCallback,
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

    private String positionX;
    private String positionY;
    private String rcvMobileNo;
    private String rcvStationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("정류소 위치");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("정류소 위치");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        LinearLayout layoutMap = (LinearLayout) findViewById(R.id.layout_map);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height-60);
        layoutMap.setLayoutParams(layoutParams);
        //layoutMap.setMinimumHeight(height-60);



        positionX = (String) getIntent().getExtras().get("positionX");
        positionY = (String) getIntent().getExtras().get("positionY");
        rcvMobileNo = (String) getIntent().getExtras().get("rcvMobileNo");
        rcvStationName = (String) getIntent().getExtras().get("rcvStationName");
        Log.e("알림", "맵뷰 ONCREATE");


        // 저장된 인스턴스 상태에서 위치 및 카메라 위치를 검색
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // 네트워크 연결 관리자 핸들 얻기

        isNetWork();

        build();

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
            new AlertDialog.Builder(WhereIsStation.this)
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

    }

    @SuppressWarnings("MissingPermission")
    private void getDeviceLocation() {
        Log.e("알림","주변정류소앱 getDeviceLocation");
        Log.e("위치",positionY+" ");
        Log.e("위치",positionX+" ");
        LatLng mtLocation = new LatLng(Double.parseDouble(positionY),Double.parseDouble(positionX));


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mtLocation, DEFAULT_ZOOM));
        mMap.addMarker(new MarkerOptions()
                .title(rcvStationName + "(" + rcvMobileNo + ")")
                .position(mtLocation));


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


}



//https://developers.google.com/maps/documentation/android-api/current-places-tutorial?hl=ko