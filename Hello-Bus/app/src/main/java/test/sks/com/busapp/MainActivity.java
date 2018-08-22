package test.sks.com.busapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static Activity mainActivity;
    private StorageReference mStorageRef;
    private StorageReference dbFileRef;
    private File dirPath;
    private File filePath;
    private DBHelper accessDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = MainActivity.this;

        setTitle("");
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://memoapp-5c391.appspot.com");  // Firebase 저장소를 가리킴
        dbFileRef = mStorageRef.child("BusDB.sqlite");  // 다운받을 파일을 가리킴
        dirPath = new File(Environment.getDataDirectory().getAbsolutePath() + "/data/" + this.getPackageName() + "/databases");    // 파일을 저장할 dir 경로
        filePath = new File(dirPath + "/BusDB.sqlite"); // 파일의 경로


//        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("VERSION");

    }
//    private ValueEventListener valueEventListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            String newVersion = dataSnapshot.getValue(String.class);
//            String curVersion = VersionManager.getAppVersionName(activity);
//            VersionManager.checkVer(activity, curVersion, newVersion);
//            VersionManager.setNewVersionName(newVersion);
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//
//        }
//    };

    private void isNetWork() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        Log.e("This is","isNewWork method");
        if ((isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect)){
            Log.e("알림","데이터 가능");
            downloadFirebaseFile();
        }else{
            Log.e("알림","데이터 불가능");
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("데이터가 꺼져 있음")
                    .setMessage("데이터를 켜거나 Wi-Fi를 사용하십시오.")
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
    }

    public void downloadFirebaseFile() {


        if (!filePath.exists()) {
            final File dbFile = new File(dirPath, "BusDB.sqlite");

            dbFileRef.getFile(dbFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("###", "DB DOWNLOAD SUCCESS");
//                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
                    accessDB = DBHelper.getInstance(getApplicationContext());
                    accessDB.connect();
                    //accessDB.updateUserType("");

                    if ( accessDB.selectUserType().equals("승객") ) {
                        startActivity(new Intent(MainActivity.this, ModeActivity.class));
                        finish();
                    } else if ( accessDB.selectUserType().equals("운전자") ) {
                        startActivity(new Intent(MainActivity.this, ModeActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(MainActivity.this, UserSelectActivty.class));
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("###", "DB DOWNLOAD FAIL");
//                    Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_LONG).show();
                }
            });

        } else {
//            Toast.makeText(getApplicationContext(), "already exist", Toast.LENGTH_LONG).show();
            accessDB = DBHelper.getInstance(getApplicationContext());
            accessDB.connect();
            //accessDB.updateUserType("");
            try {
                Thread.sleep(3000);
            } catch (Exception e) {}

             if ( accessDB.selectUserType().equals("승객") ) {
                startActivity(new Intent(MainActivity.this, ModeActivity.class));
                finish();
            } else if ( accessDB.selectUserType().equals("운전자") ) {
                startActivity(new Intent(MainActivity.this, ModeActivity.class));
                finish();
            } else {
                startActivity(new Intent(MainActivity.this, UserSelectActivty.class));
            }
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

        Log.e("Log","Resume");

        isNetWork();

//        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://memoapp-5c391.appspot.com");  // Firebase 저장소를 가리킴
//        dbFileRef = mStorageRef.child("BusDB.sqlite");  // 다운받을 파일을 가리킴
//        dirPath = new File(Environment.getDataDirectory().getAbsolutePath() + "/data/" + this.getPackageName() + "/databases");    // 파일을 저장할 dir 경로
//        filePath = new File(dirPath + "/BusDB.sqlite"); // 파일의 경로

 //       downloadFirebaseFile();
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
        Log.e("Log","onStart");
        isNetWork();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
