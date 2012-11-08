package com.stretchcom.media.models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PushNotification {
	private static final Logger log = Logger.getLogger(PushNotification.class.getName());
	
	// Keys used in JSON key/value pairs
	public final static String TYPE_JSON = "Type";
	public final static String APPLICATION_JSON = "Application";
	public final static String DEVICES_JSON = "Devices";
	public final static String DEVICE_TOKEN_JSON = "DeviceToken";
	public final static String MESSAGE_JSON = "Message";
	
	public final static String PRODUCTION_SERVER = "Production";
	public final static String DEVELOPMENT_SERVER = "Development";
	
	public final static String ARC_CUSTOMER_APPLICATION = "ArcCustomer";
	public final static String ARC_MERCHANT_APPLICATION = "ArcMerchant";

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

}
