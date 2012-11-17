package com.stretchcom.media.server;

public class ApiStatusCode {
	public static final String SERVER_ERROR = "0";
	
	public static final String SUCCESS = "100";
	
	public static final String SEND_EMAIL_FAILURE = "200";
	
	public static final String SUBJECT_BODY_AND_EMAIL_ADDRESSES_REQUIRED = "300";
	public static final String APPLICATION_REQUIRED = "302";
	public static final String DEVICES_REQUIRED = "303";
	public static final String MESSAGE_REQUIRED = "304";
	
	public static final String INVALID_PUSH_TYPE_PARAMETER = "400";
	public static final String INVALID_APPLICATION_PARAMETER = "401";
	public static final String INVALID_CLIENT_PARAMETER = "402";
	public static final String INVALID_MESSAGE_PARAMETER = "403";
	
	public static final String PHONE_NUMBER_AND_MOBILE_CARRIER_ID_MUST_BE_SPECIFIED_TOGETHER = "500";
	
	public static final String USER_NOT_FOUND = "600";
	

}
