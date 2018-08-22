package test.sks.com.busapp;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InformationActivity extends Activity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("경기 버스 도움말");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryText));
        setTitle("경기 버스 도움말");

        ImageButton btnHome = (ImageButton) findViewById(R.id.btn_home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ModeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expandablelist);

        // preparing list data
        prepareListData();

        listAdapter = new BaseExpandableAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("버스 검색");
        listDataHeader.add("정류소 검색");
        listDataHeader.add("주변 정류소");
        listDataHeader.add("즐겨찾기");
        listDataHeader.add("탑승 알림");
        listDataHeader.add("도착 알람");
        listDataHeader.add("사용자 모드 변경");
        listDataHeader.add("운전자 화면");

        // Adding child data
        List<String> bus_search = new ArrayList<String>();
        bus_search.add("노선번호를 입력하여 노선의 기점, 종점 정보와 운행시간 및 배차 정보를 제공합니다. " +
                "노선 정보를 조회한 후에는 노선의 실시간 버스 위치와 운행구간 노선도를 확인할 수 있습니다.");

        List<String> station_search = new ArrayList<String>();
        station_search.add("정류소명이나 정류소번호를 입력하여 정류소의 위치와 정류소를 경유하는 노선정보를 제공합니다.\n" +
                "정류소 정보를 조회한 후에는 정류소를 경유하는 노선들의 도착예측 시간과 노선별 상세정보를 확인할 수 있습니다.");

        List<String> peripheral = new ArrayList<String>();
        peripheral.add("사용자의 현재 위치를 기준으로 주변의 정류소 정보를 제공합니다.\n 조회 후 정류소를 선택하면 지도 상의 정류소 위치를 표시합니다.\n" +
                "정류소 항목을 누르면, 해당 정류소를 경유하는 버스들의 도착정보를 볼 수 있습니다.");

        List<String> favorite = new ArrayList<String>();
        favorite.add("자주 이용하는 정류소 혹은 버스를 대상으로 편리성을 제공합니다.\n" +
                "버스 탭은 즐겨찾기한 버스를 보여줍니다.\n 정류소 탭은 즐겨찾기한 정류소 혹은 특정 정류소의 버스를 보여줍니다.");


        List<String> alarm_1 = new ArrayList<String>();
        alarm_1.add("1. 정보에는 정류소, 버스, 도착 시간이 있습니다.");
        alarm_1.add("2. 버스가 곧 진입중이거나 지나간 경우에는 진동과 함께 상단바를 통해 알림이 옵니다.");
        alarm_1.add("3. 상단바 알림을 누르면 탑승 알림 메뉴로 이동합니다.");
        alarm_1.add("4. 탑승 알림 요청은 해당 정류소 근처에 있을 경우만 가능합니다.");
        alarm_1.add("5. 탑승 알림을 취소하고 싶으시다면, 탑승 알림 메뉴에 있는 버튼이나, 해당 정류소의 버스에 있는 탑승 요청버튼을 다시 누르십시오.");


        List<String> alarm_2 = new ArrayList<String>();
        alarm_2.add("1. 도착 알람은 버스가 언제 오는지 알려주는 기능을 합니다.");
        alarm_2.add("2. 버스가 곧 진입중이거나 지나간 경우에는 진동과 함께 상단바를 통해 알림이 옵니다.");
        alarm_2.add("3. 상단바 알림을 누르면 해당 버스의 도착 정보만 나오는 정류소 화면으로 이동합니다.");
        alarm_2.add("4. 도착 알람을 취소하고 싶으시다면, 정류소 화면으로 이동하여 도착 알림 버튼을 다시 누르십시오.");
        alarm_2.add("5. 상단바를 통해 이동하면 보다 쉽게 취소하실 수 있습니다.");

        List<String> userchange = new ArrayList<String>();
        userchange.add("메뉴 화면에서 오른쪽 상단에 있는 버튼을 클릭하여 사용자 모드를 전환할 수 있습니다.\n" +
                "버스 운전자이시라면 운전자 모드를, 탑승객이시라면 승객모드를 이용하여 주세요.");

        List<String> driver = new ArrayList<String>();
        driver.add("1. 버스 번호와 차량 번호를 입력하여 정보를 저장합니다.");
        driver.add("2. 다음 정류소에 탑승할 시각장애인의 수가 표시될 것입니다.");
        driver.add("3. 탑승할 시각장애인이 있다면 빨간색으로 원으로 강조되어 쉽게 확인할 수 있습니다.");

        listDataChild.put(listDataHeader.get(0), bus_search); // Header, Child data
        listDataChild.put(listDataHeader.get(1), station_search);
        listDataChild.put(listDataHeader.get(2), peripheral);
        listDataChild.put(listDataHeader.get(3), favorite);
        listDataChild.put(listDataHeader.get(4), alarm_1);
        listDataChild.put(listDataHeader.get(5), alarm_2);
        listDataChild.put(listDataHeader.get(6), userchange);
        listDataChild.put(listDataHeader.get(7), driver);
    }

}