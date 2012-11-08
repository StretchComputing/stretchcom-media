package com.stretchcom.media.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.resource.ServerResource;
import org.apache.commons.codec.binary.Base64;

import com.stretchcom.media.models.RskyboxLog;

public class RskyboxClient {
	private static final Logger log = Logger.getLogger(RskyboxClient.class.getName());
	private ServerResource serverResource;
	
	// HTTP Methods
	private static final String HTTP_PUT = "PUT";
	private static final String HTTP_POST = "POST";
	
	// rSkybox credentials for Arc application in rSkybox
	private static final String RSKYBOX_APPLICATION_ID = "ahRzfnJza3lib3gtc3RyZXRjaGNvbXITCxILQXBwbGljYXRpb24YgPYvDA";
	private static final String RSKYBOX_APPLICATION_TOKEN = "ekokq167k46gbrmr6hvbht9lab";
	private static final String RSKYBOX_BASIC_AUTH_USER_NAME = "rskyboxUser";
	
	private static final String RSKYBOX_SERVICE_PROVIDER = "GAE Server";
	private static final String RSKYBOX_USER_NAME = "rTeam Common Code";
	
	public RskyboxClient() {
		this.serverResource = null;
	}
	
	public RskyboxClient(ServerResource serverResource) {
		this.serverResource = serverResource;
	}
	
	// rSkybox URL https://rskybox-stretchcom.appspot.com/rest/v1/applications/<applicationId>/clientLogs
	private static final String RSKYBOX_BASE_URL_WITH_SLASH = "https://rskybox-stretchcom.appspot.com/rest/v1/applications/";
	
	public static void exception(String theName, String theMessage, Exception theException, Request theRequest, Boolean theIncludeLocalLog) {
		log(theName, RskyboxLog.EXCEPTION_LEVEL, theMessage, theException, theRequest, theIncludeLocalLog);
	}
	public void exception(String theName, String theMessage, Exception theException) {
		Request request = this.serverResource == null ? null : serverResource.getRequest();
		log(theName, RskyboxLog.EXCEPTION_LEVEL, theMessage, theException, request, true);
	}
	
	public static void error(String theName, String theMessage, Request theRequest, Boolean theIncludeLocalLog) {
		log(theName, RskyboxLog.ERROR_LEVEL, theMessage, null, theRequest, theIncludeLocalLog);
	}
	public void error(String theName, String theMessage) {
		Request request = this.serverResource == null ? null : serverResource.getRequest();
		log(theName, RskyboxLog.ERROR_LEVEL, theMessage, null, request, true);
	}
	
	public static void info(String theName, String theMessage, Request theRequest, Boolean theIncludeLocalLog) {
		log(theName, RskyboxLog.INFO_LEVEL, theMessage, null, theRequest, theIncludeLocalLog);
	}
	public static void info(String theName, String theMessage, Request theRequest) {
		log(theName, RskyboxLog.INFO_LEVEL, theMessage, null, theRequest, true);
	}
	public void info(String theMessage) {
		Request request = this.serverResource == null ? null : serverResource.getRequest();
		log(null, RskyboxLog.INFO_LEVEL, theMessage, null, request, true);
	}
	
	public static void debug(String theName, String theMessage, Request theRequest, Boolean theIncludeLocalLog) {
		log(theName, RskyboxLog.DEBUG_LEVEL, theMessage, null, theRequest, theIncludeLocalLog);
	}
	public static void debug(String theName, String theMessage, Request theRequest) {
		log(theName, RskyboxLog.DEBUG_LEVEL, theMessage, null, theRequest, true);
	}
	public void debug(String theMessage) {
		Request request = this.serverResource == null ? null : serverResource.getRequest();
		log(null, RskyboxLog.DEBUG_LEVEL, theMessage, null, request, true);
	}
	
	// --------------
	// rSkybox Rules:
	// --------------
	// * DEBUG and INFO logs can NOT be enabled/disabled as a LEVEL for all users
	//      (rational: turning on these levels for all users could generate excessive traffic so just don't allow it)
	// * DEBUG and INFO logs can be enabled/disabled as a LEVEL for individual users
	//      (rational: want to be able to do detailed debugging for indidual users)
	// * DEBUG and INFO logs can NOT be enabled/disabled as individual LOGs/insertion points
	//      (rational: it would be expensive to if active/inactive for logs that can occur so frequently. Also, too much maintenance for rSkybox member to do this)
	// * ERROR and EXCEPTION logs can NOT be disabled as a LEVEL
	//      (rational: dangerous to turn off all error reporting. May be needed for rSkybox to throttle application if quotas have been exceeded)
	// * ERROR and EXCEPTION logs can be enabled/disabled as individual LOGs/insertion points
	//      (rational: no sense to keep getting the same error if it keeps happening. Also, allows member to control logging volume)
	public static void log(String theName, String theLevel, String theMessage, Exception theException, Request theRequest, Boolean theIncludeLocalLog) {
		// theLevel must be valid, or pack up our books and go home!
		if(!RskyboxLog.isLogLevelValid(theLevel)) {
			log.severe("bad log level = " + theLevel);
			return;
		}
		
		// Prefix the log name so we know these issues are reported by the comm.server app
		theName = "Comm.server." + theName;

		String stackTrace = "";
		if(theException != null) {
			StackTraceElement[] stackTraceElements = theException.getStackTrace();
			StringBuffer sb = new StringBuffer("");
			for(StackTraceElement ste : stackTraceElements) {
				sb.append(ste.toString());
			}
			stackTrace = sb.toString();
		}

		// No matter what, do the local logging if it is turned on
		if(theIncludeLocalLog != null && theIncludeLocalLog) {
			if(theLevel.equalsIgnoreCase(RskyboxLog.DEBUG_LEVEL)) {
				log.fine(theMessage);
			} else if(theLevel.equalsIgnoreCase(RskyboxLog.INFO_LEVEL)) {
				log.info(theMessage);
			} else if(theLevel.equalsIgnoreCase(RskyboxLog.ERROR_LEVEL) || theLevel.equalsIgnoreCase(RskyboxLog.EXCEPTION_LEVEL)) {
				if(theException != null) {
					log.severe(theMessage + " exception: " + theException.getMessage() + " stackTrace: " + stackTrace);
				} else {
					log.severe(theMessage);
				}
			}
		}
		
		// Identify end user if possible
		// TODO: maybe this service will have multiple users eventually. At that point, this should be activated.
		String userName = RSKYBOX_USER_NAME;
		String userId = "";
		if(theRequest != null) {
			/*User currentUser = Utility.getCurrentUser(theRequest);
			if(currentUser != null) {
				userName = currentUser.getFullName() + ", " + currentUser.getEmailAddress();
				userId = currentUser.getToken();
			}*/
		}
		
		// check to see if request LOG is enabled
		if(!isLogEnabled(theName, theLevel)) {
			//log.info("log " + theName + " is inactive");
			return;
		}
		
		//////////////////////////
		// Create the JSON Payload
		//////////////////////////
		JSONObject jsonPayload = null;
		try {
			jsonPayload = new JSONObject();
			jsonPayload.put("logLevel", theLevel);
			jsonPayload.put("logName", theName);
			jsonPayload.put("message", theMessage);
			
			if(stackTrace.length() > 0) {
				// TODO modify rSkybox to take a JSON array of stackTraceElements
				jsonPayload.put("stackBackTrace", stackTrace);
			}
			
			// TODO rSkybox should rename instanceUrl to something more generic - here we use the current user's login ID if there is a current user
			jsonPayload.put("userName", userName);
			jsonPayload.put("userId", userId);
			jsonPayload.put("instanceUrl", RSKYBOX_SERVICE_PROVIDER);
			//log.info("jsonPayload = " + jsonPayload.toString());
			
			String response = null;
			String urlStr = RSKYBOX_BASE_URL_WITH_SLASH + RSKYBOX_APPLICATION_ID + "/clientLogs";
			URL url = null;
			try {
				url = new URL(urlStr);
				response = send(url, jsonPayload.toString());
				if(response.length() > 0) {
					JSONObject jsonReturn = new JSONObject(response);
					if(jsonReturn.has("apiStatus")) {
						String apiStatus = jsonReturn.getString("apiStatus");
						//log.info("apiStatus of rSkybox API call = " + apiStatus);
					}
					
					// see if rSkybox is turning off this log
					if(jsonReturn.has("logStatus")) {
						String logStatus = jsonReturn.getString("logStatus");
						//log.info("logStatus of rSkybox API call = " + logStatus);
						if(logStatus.equalsIgnoreCase(RskyboxLog.INACTIVE_STATUS)) {
							RskyboxLog.setLog(theName, RskyboxLog.INACTIVE_STATUS);
						}
					}
				}
			} catch (MalformedURLException e) {
				log.severe("MalformedURLException exception: " + e.getMessage());
			}
		} catch (JSONException e1) {
			log.severe("JSONException exception: " + e1.getMessage());
			return;
		}
	}
	
	// theUrl: complete url
	// thePayload: the JSON payload to send, if any.  Can be null.
	static private String send(URL theUrl, String theJsonPayload) {
		//log.info("RskyboxClient::send theUrl = " + theUrl.toString());

		String response = "";
		HttpURLConnection connection = null;
		OutputStreamWriter writer = null;
		InputStreamReader reader = null;
		try {
			/////////////////////
			// Prepare connection
			/////////////////////
			connection = (HttpURLConnection)theUrl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setAllowUserInteraction(false);
			connection.setRequestMethod(HTTP_POST);
			connection.setRequestProperty("Content-Type", "application/json");
			
			///////////////////////
			// Basic Authentication
			///////////////////////
			StringBuilder buf = new StringBuilder(RSKYBOX_BASIC_AUTH_USER_NAME);
			buf.append(':');
			buf.append(RSKYBOX_APPLICATION_TOKEN);
			byte[] bytes = null;
			try {
				bytes = buf.toString().getBytes("ISO-8859-1");
			} catch (java.io.UnsupportedEncodingException uee) {
				log.severe("base64 encoding failed: " + uee.getMessage());
			}

			String header = "Basic " + Base64.encodeBase64(bytes);
			connection.setRequestProperty("Authorization", header);

			////////////////////
			// Send HTTP Request
			////////////////////
			connection.connect();
			
			if(theJsonPayload == null) {
				theJsonPayload = "{}";
			}
			writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			writer.write(theJsonPayload);
			writer.flush();

			////////////////////
			// Get HTTP response
			////////////////////
			int responseCode = connection.getResponseCode();
			//log.info("responseCode = " + responseCode);
			
			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
				// read-back the response
				reader = new InputStreamReader(connection.getInputStream());
				BufferedReader in = new BufferedReader(reader);
				StringBuffer responseBuffer = new StringBuffer();
				//log.info("reading the response ...");
				while (true) {
					String inputLine = in.readLine();
					if(inputLine == null) {break;}
					//log.info("response inputLine = " + inputLine);
					responseBuffer.append(inputLine);
				}
				in.close();
				response = responseBuffer.toString();
			} else {
				// Server returned HTTP error code.{
				log.severe("RskyboxClient::send server returned HTTP error code: " + responseCode);
			}
		} catch (UnsupportedEncodingException ex) {
			log.severe("RskyboxClient::send UnsupportedEncodingException: " + ex);
		} catch (MalformedURLException ex) {
			log.severe("RskyboxClient::send MalformedURLException: " + ex);
		} catch (IOException ex) {
			log.severe("RskyboxClient::send IOException: " + ex);
		} finally {
			try {
				if (writer != null) {writer.close();}
			} catch (Exception ex) {
				log.severe("RskyboxClient::send Exception closing writer: " + ex);
			}
			try {
				if (reader != null) {reader.close();}
			} catch (Exception ex) {
				log.severe("RskyboxClient::send Exception closing reader: " + ex);
			}
			if (connection != null) {connection.disconnect();}
		}

		return response;
	}
	
	private static Boolean isLogEnabled(String theName, String theLevel) {
		if(theLevel.equalsIgnoreCase(RskyboxLog.DEBUG_LEVEL) || theLevel.equalsIgnoreCase(RskyboxLog.INFO_LEVEL)) {
			// TODO add support for:  DEBUG and INFO logs can be enabled/disabled as a LEVEL for individual users
			// for now, always disabled
			return false;
		}
		
		if(theLevel.equalsIgnoreCase(RskyboxLog.ERROR_LEVEL) || theLevel.equalsIgnoreCase(RskyboxLog.EXCEPTION_LEVEL)) {
			//////////////////////////////////////////////
			// TODO potentially add memcache optimizations
			//////////////////////////////////////////////
			
			// if associated log not found, default status is ENABLED
			RskyboxLog rskyboxLog = RskyboxLog.getLog(theName);
			if(rskyboxLog != null && !rskyboxLog.isEnabled()) {
				return false;
			}
		}
		
		return true;
	}
}
