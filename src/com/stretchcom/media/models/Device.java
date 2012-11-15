package com.stretchcom.media.models;

public class Device {
	
	public final static String IOS_CLIENT = "iOS";
	public final static String ANDROID_CLIENT = "Android";
	
	public final static String PRODUCTION_PUSH_TYPE = "Production";
	public final static String DEVELOPMENT_PUSH_TYPE = "Development";

	private String client;
	private String deviceToken;
	private String pushType;
	
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}

	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	
	public String getPushType() {
		return pushType;
	}
	public void setPushType(String pushType) {
		this.pushType = pushType;
	}
	
	public static boolean isPushTypeValid(String theType) {
		if(theType.equalsIgnoreCase(PRODUCTION_PUSH_TYPE) || theType.equalsIgnoreCase(DEVELOPMENT_PUSH_TYPE)) {
			return true;
		}
		return false;
	}
	
	public boolean isProduction() {
		if(this.pushType != null && this.pushType.equalsIgnoreCase(PRODUCTION_PUSH_TYPE)) { return true;}
		return false;
	}

}
