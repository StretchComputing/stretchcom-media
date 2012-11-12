package com.stretchcom.media.models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.stretchcom.media.server.RskyboxClient;

public class PushNotification {
	//private static final Logger log = Logger.getLogger(PushNotification.class.getName());
	private RskyboxClient log = new RskyboxClient();
	
	public final static String PRODUCTION_SERVER = "Production";
	public final static String DEVELOPMENT_SERVER = "Development";
	
	public final static String ARC_CUSTOMER_APPLICATION = "ArcCustomer";
	public final static String ARC_MERCHANT_APPLICATION = "ArcMerchant";
	
	public final static String IOS_CLIENT = "iOS";
	public final static String ANDROID_CLIENT = "Android";

	private String serverType;
	private String application;
	private String message;
	private List<Device> devices = new ArrayList<Device>();

	public PushNotification() {		
	}
	
	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}
	
	public static boolean isTypeValid(String theType) {
		if(theType.equalsIgnoreCase(PRODUCTION_SERVER) || theType.equalsIgnoreCase(DEVELOPMENT_SERVER)) {
			return true;
		}
		return false;
	}
	
	public static boolean isApplicationValid(String theApplication) {
		if(theApplication.equalsIgnoreCase(ARC_CUSTOMER_APPLICATION) || theApplication.equalsIgnoreCase(ARC_MERCHANT_APPLICATION)) {
			return true;
		}
		return false;
	}
	
	public static boolean isClientValid(String theClient) {
		if(theClient.equalsIgnoreCase(IOS_CLIENT) || theClient.equalsIgnoreCase(ANDROID_CLIENT)) {
			return true;
		}
		return false;
	}

}
