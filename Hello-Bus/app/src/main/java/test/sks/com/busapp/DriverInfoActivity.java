package test.sks.com.busapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DriverInfoActivity extends AppCompatActivity {

    private EditText editTextRouteName, editTextPlateNo, editTextCity;
    private DBHelper accessDB;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("버스 정보 입력");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("버스 정보 입력");
        editTextRouteName = (EditText) findViewById(R.id.edit_txt_route_name); 
        editTextPlateNo = (EditText) findViewById(R.id.edit_txt_plate_no);
        editTextCity = (EditText) findViewById(R.id.edit_txt_city);

        accessDB = DBHelper.getInstance(getApplicationContext());

        String route_name = accessDB.selectDriverRouteName();
        String plate_no = accessDB.selectDriverPlateNo();

        editTextRouteName.setText(route_name);
        editTextPlateNo.setText(plate_no);
    }
    
    public void btnUpdateDriverInfo(View view) {
        String route_name = editTextRouteName.getText().toString();
        String plate_no = editTextPlateNo.getText().toString();
        String city = editTextCity.getText().toString();
        
        if(plate_no.equals("") && route_name.equals("")) {
            Toast.makeText(getApplicationContext(), "차량번호와 버스번호를 입력하세요", Toast.LENGTH_LONG).show();
        } else if(route_name.equals("")) {
            Toast.makeText(getApplicationContext(), "버스번호를 입력하세요", Toast.LENGTH_LONG).show();
        } else if(plate_no.equals("")) {
            Toast.makeText(getApplicationContext(), "차량번호를 입력하세요", Toast.LENGTH_LONG).show();
        } else if(city.equals("")) {
            Toast.makeText(getApplicationContext(), "차량번호를 입력하세요", Toast.LENGTH_LONG).show();
        } else {
            accessDB.updateDriverInfo(route_name, plate_no, city);
            Toast.makeText(getApplicationContext(), "운전자 정보를 업데이트하였습니다", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
