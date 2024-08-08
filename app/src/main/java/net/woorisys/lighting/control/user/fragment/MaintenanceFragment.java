package net.woorisys.lighting.control.user.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import net.woorisys.lighting.control.user.R;
import net.woorisys.lighting.control.user.sjp.EditTextErrorCheck;
import net.woorisys.lighting.control.user.sjp.EditTextErrorCheckSingle;
import net.woorisys.lighting.control.user.sjp.classmanagement.MaintenanceSetting;
import net.woorisys.lighting.control.user.sjp.UsbManagement;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MaintenanceFragment extends Fragment {

    // Text MacId
    @BindView(R.id.et_macID)
    EditText macID;
    // On Button
    @BindView(R.id.btn_maintain_on)
    Button btn_maintain_on;
    // Off Button
    @BindView(R.id.btn_maintain_off)
    Button btn_maintain_off;
    // Setting
    @BindView(R.id.btn_Maintain_Setting)
    Button btn_maintain_setting;
    @BindView(R.id.et_Maintain_MaxLight)
    EditText et_Maintain_MaxLight;
    @BindView(R.id.et_Maintain_MinLight)
    EditText et_Maintain_MinLight;
    @BindView(R.id.et_Maintain_LightOn)
    EditText et_Maintain_OnLight;
    @BindView(R.id.et_Maintain_LightOff)
    EditText et_Maintain_OffLight;
    @BindView(R.id.et_Maintain_LightMaintain)
    EditText et_Maintain_MaintainLight;
    @BindView(R.id.sensitivity_level_sp)
    Spinner sp_Maintain_Sensitivity;
    @BindView(R.id.btn_Maintain_Single_Setting)
    Button btn_Maintain_Single_Setting;

    @BindView(R.id.minLightLayout)
    LinearLayout minLightLayout;
    @BindView(R.id.lightMaintainLayout)
    LinearLayout lightMaintainLayout;
    @BindView(R.id.lightOnLayout)
    LinearLayout lightOnLayout;
    @BindView(R.id.lightOffLayout)
    LinearLayout lightOffLayout;
    @BindView(R.id.levelLayout)
    LinearLayout levelLayout;

    private EditTextErrorCheckSingle errorCheck;

    public MaintenanceFragment() {
    }

    public static MaintenanceFragment newInstance() {
        return new MaintenanceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maintenance, container, false);
        ButterKnife.bind(this, view);

        errorCheck = new EditTextErrorCheckSingle();
        setUISetting();

        return view;
    }

    private void setUISetting() {
        String[] sensitivityLevel = {"0", "1", "2", "3", "4", "5", "6", "7"};

        ArrayAdapter<String> sensitivityLevelAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, sensitivityLevel);

        sp_Maintain_Sensitivity.setAdapter(sensitivityLevelAdapter);

        // On 버튼 클릭
        btn_maintain_on.setOnClickListener(v -> lightOn());

        // Off 버튼 클릭
        btn_maintain_off.setOnClickListener(v -> lightOff());

        // 설정 확인 버튼 클릭
        btn_maintain_setting.setOnClickListener(v -> settingConfirm());

        btn_Maintain_Single_Setting.setOnClickListener(v -> singleSetting());

        float dp = 0f;

        Point screenSize  = getScreenSize(getActivity());
        if (screenSize.y > 2400) {
            dp = 16;
        } else if (screenSize.y > 2000) {
            dp = 12;
        } else if (screenSize.y > 1600) {
            dp = 8;
        }  else if (screenSize.y > 1400) {
            dp = 4;
        } else {
            dp = 0;
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) minLightLayout.getLayoutParams();
        layoutParams.topMargin = dpToPx(dp);

        minLightLayout.setLayoutParams(layoutParams);
        lightMaintainLayout.setLayoutParams(layoutParams);
        lightOnLayout.setLayoutParams(layoutParams);
        lightOffLayout.setLayoutParams(layoutParams);

        layoutParams = (LinearLayout.LayoutParams) levelLayout.getLayoutParams();
        layoutParams.topMargin = dpToPx(dp + 4);
        layoutParams.bottomMargin = dpToPx(84);
        levelLayout.setLayoutParams(layoutParams);

//        Log.d("MaintenanceFragment", getScreenSize(getActivity()) + "," + dpToPx(dp));
    }

    private Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return  size;
    }

    private int dpToPx(float dp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
        return px;
    }

    private void lightOn() {
        String macIDValue = macID.getText().toString();
        if (!macIDValue.isEmpty()) {
            Intent intent = new Intent(UsbManagement.getAction_Maintenance_On());
            intent.putExtra(UsbManagement.getMacID(), macID.getText().toString());
            getActivity().sendBroadcast(intent);
        } else {
            EditTextErrorCheck textErrorCheck = new EditTextErrorCheck();
            textErrorCheck.ErrorAlertDialog(getContext(), "시리얼 번호 미입력", "시리얼 번호를 입력하여 주세요.");
        }
    }

    private void lightOff() {
        String macIDValue = macID.getText().toString();
        if (!macIDValue.isEmpty()) {
            Intent intent = new Intent(UsbManagement.getAction_Maintenance_Off());
            intent.putExtra(UsbManagement.getMacID(), macID.getText().toString());
            getActivity().sendBroadcast(intent);
        } else {
            EditTextErrorCheck textErrorCheck = new EditTextErrorCheck();
            textErrorCheck.ErrorAlertDialog(getContext(), "시리얼 번호 미입력", "시리얼 번호를 입력하여 주세요.");
        }
    }

    private void settingConfirm() {
        String macIDValue = macID.getText().toString();
        if (!macIDValue.isEmpty()) {
            Intent intent = new Intent(UsbManagement.getAction_Maintenance_Setting_Check());
            intent.putExtra(UsbManagement.getMacID(), macID.getText().toString());
            getActivity().sendBroadcast(intent);
        } else {
            EditTextErrorCheck textErrorCheck = new EditTextErrorCheck();
            textErrorCheck.ErrorAlertDialog(getContext(), "시리얼 번호 미입력", "시리얼 번호를 입력하여 주세요.");
        }
    }

    // 단일 설정 전송 버튼 Event
    private void singleSetting() {
        String MacId = macID.getText().toString();
        if (TextUtils.isEmpty(MacId))
            errorCheck.ErrorAlertDialog(getContext(), "시리얼 번호 없음", "시리얼 번호를 입력하여 주세요");
        else {
            String MAX = et_Maintain_MaxLight.getText().toString();
            String MIN = et_Maintain_MinLight.getText().toString();
            String ON = et_Maintain_OnLight.getText().toString();
            String OFF = et_Maintain_OffLight.getText().toString();
            String MainTain = et_Maintain_MaintainLight.getText().toString();
            String Sensitivity = sp_Maintain_Sensitivity.getSelectedItem().toString();

            String ErrorMessage = "";

            // 사용자 지정 Data 들은 NULL 있어선 안된다.
            if (!errorCheck.NullCheck(MAX, MIN, ON, OFF, MainTain).isEmpty()) {
                ErrorMessage += "＊" + errorCheck.NullCheck(MAX, MIN, ON, OFF, MainTain) + "의 값이 없습니다.\n";
            }

            // 최소 밝기는 최대 밝기보다 크거나 같을 수 없다.
            if (!MAX.isEmpty() && !MIN.isEmpty() && Integer.valueOf(MAX) <= Integer.valueOf(MIN)) {
                ErrorMessage += "＊최소 밝기는 최대 밝기보다 크거나 같을 수 없습니다.\n";
            }

            // 지정된 최대 값을 넘을 수 없다.
            if (!errorCheck.MaxValue(MAX, MIN, ON, OFF, MainTain).isEmpty()) {
                ErrorMessage += "＊" + errorCheck.MaxValue(MAX, MIN, ON, OFF, MainTain) + "가 최대값을 넘었습니다.\n";
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
                int send_Max = Integer.valueOf(MAX);
                int send_Min = Integer.valueOf(MIN);
                int send_On = Integer.valueOf(ON);
                int send_Off = Integer.valueOf(OFF);
                int send_Maintain = Integer.valueOf(MainTain);
                int send_Sensitivity = Integer.valueOf(Sensitivity);

                MaintenanceSetting maintenanceSetting = new MaintenanceSetting();
                maintenanceSetting.setMacID(MacId);
                maintenanceSetting.setMax(send_Max);
                maintenanceSetting.setMin(send_Min);
                maintenanceSetting.setOn(send_On);
                maintenanceSetting.setOff(send_Off);
                maintenanceSetting.setMaintain(send_Maintain);
                maintenanceSetting.setSensitivity(send_Sensitivity);

                Intent intent = new Intent(UsbManagement.getAction_Maintenance_Single_Setting_Send());
                intent.putExtra(UsbManagement.getMaintenanceFragment_SettingConfirm(), maintenanceSetting);
                getActivity().sendBroadcast(intent);
                Log.d("JHLEE", "maintenanceSetting :" + maintenanceSetting);
            }
        }
    }
}
