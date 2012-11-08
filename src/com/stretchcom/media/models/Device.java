package com.stretchcom.media.models;

public class Device {
	
	public final static String IOS_CLIENT = "iOS";
	public final static String ANDROID_CLIENT = "Android";

	private String client;
	private String deviceToken;
	
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

}
