package net.woorisys.lighting.control.user.sjp.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import net.woorisys.lighting.control.user.sjp.usb.CP2101.CP210XConstants;
import net.woorisys.lighting.control.user.sjp.usb.CP2101.CP210XUSBDeviceManager;
import net.woorisys.lighting.control.user.sjp.usb.cdcam.CDCACMContstants;
import net.woorisys.lighting.control.user.sjp.usb.cdcam.CDCACMUSBDeviceManager;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.BoardLEDOnOffPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.ChannelSetPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.Config2RequestPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.ConfigPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.ConfigRequestPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.DongCannelPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.GroupDeletePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.GroupPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.GroupRequestPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.InterruptGroupSetting;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.InterruptSetPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.LEDGroupEnablePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.LEDOffPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.LEDOnPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.RSSIRequestPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.RequestPDUBase;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.SectionIDPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.Config2ResponsePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.ConfigResponsePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.GroupResponsePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.NormalResponsePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.RSSIResponsePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.ResponseInterruptState;
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.ResponsePDUBase;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.DongleChannelSetPDU;
import net.woorisys.lighting.control.user.sjp.usb.util.NumberUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

public class DefaultUSBDeviceManager extends AbstractUSBDeviceManager {

	private final static String TAG="SJP_DEFAULT_TAG";

	private boolean debug = false;
	
	private AbstractUSBDeviceManager usbDeviceManager;
	private Handler handler = null;
	
	public DefaultUSBDeviceManager(Context context, Handler handler, UsbManager usbManager) {
		this.usbManager = usbManager;
		this.handler = handler;
	}
	
	@Override
	public void close() {
		if (usbDeviceManager != null) {
			usbDeviceManager.close();
		}
	}

	@Override
	public boolean initDevice(Context context, UsbManager usbManager,
                              UsbDevice device, SerialSettings serialSettings)

			throws USBTerminalException {
		Log.d(TAG,"DEVIDE VID : "+device.getVendorId());

		System.out.println("VID = " + device.getVendorId());

		if (device.getVendorId() == CP210XConstants.CP2102_VENDOR_ID) {
			usbDeviceManager = new CP210XUSBDeviceManager(context, handler, usbManager);
		} else if (device.getVendorId() == CDCACMContstants.TI2530_VENDOR_ID) {
			usbDeviceManager = new CDCACMUSBDeviceManager(context, handler, usbManager);
		} else if (0x02 == device.getInterface(0).getInterfaceClass()
				&& 0x02 == device.getInterface(0).getInterfaceSubclass()) {
			usbDeviceManager = new CDCACMUSBDeviceManager(context, handler, usbManager);
		}

		return usbDeviceManager.initDevice(context, usbManager, device, serialSettings);
	}

	@Override
	public ModemControlLineStatus getModemStatus() {
		return usbDeviceManager.getModemStatus();
	}

	@Override
	public int getBaudrate() {
		return usbDeviceManager.getBaudrate();
	}

	@Override
	public boolean setBaudrate(int baudRateSettings) {
		return usbDeviceManager.setBaudrate(baudRateSettings);
	}

	@Override
	public int getLineControl() {
		return usbDeviceManager.getLineControl();
	}

	@Override
	public boolean setLineControl(float stopBits, String parity, int dataBits)
			throws USBTerminalException {
		return usbDeviceManager.setLineControl(stopBits, parity, dataBits);
	}

	@Override
	public int[] getFlowControl() {
		return usbDeviceManager.getFlowControl();
	}

	@Override
	public boolean setFlowControl(String mode) {
		return usbDeviceManager.setFlowControl(mode);
	}

	@Override
	public boolean setModemControl(boolean dtrState, boolean rtsState) {
		return usbDeviceManager.setModemControl(dtrState, rtsState);
	}

	@Override
	public void send(String message) {
		usbDeviceManager.send(message);
	}

	@Override
	public void send(byte[] message) {
		usbDeviceManager.send(message);
	}

	@Override
	public byte[] recv() {
		return usbDeviceManager.recv();
	}
	
	@Override
	public int recv(byte[] recvBuffer) {
		return usbDeviceManager.recv(recvBuffer);
	}
	
	/**
	 * 수정사항: 2014-02-27 입력으로 들어온 값을 16진수로 변환한다.
	 * 
	 * 인자로 주어진 장비의 MAC Address 문자열에서 장비 주소를 생성한다.
	 * 장비  MAC Address 에서 하위 4 자리수만 장비 주소로 사용한다.
	 * 
	 * @param mac 장비의 MAC Address
	 * @return MAC 주소가 유효하지 않는 형태이니 경우 NULL을 리턴한다.
	 */
	private String getAddress(String mac) {
		System.out.println("mac ===> " + mac);
		mac = NumberUtil.convert2Hex(mac);
		if (mac.length() < 4) {
			System.out.println("Invalid mac address '" + mac + "'");
			return null;
		}

		String id = mac.substring(mac.length() - 4);
		System.out.println("Device id is '" + id + "'");

		return id;
	}

	/**
	 * 인자로 주어진 노드에 LED ON을 요청한다.
	 * 
	 * @param mac 장비 mac(주어진 mac에서 하위 4 자리수만 장비 주소로 사용한다.)
	 * @param transferType 전송 형태(UNICAST / BROADCAST)
	 * @return
	 * @throws IOException
	 */
	public boolean sendOn(String mac, String transferType) throws IOException {
		String id = getAddress(mac);

		LEDOnPDU pdu = new LEDOnPDU(id, transferType);
		ResponsePDUBase response = sendCommand(pdu);

		if(null == response)
			return false;
		
		return response.getResult();
	}

	/**
	 * 인자로 주어진 노드에 LED OFF을 요청한다.
	 * 
	 * @param mac 장비 mac(주어진 mac에서 하위 4 자리수만 장비 주소로 사용한다.)
	 * @param transferType 전송 형태(UNICAST / BROADCAST)
	 * @return
	 * @throws IOException
	 */
	public boolean sendOff(String mac, String transferType) throws IOException {
		String id = getAddress(mac);
		if (null == id)
			return false;

		LEDOffPDU pdu = new LEDOffPDU(id, transferType);
		ResponsePDUBase response = sendCommand(pdu);

		if(null == response)
			return false;
		
		return response.getResult();
	}

	/**
	 * 인자로 주어진 노드에 구역 설정을 요청한다.
	 * 
	 * @param mac 장비 mac(주어진 mac에서 하위 4 자리수만 장비 주소로 사용한다.)
	 * @param transferType 전송 형태(UNICAST / BROADCAST)
	 * @param sectionId 구역 ID(1 ~ 5)
	 * @return
	 * @throws IOException
	 */
	public boolean sendSectionID(String mac, String transferType, String sectionId) throws IOException {
		String id = getAddress(mac);
		if (null == id)
			return false;

		SectionIDPDU pdu = new SectionIDPDU(id, transferType, sectionId);
		ResponsePDUBase response = sendCommand(pdu);

		if(null == response)
			return false;
		
		return response.getResult();
	}
	
	/**
	 * 인자로 주어진 채널에 채널 설정을 요청한다.
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	public boolean sendDongChannel(String channel) throws IOException {
		DongCannelPDU pdu = new DongCannelPDU(channel);
		ResponsePDUBase response = sendCommand(pdu);

		if(null == response)
			return false;
		
		return response.getResult();
	}
	
	/**
	 * 보드 LED 설정을 요청한다.
	 * @return
	 * @throws IOException
	 */
	public boolean sendBoardLED(String mac, String transferType, String use) throws IOException {
		String id = getAddress(mac);
		if (id == null)
			return false;
		
		BoardLEDOnOffPDU pdu = new BoardLEDOnOffPDU(id, transferType, use);
		ResponsePDUBase response = sendCommand(pdu);

		return response.getResult();
	}
	
	/**
	 * 보드 채널 설정을 요청한다.
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	public boolean sendChannelSet(String mac, String transferType, String channel) throws IOException {
		String id = getAddress(mac);
		if (id == null)
			return false;
		
		ChannelSetPDU pdu = new ChannelSetPDU(id, transferType, channel);
		ResponsePDUBase response = sendCommand(pdu);

		return response.getResult();
	}
	
	/**
	 * 인터럽트 설정을 요청한다.
	 * @return
	 * @throws IOException
	 */
	public boolean sendInterruptSet(String mac, String transferType, String use, String time) throws IOException {
		String id = getAddress(mac);
		if (id == null)
			return false;
		
		InterruptSetPDU pdu = new InterruptSetPDU(id, transferType, use, time);
		Log.d(TAG,"PDU : "+pdu);
		ResponsePDUBase response = sendCommand(pdu);
		Log.d(TAG,"PDU RESPONSE"+response);
		return response.getResult();
	}



	/**
	 * 인자로 주어진 장비의 설정 정보를 조회를 요청한다.
	 * 
	 * @param mac
	 * @return 응답 PDU, 명령 처리 실패 시 null 리턴
	 */
	public Config2ResponsePDU sendLED2ConfigRequest(String mac) {
		String id = getAddress(mac);
		if (id == null)
			return null;

		Config2RequestPDU pdu = new Config2RequestPDU(id);

		return (Config2ResponsePDU) sendCommand(pdu);
	}

	public ResponseInterruptState senInterrupGroupSetting(String mac)
	{
		String id=getAddress(mac);
		if(id==null)
			return null;

		InterruptGroupSetting pdu=new InterruptGroupSetting(id);
		Log.d(TAG,"PDU : "+pdu);
		return (ResponseInterruptState) sendCommand(pdu);
	}


	/**
	 * 인자로 주어진 노드에 UNICAST로 설정을 요청한다.
	 * 
	 * @param mac 장비 mac(주어진 mac에서 하위 4 자리수만 장비 주소로 사용한다.)
	 * @param maxBrightness 점등 시 조명 밝기(%)
	 * @param standByBrightness 대기 시 조명 밝기(%)
	 * @param autoOffDelayTime 센서가 감지되지 않은 후 디밍으로 들어가기까지의 시간(초)
	 * @param onDimmingDelay 전등 켜기 디밍에 소요되는 시간(초)
	 * @param offDimmingDelay 전등 끄기 디밍에 소요되는 시간(초)
	 * @param sensitivity  감도
	 * @param useBroadcastSectionFilter 구역 내 브로드캐스트 사용 여부
	 * @return
	 * @throws IOException
	 */
	public boolean sendConfigUnicast(String mac, int maxBrightness, int standByBrightness, int autoOffDelayTime,
                                     int onDimmingDelay, int offDimmingDelay, int sensitivity, boolean useBroadcastSectionFilter)
					throws IOException {
		String id = getAddress(mac);
		if (null == id)
			return false;

		ConfigPDU pdu = new ConfigPDU(id, maxBrightness, standByBrightness,
				autoOffDelayTime, onDimmingDelay, offDimmingDelay, sensitivity, useBroadcastSectionFilter);
		ResponsePDUBase response = sendCommand(pdu);

		if(null == response)
			return false;
		
		return response.getResult();
	}

	/**
	 * 인자로 주어진 노드에 BROADCAST로 설정을 요청한다.
	 * 
	 * @param mac 장비 mac(주어진 mac에서 하위 4 자리수만 장비 주소로 사용한다.)
	 * @param sectionID 구역 ID
	 * @param maxBrightness 점등 시 조명 밝기(%)
	 * @param standByBrightness 대기 시 조명 밝기(%)
	 * @param autoOffDelayTime 센서가 감지되지 않은 후 디밍으로 들어가기까지의 시간(초)
	 * @param onDimmingDelay 전등 켜기 디밍에 소요되는 시간(초)
	 * @param offDimmingDelay 전등 끄기 디밍에 소요되는 시간(초)
	 * @param sensitivity  감도
	 * @param useBroadcastSectionFilter 구역 내 브로드캐스트 사용 여부
	 * @return
	 * @throws IOException
	 */
	public boolean sendConfigBroadcast(String mac, String sectionID, int squenceNum, int maxBrightness, int standByBrightness,
                                       int autoOffDelayTime, int onDimmingDelay, int offDimmingDelay, int sensitivity,
                                       boolean useBroadcastSectionFilter) throws IOException {
		String id = getAddress(mac);
		if (null == id)
			return false;

		ConfigPDU pdu = new ConfigPDU(id, sectionID, squenceNum, maxBrightness, standByBrightness,
				autoOffDelayTime, onDimmingDelay, offDimmingDelay, sensitivity, useBroadcastSectionFilter);

		ResponsePDUBase response = sendCommand(pdu);

		if(null == response)
			return false;
		
		return response.getResult();
	}

	public boolean GroupSendEnable(String mac,int enable)
	{
		String id=getAddress(mac);
		if(null==id)
			return  false;
		LEDGroupEnablePDU pdu=new LEDGroupEnablePDU(id,enable);

		ResponsePDUBase response = sendCommand(pdu);

		Log.d(TAG,"PDU : "+pdu+" / "+response);
		if(null==response)
			return false;

		return response.getResult();

	}
	/**
	 * 인자로 주어진 장비의 설정 정보를 조회를 요청한다.
	 * 
	 * @param mac
	 * @return 응답 PDU, 명령 처리 실패 시 null 리턴
	 */
	public ConfigResponsePDU sendConfigRequest(String mac) {
		String id = getAddress(mac);
		if (null == id)
			return null;

		ConfigRequestPDU pdu = new ConfigRequestPDU(id);
		return (ConfigResponsePDU) sendCommand(pdu);
	}	

	/**
	 * 인자로 주어진 노드에 Broadcast로 그룹 설정을 요청한다.
	 * 
	 * @param mac 장비 mac(주어진 mac에서 하위 4 자리수만 장비 주소로 사용한다.)
	 * @param groupItemMacs 그룹에 속한 장비의 MAC 목록
	 * @return
	 */
	public boolean sendGroup(String mac, String transferType, String sectionId, String[] groupItemMacs) {
		String id = getAddress(mac);
		if (null == id)
			return false;

		String[] groupItems = new String[groupItemMacs.length];

		for (int i = 0; i < groupItemMacs.length; i++) {
			String groupItemMac = groupItemMacs[i];
			String itemId = getAddress(groupItemMac);
			if (null == itemId)
				return false;

			groupItems[i] = itemId;
		}

		GroupPDU pdu = new GroupPDU(id, transferType, sectionId, groupItems);
		ResponsePDUBase response = sendCommand(pdu);

		if(null == response)
			return false;
		
		return response.getResult();
	}

	/**
	 * 인자로 주어진 장비의 그룹 정보를 조회를 요청한다.
	 * 
	 * @param mac 조회 대상 장비 MAC Address
	 * @return 응답 PDU, 명령 처리 실패 시 null 리턴
	 */
	public GroupResponsePDU sendGroupRequest(String mac) {
		String id = getAddress(mac);
		if (null == id)
			return null;

		GroupRequestPDU pdu = new GroupRequestPDU(id);
		return (GroupResponsePDU) sendCommand(pdu);
	}

	/**
	 * 인자로 주어진 장비의 그룹 정보 삭제를 요청한다.
	 * 
	 * @param mac 조회 대상 장비 MAC Address
	 * @param transferType 전송 형태(UNICAST / BROADCAST)
	 * 
	 * @return 응답 PDU, 명령 처리 실패 시 null 리턴
	 */
	public boolean sendGroupDelete(String mac, String transferType) {
		String id = getAddress(mac);
		if (null == id)
			return false;

		GroupDeletePDU pdu = new GroupDeletePDU(id, transferType);
		ResponsePDUBase response = sendCommand(pdu);

		if(null == response)
			return false;
		
		return response.getResult();
	}


	/**
	 * 동글로 부터 가장 가까운 노드들의 조회를 요청한다.
	 * 
	 * @return 응답 PDU, 명령 처리 실패 시 null 리턴
	 */
	public RSSIResponsePDU sendRSSIRequest() {
		RSSIRequestPDU pdu = new RSSIRequestPDU();
		return (RSSIResponsePDU) sendCommand(pdu);
	}

	/**
	 * 요청 메시지를 전송하고 응답 메시지를 수신하여 리턴한다.
	 * 
	 * @param pdu 요청 PDU
	 * @return
	 */
	public ResponsePDUBase sendCommand(RequestPDUBase pdu) {
		Log.d("pdu", pdu.getCommandID());
		String response = sendCommand(pdu.encode());
		String commandID = pdu.getCommandID();
				if(RequestPDUBase.COMMAND_ID_ON.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_OFF.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_SECTION_ID.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_CONFIG.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_GROUP.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_GROUP_DELETE.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_DONG_CHANNEL.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_GROUP_ENABLE.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_GROUP_DISABLE.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_DIM_OFF_SET.equals(commandID)) {
			NormalResponsePDU resPdu = new NormalResponsePDU();
			if (resPdu.decode(response))
				return resPdu;
		} else if (RequestPDUBase.COMMAND_ID_CONFIG_REQ.equals(commandID)) {
			ConfigResponsePDU resPdu = new ConfigResponsePDU();

			if (resPdu.decode(response))
				return resPdu;
		} else if (RequestPDUBase.COMMAND_ID_GROUP_REQ.equals(commandID)) {
			GroupResponsePDU resPdu = new GroupResponsePDU();

			if (resPdu.decode(response))
				return resPdu;
		} else if (RequestPDUBase.COMMAND_ID_RSSI_REQ.equals(commandID)) {
			RSSIResponsePDU resPdu = new RSSIResponsePDU();

			if (resPdu.decode(response))
				return resPdu;
		}else if (RequestPDUBase.COMMAND_ID_CONFIG2_REQ.equals(commandID)) {
			Config2ResponsePDU resPdu = new Config2ResponsePDU();
			if (resPdu.decode(response))
				return resPdu;
		}else if(RequestPDUBase.COMMAND_ID_GROUP_STATE_RES.equals(commandID))
		{
			ResponseInterruptState responseInterruptState=new ResponseInterruptState();
			if(responseInterruptState.decode(response))
				return responseInterruptState;
		}

		System.out.println("Invalid pdu: " + response);
		return null;
	}

	/**
	 * 명령어를 전송하고 응답을 수신한다.
	 * 
	 *            8자리 mac, 실제 전송 시에는 하위 4바이트만을 사용한다.
	 * @return
	 */
	synchronized public String sendCommand(final String command) {
		if (debug) {
			if (command.contains("RSSI_REQ")) {
				return "SUCCESS,RSSI_RES,2,A001,A002;";
			} else if (command.contains("GROUP_REQ")) {
				return "SUCCESS,GROUP_RES,2,A002,A003;";
			} else if (command.contains("CONFIG_REQ")) {
				return "SUCCESS,CONFIG_RES,90,20,10,6,6,3,OFF,1;";
			}
			return "SUCCESS;";
		} else {
			Log.d("pdu", command);
			byte[] send = command.getBytes();
			Log.d("pdu", send.length + "");
			send(send);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int ret = recv(recvBuffer);
			if (ret < 0) {
				Log.e("Send Command3", "센서가 응답하지 않습니다");
				Message message = handler.obtainMessage();
				message.what = USBConstants.WHAT_DEVICE_TIME_OUT;
				handler.sendMessage(message);
				return "";
			}

			Log.e("Received bytes", Arrays.toString(recvBuffer));
			if (ret != 0) {
				String str = new String(recvBuffer, 0, ret);
				Log.e("Received String ", str);
				if(str.contains(";"))
				{
					for(int i=0;i<recvBuffer.length;i++) recvBuffer[i] = 0;
					String[] arrayStr = null;
					arrayStr = str.split(";");
					return skipBlankLines(arrayStr[0]+";");
				}
			}
			return "";
		}
	}

	/**
	 * 동글 채널 설정을 요청한다.
	 * @return
	 * @throws IOException
	 */
	public boolean sendDongleChannelSet(String mac, String transferType, String count) throws IOException {
		String id = getAddress(mac);
		if (id == null)
			return false;

		DongleChannelSetPDU pdu = new DongleChannelSetPDU(id, transferType, count);
		Log.d(TAG,"PDU : "+pdu);
		ResponsePDUBase response = sendCommand(pdu);
		Log.d(TAG,"PDU RESPONSE : "+response.getResult());
		return response.getResult();
	}

	/**
	 * 인자로 주어진 문자열에서 공백라인을 제거하여 리턴한다. WiFi 모듈과 인터페이스 문제로 공백을 모두 제거하지 못하는 경우가 있음
	 * 
	 * @param source
	 * @return
	 */
	private String skipBlankLines(String source) {
		if (null == source)
			return null;

		BufferedReader reader = new BufferedReader(new StringReader(source));
		StringBuilder sb = new StringBuilder();

		while (true) {
			try {
				String line = reader.readLine();
				if (null == line)
					break;

				line = line.trim();
				if (0 == line.length())
					continue;

				if (0 != sb.length())
					sb.append("\n");
				sb.append(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("NORMAL:" + sb.toString());
		return sb.toString();
	}
}
