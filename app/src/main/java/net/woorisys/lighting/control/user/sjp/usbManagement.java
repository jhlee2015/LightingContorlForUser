package net.woorisys.lighting.control.user.sjp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import net.woorisys.lighting.control.user.sjp.classmanagement.LightSetting;
import net.woorisys.lighting.control.user.sjp.classmanagement.MaintenanceSetting;
import net.woorisys.lighting.control.user.sjp.observer.FragmentListener;
import net.woorisys.lighting.control.user.sjp.observer.FragmentValue;
import net.woorisys.lighting.control.user.sjp.usb.DefaultUSBDeviceManager;
import net.woorisys.lighting.control.user.sjp.usb.SerialSettings;
import net.woorisys.lighting.control.user.sjp.usb.USBTerminalException;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.RequestPDUBase;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.ConfigResponsePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.GroupResponsePDU;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class usbManagement extends BroadcastReceiver {

    private final static String TAG="SJP_USB_BR_TAG";

    /** 장비 일괄 설정 전체 대상 장비 개수 */
    private int totalDevices = 0;
    /** Progress 진행을 메인 쓰레드에 알리기위하여 사용하는 상수 값 */
    private static final int WHAT_LOAD_GROUP_CSV_INC = 20;
    /** Progress 종료를 메인 쓰레드에 알리기위하여 사용하는 상수 값 */
    private static final int WHAT_LOAD_GROUP_CSV_END = 21;

    /** 디밍 설정 **/
    @Getter// 설정 전송
    private final static String Action_Dimming_Setting_Send_B="com.android.psj.light.control.dimming.setting.send";
    @Getter// 그룹 동작제어 - Enable
    private final static String Action_Group_DImming_Enable_B="com.android.psj.setting.dimming.enable";
    @Getter// 그룹 동작제어 - Disable
    private final static String Action_Group_Dimming_Disable_B="com.android.psj.setting.dimming.disable";

    /** 그룹 설정 **/
    @Getter  // 그룹 설정 - 그룹 전송
    private final static String Action_GroupSetting_Send="com.android.psj.setting.group.send";
    @Getter  // 그룹 설정 - 전체 그룹 삭제
    private final static String Action_Group_Delete="com.android.psj.setting.group.delete";

    /**유지 보수**/
    @Getter //유지보수 - ON
    private final static String Action_Maintenance_On="com.android.psj.setting.maintenance.on";
    @Getter //유지보수 - OFF
    private final static String Action_Maintenance_Off="com.android.psj.setting.maintenance.off";
    @Getter //유지보수 - 설정 확인
    private final static String Action_Maintenance_Setting_Check="com.android.psj.setting.maintenance.check";
    @Getter //유지보수 - 그룹 확인
    private final static String Action_Maintenance_Group_Check="com.android.psj.setting.maintenance.group.check";

    @Getter //동글 채널 - 동글 채널 설정
    private final static String Action_Maintenance_Dongle_Channel="com.android.psj.setting.maintenance.dongle.channel";

    // USB 분리
    @Getter
    private final static String Action_Usb_Detached=UsbManager.ACTION_USB_DEVICE_DETACHED;
    // USB 연결
    @Getter
    private final static String Action_Usb_Init="com.android.psj.usb.init";

    @Getter
    // 유지보수 - 단일 설정 전송
    private final static String Action_Maintenance_Single_Setting_Send="com.android.psj.setting.maintenance.single.send";
    // 유지보수 - 단일 그룹 전송
    @Getter
    private final static String Action_Maintenance_Single_Group_Setting="com.android.psj.setting.maintenance.single.group";
    // 인터럽트 - 인터럽트 불러오기
    @Getter
    private final static String Action_Interrupt_Setting_Load="com.android.psj.setting.interrupt.load";
    // 인터럽트 - 인터럽트 설정
    @Getter
    private final static String Action_Interrupt_Setting="com.android.psj.setting.interrupt";
    // 인터럽트 - 그룹동작 확인
    @Getter
    private final static String Action_Interrupt_Group_Setting_Check="com.android.psj.group.setting.check";


    /** 디밍 설정 **/
    @Getter
    private final static String DimmingSettingFragment_LightSetting="dimming_setting_light_setting";

    /** 그룹 설정 **/
    @Getter
    private final static String GroupSettingFragment_GroupSetting="group_setting_group_setting";

    /** 유지 보수 **/
    @Getter
    private final static String MaintenanceFragment_SettingConfirm="maintenance_setting_confirm";
    @Getter
    private final static String Maintain_SendValue="maintenance_send";
    @Getter
    private final static String Maintain_AreaID="maintenance_area";

    @Getter
    private final static String Interrupt_Time="interruptTime";

    /** 공통 **/
    @Getter
    private final static String MacID="MACID";


    @Getter
    private final static String Dongle_Channel="dongleChannel";

    // USB 통신에 필요한 Class
    private static DefaultUSBDeviceManager usbDeviceManager;
    private UsbManager usbManager;
    private SerialSettings serialSettings;

    @Setter
    FragmentListener listener;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String Action = intent.getAction();

        USBInit(context);

        switch (Action)
        {
            case Action_Maintenance_Dongle_Channel:
                String Dongle_Channel_Value =intent.getStringExtra(Dongle_Channel);
                DongleChannelSetting(Dongle_Channel_Value,context);
                break;

            // region 디밍 설정
            case Action_Dimming_Setting_Send_B:

                LightSetting lightSetting= (LightSetting) intent.getExtras().getSerializable(DimmingSettingFragment_LightSetting);

                DimmingSetting(lightSetting.getMaxLight(),lightSetting.getMinLight(),lightSetting.getMaintainLight()
                                ,lightSetting.getOnDimmingLight(),lightSetting.getOffDimmingLight(),lightSetting.getSensitivityLight(), context);
                break;
             //endregion

            // region 디밍 설정 - enable button
            case Action_Group_DImming_Enable_B:

                GroupEnable();
                break;
            //endregion

            // region 디밍 설정 - disable button
            case Action_Group_Dimming_Disable_B:
                GroupDisable();
                break;
            // endregion


            //region 유지보수 - On
            case Action_Maintenance_On:

                String MacIDOn=intent.getStringExtra(MacID);

                MaintainLightOn(MacIDOn);

                break;
            //endregion

            //region 유지보수 - Off
            case Action_Maintenance_Off:
                String MacIDOff=intent.getStringExtra(MacID);
                MaintainLightOff(MacIDOff);
                break;
            //endregion

            //region 유지보수 - 설정 확인
            case Action_Maintenance_Setting_Check:
                String MacId_SettingCheck=intent.getStringExtra(MacID);
                MaintainSettingConfirm(MacId_SettingCheck,context);
                break;
            //endregion

            //region 유지보수 - 단일 설정 전송
            case Action_Maintenance_Single_Setting_Send:

                MaintenanceSetting maintenanceSetting= (MaintenanceSetting) intent.getExtras().getSerializable(MaintenanceFragment_SettingConfirm);

                String MacID_Maintain=maintenanceSetting.getMacID();
                String AreaValue_Maintain=maintenanceSetting.getArea();
                int MaxValue_Maintain=maintenanceSetting.getMax();
                int MinValue_Maintain=maintenanceSetting.getMin();
                int OnValue_Maintain=maintenanceSetting.getOn();
                int OffValue_Maintain=maintenanceSetting.getOff();
                int MaintainValue_Maintain=maintenanceSetting.getMaintain();
                int SensitivityValue_Maintain=maintenanceSetting.getSensitivity();

                SingleSetting(MacID_Maintain,MaxValue_Maintain,MinValue_Maintain,MaintainValue_Maintain,OnValue_Maintain,OffValue_Maintain,SensitivityValue_Maintain);

                break;
             //endregion

            case Action_Maintenance_Single_Group_Setting:
                String Mac=intent.getStringExtra(MacID);
                List<String> SendValue= (List<String>) intent.getExtras().getSerializable(Maintain_SendValue);
                String Area=intent.getStringExtra(Maintain_AreaID);

                SingleGroupSetting(Mac,Area,SendValue.toArray(new String[SendValue.size()]));
                Log.d(TAG,"MAC ID : "+Mac +", "+SendValue+" , "+Area);
                break;

            // USB Device 와 분리
            case Action_Usb_Detached:

                try
                {
                    System.runFinalization();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                catch (WindowManager.BadTokenException e)
                {
                    Log.e(TAG,"BadTokenException Error Detached USB : "+e.getMessage());
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                break;

            case Action_Usb_Init:
                USBInit(context);
                break;
        }
    }

    // USB 관련 명령어들
    private void USBInit(Context context)
    {
        usbManager=(UsbManager)context.getSystemService(Context.USB_SERVICE);


        if(usbManager!=null && usbDeviceManager==null)
        {
            usbDeviceManager=new DefaultUSBDeviceManager(context,handler,usbManager);
            HashMap<String, UsbDevice> deviceList=usbManager.getDeviceList();
            Set key = deviceList.keySet();

            try
            {
                for (Iterator iterator = key.iterator(); iterator.hasNext();) {
                    String keyName = (String) iterator.next();
                    UsbDevice valueName = deviceList.get(keyName);
                    prepareDevice(valueName,context);
                }
            }
            catch (NullPointerException e)
            {
                Toast.makeText(context,"USB 연결을 확인하여 주세요.",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
        }
    }

    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private boolean prepareDevice(UsbDevice device,Context context) {
        serialSettings = new SerialSettings();
        serialSettings.baudRate = 115200;
        serialSettings.dataBits = 8;
        serialSettings.handShaking = "None";
        serialSettings.parity = "None";
        serialSettings.stopBits = 1;

        try {

            usbDeviceManager.initDevice(context, usbManager, device, serialSettings);
        } catch (USBTerminalException e) {
            Log.e(TAG,"ERROR : "+e.getMessage());
        }
        catch (NullPointerException e)
        {
            Log.e(TAG,"ERROR : "+e.getMessage());
        }
        return true;
    }

    // 디밍 설정 Fragment 의 설정 전송 버튼 클릭시 동작
    private void DimmingSetting (int MaxValue,int MinValue,int MaintainValue,int OnValue,int OffValue , int SensitivityValue, Context context)
    {
        try
        {
            if (usbDeviceManager.sendConfigUnicast(RequestPDUBase.ADDRESS_BCAST, MaxValue,MinValue, MaintainValue,OnValue, OffValue, SensitivityValue, true ))
            {
                listener.Result(FragmentValue.DimmingSetting,true,"요청이 정상적으로 처리되었습니다.");
            }
            else {
                listener.Result(FragmentValue.DimmingSetting,false,"요청이 정상적으로 처리되지 않았습니다.");
            }
        }
        catch (IOException e)
        {
            listener.Result(FragmentValue.DimmingSetting,false,e.getMessage());
        }
        catch (NullPointerException e)
        {
            listener.Result(FragmentValue.DimmingSetting,false,e.getMessage());
        }
    }

    // 디밍 설정 ,  그룹 설정 Fragment 의 그룹 동작 제어에 Enable 버튼 클릭시 동작
    private void GroupEnable()
    {
        Log.d(TAG,"Group Enable");
        try
        {
            if(usbDeviceManager.GroupSendEnable(RequestPDUBase.ADDRESS_BCAST,1))
            {
                listener.Result(FragmentValue.DimmingSetting,true,"요청이 정상적으로 처리되었습니다.");
            }
            else {
                listener.Result(FragmentValue.DimmingSetting,false,"요청이 정상적으로 처리되지 않았습니다.");
            }
        }
        catch (NullPointerException e)
        {
            listener.Result(FragmentValue.DimmingSetting,false,e.getMessage());
        }
    }

    // 디밍 설정 Fragment 의 그룹 동작 제어에 Disable 버튼 클릭시 동작
    private void GroupDisable()
    {
        Log.d(TAG,"GroupDisable");
        try
        {
            if(usbDeviceManager.GroupSendEnable(RequestPDUBase.ADDRESS_BCAST,0))
            {
                listener.Result(FragmentValue.DimmingSetting,true,"요청이 정상적으로 처리되었습니다.");
            }
            else {listener.Result(FragmentValue.DimmingSetting,false,"요청이 정상적으로 처리되지 않았습니다.");}
        }
        catch (NullPointerException e)
        {
            listener.Result(FragmentValue.DimmingSetting,false,e.getMessage());
        }
    }

    // 유지 보수 On
    private void MaintainLightOn(String MacID)
    {
        try
        {
            boolean successOn = false;
            successOn = usbDeviceManager.sendOn(MacID, RequestPDUBase.TRANSFER_TYPE_UNICAST);

            if (successOn) {
                listener.Result(FragmentValue.Maintenance,true,"요청이 정상적으로 처리되었습니다.");
            } else {
                listener.Result(FragmentValue.Maintenance,false,"요청이 정상적으로 처리되지 않았습니다.");
            }
        }
        catch (IOException e)
        {
            listener.Result(FragmentValue.Maintenance,false,e.getMessage());
        }
        catch (NullPointerException e)
        {
            listener.Result(FragmentValue.Maintenance,false,e.getMessage());

        }
    }

    // 유지 보수 Off
    private void MaintainLightOff(String MacID)
    {
        try
        {
            boolean successOff = false;
            successOff = usbDeviceManager.sendOff(MacID, RequestPDUBase.TRANSFER_TYPE_UNICAST );

            if (successOff) {
                listener.Result(FragmentValue.Maintenance,true,"요청이 정상적으로 처리되었습니다.");
            } else {
                listener.Result(FragmentValue.Maintenance,false,"요청이 정상적으로 처리되지 않았습니다.");
            }
        }
        catch (IOException e)
        {
            listener.Result(FragmentValue.Maintenance,false,e.getMessage());
        }
        catch (NullPointerException e)
        { listener.Result(FragmentValue.Maintenance,false,e.getMessage());}
    }

    // 유지보수 설정 확인
    private void MaintainSettingConfirm(String MacID,Context context)
    {
        try
        {
            ConfigResponsePDU config2RequestPDU=usbDeviceManager.sendConfigRequest(MacID);
            if(config2RequestPDU!=null && config2RequestPDU.getResult())
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle(MacID+" 설정 정보");
                builder.setMessage(config2RequestPDU.toString());
                builder.setCancelable(false);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialog.cancel();
                    }
                }).show();
            }else
            {
                Log.d(TAG,"CONFIG : "+config2RequestPDU);
                listener.Result(FragmentValue.Maintenance,false,"요청이 정상적으로 처리되지 않았습니다.");
            }
        }
        catch (NullPointerException e)
        {
            listener.Result(FragmentValue.Maintenance,false,e.getMessage());
        }
    }

    // 유지보수 그룹 확인
    private void MaintainGroupConfirm(String MacID,Context context)
    {
        GroupResponsePDU groupRes = usbDeviceManager.sendGroupRequest(MacID);

        try
        {
            if (groupRes != null && groupRes.getResult()) {
                if (groupRes.getGroupItems().length > 0) {
                    if( groupRes!=null)
                    {

                        ArrayAdapter<String> groupadapter=new ArrayAdapter<>(context,android.R.layout.simple_list_item_1);


                        String[] recvData = groupRes.getGroupItems();
                        if(recvData!=null)
                        {
                            for(int i=0; i<recvData.length;i++)
                            {
                                Log.d(TAG,"RECV DATA : "+recvData[i]);
                                groupadapter.add(recvData[i]);
                            }

                            AlertDialog.Builder builder=new AlertDialog.Builder(context);
                            builder.setAdapter(groupadapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    dialog.cancel();
                                }
                            });
                            builder.setTitle(MacID+" 의 그룹");
                            builder.show();
                        }
                        else
                        {
                            listener.Result(FragmentValue.Maintenance,false,"수집에 실패하였습니다.");
                            return;
                        }
                    }
                    else {
                        listener.Result(FragmentValue.Maintenance,false,"수집에 실패하였습니다.");
                        return;
                    }
                } else {
                    listener.Result(FragmentValue.Maintenance,true,"수집에 성공하였습니다.\n설정된 그룹이 없습니다.");
                }
            }
        }
        catch (NullPointerException e)
        {
            listener.Result(FragmentValue.Maintenance,false,e.getMessage());
        }
    }

    // 유지보수 Fragment 의 단일 설정 전송 버튼
    private void SingleSetting(String MacID,int MaxValue,int MinValue,int MaintainValue,int OnValue,int OffValue , int SensitivityValue)
    {
        boolean success=false;

        try {
            success = usbDeviceManager.sendConfigUnicast(MacID, MaxValue, MinValue, MaintainValue,
                    OnValue, OffValue, SensitivityValue, true);

            if (success) {
                listener.Result(FragmentValue.Maintenance,true,"요청이 정상적으로 처리되었습니다.");
            } else {
                listener.Result(FragmentValue.Maintenance,false,"요청이 정상적으로 처리되지 않았습니다.");
            }

        } catch (IOException e) {
            listener.Result(FragmentValue.Maintenance,false,e.getMessage());
        }
        catch (NullPointerException e)
        {
            listener.Result(FragmentValue.Maintenance,false,e.getMessage());
        }
    }

    // 유지보수 Fragment 의 단일 그룹 설정 버튼
    private void SingleGroupSetting(String MacID,String AreaValue,String[] SendValue)
    {
        try
        {
            if (usbDeviceManager.sendGroup(MacID,RequestPDUBase.TRANSFER_TYPE_UNICAST, AreaValue, SendValue)) {
                listener.Result(FragmentValue.Maintenance,true,"요청이 정상적으로 처리되었습니다.");
            } else {
                listener.Result(FragmentValue.Maintenance,false,"요청이 정상적으로 처리되지 않았습니다.");
            }
        }
        catch (NullPointerException e)
        {
            listener.Result(FragmentValue.Maintenance,false,e.getMessage());
        }
    }

    private void DongleChannelSetting(String channel,Context context)
    {
        try {
            if (usbDeviceManager.sendDongleChannelSet("0001", RequestPDUBase.TRANSFER_TYPE_UNICAST, channel)) {
                Toast.makeText(context, "동글채널 설정 성공", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "동글채널 설정이 정상적으로 처리되지 않았습니다.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.d(TAG,"ERROR : "+e.getMessage());
        }
        catch (NullPointerException e)
        {
            Log.d(TAG,"ERROR : "+e.getMessage());
        }
    }


}
