package net.woorisys.lighting.control.user.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import net.woorisys.lighting.control.user.R;
import net.woorisys.lighting.control.user.sjp.EditTextErrorCheck;
import net.woorisys.lighting.control.user.sjp.classmanagement.LightSetting;
import net.woorisys.lighting.control.user.sjp.observer.FragmentListener;
import net.woorisys.lighting.control.user.sjp.observer.FragmentValue;
import net.woorisys.lighting.control.user.sjp.usbManagement;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Setter;

public class DimmingSettingFragment extends Fragment {

    private final static String SharedPreferenceSP="DimmingSetting";

    //region UI
    @BindView(R.id.page_title)
    TextView pageTitle;
    @BindView(R.id.channel_id_sp)
    Spinner channel_sp;
    @BindView(R.id.et_maxLight)
    EditText et_maxLight;
    @BindView(R.id.et_minLight)
    EditText et_minLight;
    @BindView(R.id.et_onDemming)
    EditText et_onDemming;
    @BindView(R.id.et_offDeming)
    EditText et_offDemming;
    @BindView(R.id.et_maintainDemming)
    EditText et_maintainDemming;
    @BindView(R.id.btn_Setting_Send)
    Button btn_Setting_Send;
    @BindView(R.id.btn_channel_send)
    Button btn_Channel_Send;

    //endregion

    private final static String TAG="SJP_DIMMING_TAG";  //  Dimming Setting Fragment Tag

    private EditTextErrorCheck errorCheck;

    //아파트
    String[] channel_Id={"지하1층","지하2층","지하3층"};

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
        pageTitle.setText("디밍 설정");

        SJ_UISetting();

        return view;
    }

    private void SJ_UISetting()
    {

        errorCheck=new EditTextErrorCheck();

        ArrayAdapter<String> channel_id=new ArrayAdapter<>(getContext(),android.R.layout.simple_dropdown_item_1line,channel_Id);

        channel_sp.setAdapter(channel_id);


        btn_Setting_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingSend();
            }
        });

        SharedPreference();

        //동글 채널 설정 버튼클릭
        btn_Channel_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DongleChannelSetting();
            }
        });

    }

    // 설정 전송
    private void SettingSend()
    {
        String MAX=et_maxLight.getText().toString();
        String MIN=et_minLight.getText().toString();
        String ON=et_onDemming.getText().toString();
        String OFF=et_offDemming.getText().toString();
        String MainTain=et_maintainDemming.getText().toString();
        String Area="255";

        String ErrorMessage="";

        // 사용자 지정 Data 들은 NULL 있어선 안된다.
        if(!errorCheck.NullCheck(MAX,MIN,ON,OFF,MainTain).isEmpty())
        {
            ErrorMessage+="＊"+errorCheck.NullCheck(MAX,MIN,ON,OFF,MainTain)+"의 값이 없습니다.\n";
        }

        // 최소 밝기는 최대 밝기보다 크거나 같을 수 없다.
        if(!MAX.isEmpty()&& !MIN.isEmpty() && Integer.valueOf(MAX)<=Integer.valueOf(MIN))
        {
            ErrorMessage+="＊ 최소 밝기는 최대 밝기보다 크거나 같을 수 없습니다.\n";
        }

        // 지정된 최대 값을 넘을 수 없다.
        if(!errorCheck.MaxValue(MAX,MIN,ON,OFF,MainTain).isEmpty())
        {
            ErrorMessage+="＊"+errorCheck.MaxValue(MAX,MIN,ON,OFF,MainTain)+"을 최대값을 넘었습니다.\n";
        }

        if(!ErrorMessage.isEmpty())
        {
            int endIndex=ErrorMessage.lastIndexOf("\n");
            if(endIndex!=-1)
                ErrorMessage=ErrorMessage.substring(0,endIndex);

            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
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
        }
        else
        {
            // USB 로 Data 보내기
            int send_Max=Integer.valueOf(MAX);
            int send_Min=Integer.valueOf(MIN);
            int send_On=Integer.valueOf(ON);
            int send_Off=Integer.valueOf(OFF);
            int send_Maintain=Integer.valueOf(MainTain);


            LightSetting lightSetting=new LightSetting();
            lightSetting.setMaxLight(send_Max);
            lightSetting.setMinLight(send_Min);
            lightSetting.setMaintainLight(send_Maintain);
            lightSetting.setOnDimmingLight(send_On);
            lightSetting.setOffDimmingLight(send_Off);
            lightSetting.setSensitivityLight(2);

            Intent intent=new Intent(usbManagement.getAction_Dimming_Setting_Send_B());
            intent.putExtra(usbManagement.getDimmingSettingFragment_LightSetting(),  lightSetting);
            getActivity().sendBroadcast(intent);

        }
    }

    private final static String MAX="MAX";
    private final static String MIN="MIN";
    private final static String MAINTAIN="MAINTAIN";
    private final static String OFF="OFF";
    private final static String ON="ON";
    private final static String SQUENCE="SQUENCE";
    private final static String CHANNEL="CHANNEL";

    private void SharedPreference()
    {
        SharedPreferences sharedPreference=getContext().getSharedPreferences(SharedPreferenceSP, Context.MODE_PRIVATE);

        String MaxLight = sharedPreference.getString(MAX,"100");
        String MinLight = sharedPreference.getString(MIN,"20");
        String MaintainLight = sharedPreference.getString(MAINTAIN,"5");
        String OffLight = sharedPreference.getString(OFF,"2");
        String OnLight = sharedPreference.getString(ON,"1");

        et_maxLight.setText(MaxLight);
        et_minLight.setText(MinLight);
        et_maintainDemming.setText(MaintainLight);
        et_offDemming.setText(OffLight);
        et_onDemming.setText(OnLight);

    }


    @Override
    public void onStop() {
        super.onStop();
        SaveSharedPreference();
    }

    private void SaveSharedPreference()
    {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SharedPreferenceSP,Context.MODE_PRIVATE);

        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(MAX,et_maxLight.getText().toString());
        editor.putString(MIN,et_minLight.getText().toString());
        editor.putString(MAINTAIN,et_maintainDemming.getText().toString());
        editor.putString(OFF,et_offDemming.getText().toString());
        editor.putString(ON,et_onDemming.getText().toString());
        editor.putInt(CHANNEL,channel_sp.getSelectedItemPosition());

        editor.commit();
    }

    private void DongleChannelSetting()
    {
        String Mac="0001";
        String et_channel = "15";
        int sel_num=channel_sp.getSelectedItemPosition();

        //오피스텔
        if(sel_num == 0) // 지하1층
        {
            et_channel = "11";
        }else if(sel_num == 1) // 지하2층
        {
            et_channel = "15";
        }else if(sel_num == 2) // 지하3층
        {
            et_channel = "26";
        }

        Intent intent=new Intent(usbManagement.getAction_Maintenance_Dongle_Channel());
        intent.putExtra(usbManagement.getDongle_Channel(),et_channel);
        getActivity().sendBroadcast(intent);
        Log.d("JHLEE","channel :"+et_channel);
    }
}
