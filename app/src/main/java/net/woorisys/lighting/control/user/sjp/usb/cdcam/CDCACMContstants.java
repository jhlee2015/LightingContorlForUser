package net.woorisys.lighting.control.user.sjp.usb.cdcam;

public class CDCACMContstants {
	
	/** TI2530 Vendor ID :  0x0451 */
	public static final int TI2530_VENDOR_ID = 1105;
	
	/** PL2303 Product ID :  0x16A8 */
	public static final int PL2303_PRODUCT_ID = 5800;
	
	/** Configures DTE rate, stop-bits, parity, and number-of-character bits. */
	public static final int SET_LINE_CODING = 0x20;  

	/** Requests current DTE rate, stop-bits, parity, and number-of-character bits.*/
	public static final int GET_LINE_CODING = 0x21;
	
	/**
	 * RS-232 signal used to tell the DCE device the DTE device is now present.
	 */
	public static final int SET_CONTROL_LINE_STATE = 0x22;
	
	/**
	 * Sends special carrier modulation used to specify RS-232 style break.
	 */
	public static final int SEND_BREAK = 0x23;
	
	/**
	 * Returns the current state of the carrier detect, DSR, break, and ring signal.
	 */
	public static final int SERIAL_STATE = 0x20;
	
	/** USB SET Request Type */
    public static final int SET_REQUEST_TYPE = 0x21;
    
    /** USB GET Request Type */
    public static final int GET_REQUEST_TYPE = 0xA1;
}
