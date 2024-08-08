package net.woorisys.lighting.control.user;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import net.woorisys.lighting.control.user.fragment.DimmingSettingFragment;
import net.woorisys.lighting.control.user.fragment.MaintenanceFragment;
import net.woorisys.lighting.control.user.manager.PreferenceManager;
import net.woorisys.lighting.control.user.sjp.EditTextErrorCheck;
import net.woorisys.lighting.control.user.sjp.RememberData;
import net.woorisys.lighting.control.user.sjp.UsbManagement;
import net.woorisys.lighting.control.user.sjp.observer.FragmentListener;
import net.woorisys.lighting.control.user.sjp.observer.FragmentValue;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentListener {

    //Log Tag 구분 하기 위한 String
    private final static String TAG = "MainActivity";

    @BindView(R.id.page_title)
    TextView pageTitleText;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    //@BindView(R.id.nav_view)
    //NavigationView navigationView;
    @BindView(R.id.txt_Path)
    TextView txt_FilePath_Whole;
    @BindView(R.id.txt_Path_)
    TextView getTxt_FilePath_Whole;

    // 생성한 Broadcast Action 동작 시키기 위한 BroadcastReceiver 등록
    private UsbManagement broadcastReceiver;

    private IntentFilter intentFilter;

    private long backPressedTime = 0;
    private final long FINISH_INTERVAL_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        setSupportActionBar(toolbar);
        setupUI();
    }

    private void setupUI() {
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();

        //navigationView.setNavigationItemSelectedListener(this);

        //region IntentFilter
        // USB 동작 관련 BroadcastReceiver
        intentFilter = new IntentFilter();
        // Fragment (디밍 설정)
        intentFilter.addAction(UsbManagement.getAction_Dimming_Setting_Send_B());       //  설정 전송

        // Fragment (유지보수)
        intentFilter.addAction(UsbManagement.getAction_Maintenance_On());               //  전등 On
        intentFilter.addAction(UsbManagement.getAction_Maintenance_Off());              //  전등 Off
        intentFilter.addAction(UsbManagement.getAction_Maintenance_Setting_Check());    //  설정 확인
        intentFilter.addAction(UsbManagement.getAction_Maintenance_Single_Setting_Send()); //단일 설정
        intentFilter.addAction(UsbManagement.getAction_Maintenance_Dongle_Channel()); //동글 설정

        // 기타
        intentFilter.addAction(UsbManagement.getAction_Usb_Detached());                 //  Usb 분리
        intentFilter.addAction(UsbManagement.getAction_Usb_Init());                     //  Usb Initialize
        intentFilter.addAction(UsbManagement.getAction_Group_DImming_Enable_B());
        intentFilter.addAction(UsbManagement.getAction_Group_Dimming_Disable_B());
        //endregion

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        BottomNavigationView navigation = findViewById(R.id.navigation);
//        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        fragmentTransaction.add(R.id.fragment_container, DimmingSettingFragment.newInstance()).commit();
        pageTitleText.setText("디밍설정");

        String RememberPath = RememberData.getInstance().getSavefilepath().toString();
        if (RememberPath == "NULL" || RememberPath.equals("NULL")) {
            txt_FilePath_Whole.setText("");
        }

//        // CSV 선택 버튼 Event
//        btnSearch.setVisibility(View.VISIBLE);
//        btnSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent =new Intent(getApplicationContext(), SearchActivity.class);
//                startActivityForResult(intent,100);
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new UsbManagement();
            broadcastReceiver.setListener(this);
            registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            Log.d(TAG, "End Broadcast Receiver In BaseActivity, UnregisterReceiver");
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 100) {
            getTxt_FilePath_Whole.setVisibility(View.VISIBLE);
            txt_FilePath_Whole.setVisibility(View.VISIBLE);
            txt_FilePath_Whole.setText(RememberData.getInstance().getSavefilepath().getName());
        }
    }

    @Override
    public void Result(FragmentValue fragmentValue, boolean Result, String Message) {
        Log.d(TAG, "FRAGMENT : " + fragmentValue + " / Result : " + Result + " / MESSAGE : " + Message);
        if (!Result) {
            EditTextErrorCheck editTextErrorCheck = new EditTextErrorCheck();
            editTextErrorCheck.ErrorAlertDialog(MainActivity.this, fragmentValue + " Error", "응답이 없습니다. 채널 또는 아이디를 확인 하세요");
        } else {
            EditTextErrorCheck editTextErrorCheck = new EditTextErrorCheck();
            if (fragmentValue == FragmentValue.DimmingSetting) {
                editTextErrorCheck.ErrorAlerWaittDialog(MainActivity.this, fragmentValue + " Success", Message);
            } else {
                editTextErrorCheck.ErrorAlertDialog(MainActivity.this, fragmentValue + " Success", Message);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPressedTime;
            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                finishAffinity();
                super.onBackPressed();
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(), R.string.press_back_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        int id = item.getItemId();
        if (id == R.id.menu_logout) {
            Toast.makeText(getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            finish();
        }

        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.tab1:
                    replaceFragment(DimmingSettingFragment.newInstance());
                    pageTitleText.setText("디밍 설정");
                    return true;
                case R.id.tab2:
                    replaceFragment(MaintenanceFragment.newInstance());
                    pageTitleText.setText("개별 설정");
                    return true;
//                case R.id.tab3:
//                    replaceFragment(MaintenanceFragment.newInstance());
//                    pageTitle.setText("유지보수");
//                    return true;
//                case R.id.tab4:
//                    replaceFragment(InterruptFragment.newInstance());
//                    pageTitle.setText("인터럽트");
//                    return true;
            }
            return false;
        }
    };

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
    }
}