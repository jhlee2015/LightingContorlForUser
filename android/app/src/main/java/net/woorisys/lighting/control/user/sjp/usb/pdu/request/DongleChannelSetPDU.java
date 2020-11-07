package net.woorisys.lighting.control.user.sjp.usb.pdu.request;

import android.util.Log;

/**
 * 동글채널 설정
 * 
 * @author jhlee
 *
 */
public class DongleChannelSetPDU extends RequestPDUBase {

	private String channel;

	public DongleChannelSetPDU(String dest, String transferType, String channel) {
		super(dest, transferType, COMMAND_ID_DONG_CHANNEL);
		this.channel = channel;

		Log.d("SJP_DEFAULT_TAG","cmd : "+COMMAND_ID_DONG_CHANNEL+", channel :"+channel);
	}

	@Override
	public String encode() {
		StringBuilder sb = new StringBuilder();
		sb.append(channel);

		return encode(sb.toString());
	}
}
