package net.woorisys.lighting.control.user.sjp.usb.pdu.request;

/**
 * 인터럽트 설정 PDU
 * 
 * @author khk
 *
 */
public class InterruptSetPDU extends RequestPDUBase {
	
	private String use;
	
	private String time;
	
	public InterruptSetPDU(String dest, String transferType, String use, String time) {
		super(dest, transferType, COMMAND_ID_DIM_OFF_SET);
		this.use = use;
		this.time = time;
	}

	@Override
	public String encode() {
		StringBuilder sb = new StringBuilder();
		sb.append(use).append(SEPARATOR);
		sb.append(time);
		
		return encode(sb.toString());
	}
}
