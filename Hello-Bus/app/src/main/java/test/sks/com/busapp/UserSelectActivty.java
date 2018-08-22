package test.sks.com.busapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class UserSelectActivty extends AppCompatActivity {

    private MainActivity mainActivity = (MainActivity) MainActivity.mainActivity;
    private DBHelper accessDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        setContentView(R.layout.activity_user_select_activty);
        setTitle("");
        accessDB = DBHelper.getInstance(getApplicationContext());

        Toast.makeText(getApplicationContext(), "사용자 모드를 선택하세요. 화면의 위쪽은 승객 모드, 아래쪽은 운전자 모드입니다.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
        mainActivity.finish();
    }

    public void btnSelectDriver(View view) {
        startActivity(new Intent(this, ModeActivity.class));
        accessDB.updateUserType("운전자");
    }
    public void btnSelectPassenger(View view) {
        startActivity(new Intent(this, ModeActivity.class));
        accessDB.updateUserType("승객");
    }
}
