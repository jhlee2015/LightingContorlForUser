package net.woorisys.lighting.control.user.sjp.usb.cdcam;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import net.woorisys.lighting.control.user.sjp.usb.AbstractUSBDeviceManager;
import net.woorisys.lighting.control.user.sjp.usb.ModemControlLineStatus;
import net.woorisys.lighting.control.user.sjp.usb.SerialSettings;
import net.woorisys.lighting.control.user.sjp.usb.USBCommonConstants;
import net.woorisys.lighting.control.user.sjp.usb.USBConstants;
import net.woorisys.lighting.control.user.sjp.usb.USBTerminalException;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.BoardLEDOnOffPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.ChannelSetPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.Config2RequestPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.ConfigPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.ConfigRequestPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.DongCannelPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.GroupDeletePDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.GroupPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.GroupRequestPDU;
import net.woorisys.lighting.control.user.sjp.usb.pdu.request.InterruptSetPDU;
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
import net.woorisys.lighting.control.user.sjp.usb.pdu.response.ResponsePDUBase;
import net.woorisys.lighting.control.user.sjp.usb.util.DecodeUtil;
import net.woorisys.lighting.control.user.sjp.usb.util.NumberUtil;
import net.woorisys.lighting.control.user.sjp.usb.util.PrintUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

/**
 * Interface 0: CDC/ACM/AT-commands(v.25ter) - Endpoint 0: input interrupt Interface 1: CDC Data/ubused/ubknown Endpoint
 * 0: input bulk Endpoint Endpoint 1: ouput bulk Endpoint
 * 
 * @author hslim
 */
public class CDCACMUSBDeviceManager extends AbstractUSBDeviceManager {

	boolean debug = false;

	/** 처음 명령 전송 여부, 처음 전송 시 동글과 통신에 문제가 있어서 두 번 전송 한다. */
	//	private boolean isFirstSend = true;

	/**
	 * USB 인터페이스: Host 장비로의 Notification 용
	 */
	protected UsbInterface notyIntf = null;

	/**
	 * 라인 제어 시 보드레이트도 같이 설정 하므로 setBaudrate 호출 시 보드레이트를 설정하여 두었다가 setLineControl 호출 시 같이 설정한다.
	 */
	int baudRate;

	byte[] portSetting = new byte[7];

	public CDCACMUSBDeviceManager(Context context, Handler handler, UsbManager usbManager) {
		this.usbManager = usbManager;
		this.handler = handler;
	}

	@Override
	public boolean initDevice(Context context, UsbManager usbManager, UsbDevice device, SerialSettings serialSettings)
			throws USBTerminalException {
		System.out.println("initDevice");
		//		isFirstSend = true;
		usbDevice = device;

		if (null == device)
			return false;

		// print(device);

		// Interface 0
		// USB 장비(예: CC2531)에서 USB 호스트로 Notification을 알리기 위하여 사용하는 인터페이스
		notyIntf = device.getInterface(0);
		if (null == notyIntf) {
			sendMessageToHandler(USBConstants.WHAT_DEVICE_NOT_CONNECTED);
			return false;
		}

		// Interface 1
		// USB 장비(예: CC2531)와 USB 호스트 간에 데이터를 전송하기 위하여 사용하는 인터페이스
		intf = device.getInterface(1);
		if (null == intf) {
			sendMessageToHandler(USBConstants.WHAT_DEVICE_NOT_CONNECTED);
			return false;
		}

		connection = usbManager.openDevice(device);
		if (null == connection) {
			System.out.println("USB Connection is null.");

			sendMessageToHandler(USBConstants.WHAT_DEVICE_NOT_CONNECTED);
			return false;
		}

		if (!connection.claimInterface(intf, true)) {
			System.out.println("Exclusive interface access failed!");
			return false;
		}

		// 보드레이트를 읽어 온다.
		getBaudrate();

		// Line Control를 읽어 온다.
		getLineControl();

		// 보드레이트를 설정한다.
		if (!setBaudrate(serialSettings.baudRate))
			return false;

		// Line Control 설정: SET_LINE_CTL
		if (!setLineControl(serialSettings.stopBits, serialSettings.parity, serialSettings.dataBits))
			return false;

		// Line Control를 읽어 온다.
		getLineControl();

		System.out.println("Initialized...");
		return true;
	}

	@Override
	public ModemControlLineStatus getModemStatus() {
		return null;
	}

	@Override
	public int getBaudrate() {
		return baudRate;
	}

	@Override
	/**
	 * 별도로 보드레이트만 설정하는 기능이 없으므로 라인 제어 시 같이 설정하기 위하여 맵버 변수에 설정하고 
	 * true를 리턴한다.
	 * 
	 */
	public boolean setBaudrate(int baudRateSettings) {
		this.baudRate = baudRateSettings;
		return true;
	}

	@Override
	public int getLineControl() {
		// Get current settings
		int ret = connection.controlTransfer(CDCACMContstants.GET_REQUEST_TYPE, CDCACMContstants.GET_LINE_CODING, 0, 0,
				portSetting, 7, 100);
		if (ret < 0) {
			System.out.println("Get Line Control Fail");
			return -1;
		}

		// print baud rate
		int baudRate = portSetting[0] & 0xff;
		baudRate |= (portSetting[1] & 0xff) << 8;
		baudRate |= (portSetting[2] & 0xff) << 16;
		baudRate |= (portSetting[3] & 0xff) << 24;

		System.out.println("Current Baudrate is " + baudRate);

		switch (portSetting[4]) {
		case 0:
			System.out.println("Current Stopbits is " + 1);
			break;
		case 1:
			System.out.println("Current Stopbits is " + 1.5);
			break;
		case 2:
			System.out.println("Current Stopbits is " + 2);
			break;
		}

		switch (portSetting[5]) {
		case 0:
			System.out.println("Current Parity is None");
			break;
		case 1:
			System.out.println("Current Parity is Odd");
			break;
		case 2:
			System.out.println("Current Parity is Even");
			break;
		case 3:
			System.out.println("Current Parity is Mark");
			break;
		case 4:
			System.out.println("Current Parity is Space");
			break;
		}

		System.out.println("Current Data bits is " + portSetting[6]);

		return -1;
	}

	@Override
	public boolean setLineControl(float stopBits, String parity, int dataBits) throws USBTerminalException {
		System.out.println("public boolean setLineControl(float stopBits, String parity, int dataBits)");

		byte[] portSetting = makeLineControl(baudRate, stopBits, parity, dataBits);

		// Set new configuration on PL2303
		int ret = connection.controlTransfer(CDCACMContstants.SET_REQUEST_TYPE, CDCACMContstants.SET_LINE_CODING, 0, 0,
				portSetting, 7, 100);
		if (ret < 0) {
			System.out.println("Set Line Control Fail");
			return false;
		}

		return true;
	}

	/**
	 * Line Control 제어 바이트들(LITTL_ENDIAN, LSB가 하위주소에 옮)을 생성한다.
	 * 
	 * @param stopBits Stop bits
	 * @param parity parity
	 * @param dataBits data bits
	 * @return
	 * @throws USBTerminalException
	 */
	private byte[] makeLineControl(int baudRate, float stopBits, String parity, int dataBits)
			throws USBTerminalException {
		portSetting[0] = (byte) (baudRate & 0xff);
		portSetting[1] = (byte) ((baudRate >> 8) & 0xff);
		portSetting[2] = (byte) ((baudRate >> 16) & 0xff);
		portSetting[3] = (byte) ((baudRate >> 24) & 0xff);

		// Setup Stopbits
		if (1 == stopBits)
			portSetting[4] = 0;
		else if (1.5 == stopBits)
			portSetting[4] = 1;
		else if (2 == stopBits)
			portSetting[4] = 2;
		else
			throw new USBTerminalException("Invalid stop bits.");

		// Setup Parity
		if ("None".equals(parity)) // 0000
			portSetting[5] = 0;
		else if ("Odd".equals(parity)) // 0001
			portSetting[5] = 1;
		else if ("Even".equals(parity)) // 0010
			portSetting[5] = 2;
		else if ("Mark".equals(parity)) // 0010
			portSetting[5] = 3;
		else if ("Space".equals(parity)) // 0010
			portSetting[5] = 4;
		else
			throw new USBTerminalException("Invalid parity.");

		// Setup Databits
		if (dataBits < 5 || dataBits > 8)
			throw new USBTerminalException("Invalid data bits.");

		portSetting[6] = (byte) dataBits;

		return portSetting;
	}

	@Override
	public int[] getFlowControl() {
		return null;
	}

	@Override
	public boolean setFlowControl(String mode) {
		System.out.println("CDC ACM Device is not support flow control");
		return false;
	}

	@Override
	public boolean setModemControl(boolean dtrState, boolean rtsState) {
		System.out.println("DTR:" + dtrState + ", RTS:" + rtsState);

		if (!isConnected())
			return false;

		int mhs = makeModemHandshakingStatus(dtrState, rtsState);

		int ret = connection.controlTransfer(CDCACMContstants.SET_REQUEST_TYPE,
				CDCACMContstants.SET_CONTROL_LINE_STATE, mhs, 0, null, 0, 100);
		if (ret < 0) {
			System.out.println("setMHS ERROR");
			return false;
		}

		return true;
	}

	/**
	 * 모뎀 상태 설정 값을 생성한다.
	 * 
	 * @param dtrState
	 * @param rtsState
	 * @return
	 */
	private static int makeModemHandshakingStatus(boolean dtrState, boolean rtsState) {
		System.out.println("makeModemHandshakingStatus");

		/** 하위 바이트(LSB): bit 0 ~ 7 */
		byte lsb = 3;

		/** 상위 바이트(MSB): bit 8 ~ 15 */
		byte msb = 0;

		// bit 0 : DTR 상태
		if (dtrState)
			msb |= 0x01;

		// bit 1: RTS 상태
		if (rtsState)
			msb |= 0x02;

		// bit 10~15: reserved
		System.out.println("SET MHS : " + PrintUtil.bytesToBINString(new byte[] { lsb, msb }));

		return DecodeUtil.decodeInt2(lsb, msb);
	}

	/**
	 * 인자로주어진 문자열을 전송한다.
	 * 
	 * @return
	 */
	public void send(String message) {
		if (null == connection)
			return;

		System.out.println("Send > " + message);

		byte[] send = message.getBytes();
		connection.bulkTransfer(intf.getEndpoint(1), send, send.length, 0);
	}

	/**
	 * 인자로주어진 바이트 배열을 전송한다.
	 * 
	 * @return
	 */
	public void send(byte[] message) {
		if (null == connection)
			return;

		System.out.println("Send > " + PrintUtil.bytesToHEXString(message));
		connection.bulkTransfer(intf.getEndpoint(1), message, message.length, 0);
	}

	/**
	 * USB 장비로 부터 데이터를 수신한다.
	 * 
	 * @return 수신 에러 발생 시 null을 리턴한다.
	 */
	public byte[] recv() {
		if (null == connection)
			return new byte[0];

		int readCnt = connection.bulkTransfer(intf.getEndpoint(0), recvBuffer, 1024, USBCommonConstants.USB_TIME_OUT);

		if (readCnt < 0) {
			System.out.println("sensor timeout");
			return null;
		}

		System.out.println("Received bytes " + readCnt);

		byte[] ret = new byte[readCnt];
		System.arraycopy(recvBuffer, 0, ret, 0, readCnt);

		return ret;
	}
	
	public int recv(byte[] recvBuffer) {
		if (null == connection)
			return -1;

		int readCnt = connection.bulkTransfer(intf.getEndpoint(0), recvBuffer, 1024,
				USBCommonConstants.USB_TIME_OUT);

		return readCnt;
	}

	/**
	 * USB 연결을 종료한다.
	 */
	@Override
	public void close() {
		System.out.println("Closing ....");

		if (null != intf)
			connection.releaseInterface(intf);

		if (null != connection)
			connection.close();

		intf = null;
		usbDevice = null;
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

		//		String macFront = mac.substring(0, 7);
		//		String macBack = mac.substring(7, 11);

		//		int macResult = Integer.valueOf(id);
		//		String id = String.format("%04X", macResult);
		//		System.out.println("Device id is '" + id + "'");
		//		System.out.println("Original Device id is '" + mac + "'");

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
		if (id == null)
			return false;

		LEDOffPDU pdu = new LEDOffPDU(id, transferType);
		ResponsePDUBase response = sendCommand(pdu);

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
		if (id == null)
			return false;

		SectionIDPDU pdu = new SectionIDPDU(id, transferType, sectionId);
		ResponsePDUBase response = sendCommand(pdu);

		return response.getResult();
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
		if (id == null)
			return false;

		ConfigPDU pdu = new ConfigPDU(id, maxBrightness, standByBrightness,
				autoOffDelayTime, onDimmingDelay, offDimmingDelay, sensitivity, useBroadcastSectionFilter);
		ResponsePDUBase response = sendCommand(pdu);

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
	public boolean sendConfigBroadcast(String mac, String sectionID, int maxBrightness, int standByBrightness,
                                       int autoOffDelayTime, int onDimmingDelay, int offDimmingDelay, int sensitivity, int channel,
                                       boolean useBroadcastSectionFilter) throws IOException {
		String id = getAddress(mac);
		if (id == null)
			return false;

		ConfigPDU pdu = new ConfigPDU(id, sectionID, maxBrightness, standByBrightness,
				autoOffDelayTime, onDimmingDelay, offDimmingDelay, sensitivity, channel, useBroadcastSectionFilter);
		ResponsePDUBase response = sendCommand(pdu);

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
		if (id == null)
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
		if (id == null)
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

		return response.getResult();
	}

	/**
	 * 인자로 주어진 장비의 그룹 정보를 조회를 요청한다.
	 * @param mac 조회 대상 장비 MAC Address
	 * @return 응답 PDU, 명령 처리 실패 시 null 리턴
	 */
	public GroupResponsePDU sendGroupRequest(String mac) {
		String id = getAddress(mac);
		if (id == null)
			return null;

		GroupRequestPDU pdu = new GroupRequestPDU(id);
		return (GroupResponsePDU) sendCommand(pdu);
	}

	/**
	 * 인자로 주어진 장비의 그룹 정보 삭제를 요청한다.
	 * @param mac 조회 대상 장비 MAC Address
	 * @param transferType 전송 형태(UNICAST / BROADCAST)
	 * 
	 * @return 응답 PDU, 명령 처리 실패 시 null 리턴
	 */
	public boolean sendGroupDelete(String mac, String transferType) {
		String id = getAddress(mac);
		if (id == null)
			return false;

		GroupDeletePDU pdu = new GroupDeletePDU(id, transferType);
		ResponsePDUBase response = sendCommand(pdu);

		return response.getResult();
	}

	/**
	 * 동글로 부터 가장 가까운 노드들의 조회를 요청한다.
	 * @return 응답 PDU, 명령 처리 실패 시 null 리턴
	 */
	public RSSIResponsePDU sendRSSIRequest() {
		RSSIRequestPDU pdu = new RSSIRequestPDU();
		return (RSSIResponsePDU) sendCommand(pdu);
	}
	
	/**
	 * 인자로 주어진 채널 설정을 요청한다.
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	public boolean sendDongChannel(String channel) throws IOException {
		DongCannelPDU pdu = new DongCannelPDU(channel);
		ResponsePDUBase response = sendCommand(pdu);

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
		ResponsePDUBase response = sendCommand(pdu);

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

	/**
	 * 요청 메시지를 전송하고 응답 메시지를 수신하여 리턴한다.
	 * 
	 * @param pdu 요청 PDU
	 * @return
	 */
	public ResponsePDUBase sendCommand(RequestPDUBase pdu) {
		String response = sendCommand(pdu.encode());
		String commandID = pdu.getCommandID();

		if (RequestPDUBase.COMMAND_ID_ON.equals(commandID) 
				|| RequestPDUBase.COMMAND_ID_OFF.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_SECTION_ID.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_CONFIG.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_GROUP.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_GROUP_DELETE.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_DONG_CHANNEL.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_BOARD_LED.equals(commandID)
				|| RequestPDUBase.COMMAND_ID_CHANNEL.equals(commandID)
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
		} else if (RequestPDUBase.COMMAND_ID_CONFIG2_REQ.equals(commandID)) {
			Config2ResponsePDU resPdu = new Config2ResponsePDU();
			if (resPdu.decode(response))
				return resPdu;
		}

		return null;
	}

	/**
	 * 명령어를 전송하고 응답을 수신한다.
	 * 
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
			byte[] send = command.getBytes();
			connection.bulkTransfer(intf.getEndpoint(1), send, send.length, 0);

			//			if (isFirstSend)
			//			{
			//				connection.bulkTransfer(intf.getEndpoint(1), send, send.length, 0);
			//				isFirstSend = false;
			//			}

			StringBuilder sb = new StringBuilder();

			int ret = connection.bulkTransfer(intf.getEndpoint(0), recvBuffer, 1024, 5000);
			if (ret < 0) {
				Log.e("Send Command3", "센서가 응답하지 않습니다");
				Message message = handler.obtainMessage();
				message.what = USBConstants.WHAT_DEVICE_TIME_OUT;
				handler.sendMessage(message);
				return null;
			}

			Log.e("Received bytes", Arrays.toString(recvBuffer));
			if (ret != 0) {
				Log.e("Received : ", new String(recvBuffer, 0, ret));
				sb.append(new String(recvBuffer, 0, ret));
				if (sb.indexOf(";") != -1)
					return skipBlankLines(sb.toString());
			}

			return "";
		}
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

		System.out.println("NORMAL : " + sb.toString());
		return sb.toString();
	}
}
