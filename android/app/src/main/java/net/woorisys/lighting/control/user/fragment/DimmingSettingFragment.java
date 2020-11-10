package net.woorisys.lighting.control.user.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.woorisys.lighting.control.user.R;
import net.woorisys.lighting.control.user.adapter.CityAdapter;
import net.woorisys.lighting.control.user.adapter.FloorAdapter;
import net.woorisys.lighting.control.user.api.ApiInterface;
import net.woorisys.lighting.control.user.api.HttpClient;
import net.woorisys.lighting.control.user.domain.City;
import net.woorisys.lighting.control.user.domain.Floor;
import net.woorisys.lighting.control.user.manager.PreferenceManager;
import net.woorisys.lighting.control.user.sjp.EditTextErrorCheck;
import net.woorisys.lighting.control.user.sjp.classmanagement.LightSetting;
import net.woorisys.lighting.control.user.sjp.UsbManagement;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DimmingSettingFragment extends Fragment {

    @BindView(R.id.et_maxLight)
    EditText maxLightEdit;
    @BindView(R.id.et_minLight)
    EditText minLightEdit;
    @BindView(R.id.et_onDemming)
    EditText onDemmingEdit;
    @BindView(R.id.et_offDeming)
    EditText offDemmingEdit;
    @BindView(R.id.et_maintainDemming)
    EditText maintainDemmingEdit;
    @BindView(R.id.btn_Setting_Send)
    Button settingSendBtn;
    @BindView(R.id.btn_channel_send)
    Button channelSendBtn;
    @BindView(R.id.channel_id_sp)
    Spinner channelSpinner;

    private EditTextErrorCheck errorCheck;

    private final static String SharedPreferenceSP = "DimmingSetting";
    private final static String MAX = "MAX";
    private final static String MIN = "MIN";
    private final static String MAINTAIN = "MAINTAIN";
    private final static String OFF = "OFF";
    private final static String ON = "ON";
    private final static String SQUENCE = "SQUENCE";
    private final static String CHANNEL = "CHANNEL";

    public static DimmingSettingFragment newInstance() {
        return new DimmingSettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_demming_setting, container, false);
        ButterKnife.bind(this, view);

        getFloorList();
        setUISetting();

        return view;
    }

    private void getFloorList() {
        long apartmentId = PreferenceManager.getLong(getContext(), "apartmentId");
        if (apartmentId != -1L) {
            ApiInterface api = HttpClient.getRetrofit().create(ApiInterface.class);
            api.getFloorList(apartmentId).enqueue(new Callback<List<Floor>>() {
                @Override
                public void onResponse(Call<List<Floor>> call, Response<List<Floor>> response) {
                    List<Floor> floors = response.body();
                    FloorAdapter floorAdapter = new FloorAdapter(getContext(), floors);
                    channelSpinner.setAdapter(floorAdapter);
                }

                @Override
                public void onFailure(Call<List<Floor>> call, Throwable t) {
                    Log.d("LoginActivity", t.getMessage());
                    Toast.makeText(getContext(), "층 데이터 조회를 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setUISetting() {
        errorCheck = new EditTextErrorCheck();

        settingSendBtn.setOnClickListener(v -> settingSend());

        getSharedPreference();

        //동글 채널 설정 버튼클릭
        channelSendBtn.setOnClickListener(v -> dongleChannelSetting());
    }

    // 설정 전송
    private void settingSend() {
        String MAX = maxLightEdit.getText().toString();
        String MIN = minLightEdit.getText().toString();
        String ON = onDemmingEdit.getText().toString();
        String OFF = offDemmingEdit.getText().toString();
        String MainTain = maintainDemmingEdit.getText().toString();
        String Area = "255";

        String ErrorMessage = "";

        // 사용자 지정 Data 들은 NULL 있어선 안된다.
        if (!errorCheck.NullCheck(MAX, MIN, ON, OFF, MainTain).isEmpty()) {
            ErrorMessage += "＊" + errorCheck.NullCheck(MAX, MIN, ON, OFF, MainTain) + "의 값이 없습니다.\n";
        }

        // 최소 밝기는 최대 밝기보다 크거나 같을 수 없다.
        if (!MAX.isEmpty() && !MIN.isEmpty() && Integer.valueOf(MAX) <= Integer.valueOf(MIN)) {
            ErrorMessage += "＊ 최소 밝기는 최대 밝기보다 크거나 같을 수 없습니다.\n";
        }

        // 지정된 최대 값을 넘을 수 없다.
        if (!errorCheck.MaxValue(MAX, MIN, ON, OFF, MainTain).isEmpty()) {
            ErrorMessage += "＊" + errorCheck.MaxValue(MAX, MIN, ON, OFF, MainTain) + "을 최대값을 넘었습니다.\n";
        }

        if (!ErrorMessage.isEmpty()) {
            int endIndex = ErrorMessage.lastIndexOf("\n");
            if (endIndex != -1)
                ErrorMessage = ErrorMessage.substring(0, endIndex);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("ERROR");
            builder.setMessage(ErrorMessage);
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
            // USB 로 Data 보내기
            int sendMax = Integer.valueOf(MAX);
            int sendMin = Integer.valueOf(MIN);
            int sendOn = Integer.valueOf(ON);
            int sendOff = Integer.valueOf(OFF);
            int sendMaintain = Integer.valueOf(MainTain);


            LightSetting lightSetting = new LightSetting();
            lightSetting.setMaxLight(sendMax);
            lightSetting.setMinLight(sendMin);
            lightSetting.setMaintainLight(sendMaintain);
            lightSetting.setOnDimmingLight(sendOn);
            lightSetting.setOffDimmingLight(sendOff);
            lightSetting.setSensitivityLight(2);

            Intent intent = new Intent(UsbManagement.getAction_Dimming_Setting_Send_B());
            intent.putExtra(UsbManagement.getDimmingSettingFragment_LightSetting(), lightSetting);
            getActivity().sendBroadcast(intent);
        }
    }

    private void getSharedPreference() {
        SharedPreferences sharedPreference = getContext().getSharedPreferences(SharedPreferenceSP, Context.MODE_PRIVATE);
        String MaxLight = sharedPreference.getString(MAX, "100");
        String MinLight = sharedPreference.getString(MIN, "20");
        String MaintainLight = sharedPreference.getString(MAINTAIN, "5");
        String OffLight = sharedPreference.getString(OFF, "2");
        String OnLight = sharedPreference.getString(ON, "1");

        maxLightEdit.setText(MaxLight);
        minLightEdit.setText(MinLight);
        onDemmingEdit.setText(OnLight);
        offDemmingEdit.setText(OffLight);
        maintainDemmingEdit.setText(MaintainLight);
    }

    private void saveSharedPreference() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SharedPreferenceSP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MAX, maxLightEdit.getText().toString());
        editor.putString(MIN, minLightEdit.getText().toString());
        editor.putString(MAINTAIN, maintainDemmingEdit.getText().toString());
        editor.putString(OFF, offDemmingEdit.getText().toString());
        editor.putString(ON, onDemmingEdit.getText().toString());

        Floor floor = (Floor) channelSpinner.getSelectedItem();
        editor.putInt(CHANNEL, floor.getChannel());
        editor.commit();
    }

    private void dongleChannelSetting() {
        Floor floor = (Floor) channelSpinner.getSelectedItem();
        String channel = String.valueOf(floor.getChannel());

        Toast.makeText(getContext(), "채널 : " + channel, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(UsbManagement.getAction_Maintenance_Dongle_Channel());
        intent.putExtra(UsbManagement.getDongle_Channel(), channel);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        saveSharedPreference();
    }
}
