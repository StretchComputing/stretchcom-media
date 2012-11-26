package com.stretchcom.media;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientTest {

    //private static final String HTTPS_BASE_URL = "https://rskybox-stretchcom.appspot.com/";
    //private static final String HTTPS_BASE_URL = "https://rskybox-staging.appspot.com/";
    private static final String HTTPS_BASE_URL = "http://localhost:8080/rTeamSms/";  //development server.  Run->Run As->Web Application
    //private static final String HTTPS_BASE_URL = "http://50.57.64.254:8443/rTeamSms/";  //pr0duction server.  Run->Run As->Web Application
    
    //private static final String REST_BASE_URL = HTTPS_BASE_URL + "rest/v1/";
    private static final String REST_BASE_URL = HTTPS_BASE_URL + "";

    //private static final String PUSH_NOTIFICATION_RESOURCE_URI = "vitals";
    private static final String PUSH_NOTIFICATION_RESOURCE_URI = "pushNotifications";
    private static Boolean isLoggingEnabled = true;

    // HTTP Methods
    private static final String HTTP_PUT = "PUT";
    private static final String HTTP_POST = "POST";
    private static final String HTTP_GET = "GET";
    private static final String HTTP_DELETE = "DELETE";

    private static final int TEN_SECONDS_IN_MILLIS = 10000;
    
    private static final String LOGIN = "arc";
    private static final String AUTH_TOKEN = "111jimjoenicktyleruntitled";

    public static void main(String[] args) throws Exception {
        isLoggingEnabled = true;
//        String joesDevToken = "BE2BC710DB1454FC7314163DFFAEA64274C91A186FE88E7E2A5893B3E53A44FB";  // Merchant App
//        String application = "ArcMerchant";

//        String joesDevToken = "BE2BC710DB1454FC7314163DFFAEA64274C91A186FE88E7E2A5893B3E53A44FB";  // Arc
//        String application = "ArcCustomer";

        String joesDevToken = "BE2BC710DB1454FC7314163DFFAEA64274C91A186FE88E7E2A5893B3E53A44FB";  // rTeam
        String application = "rTeam";
        
        // ========================
        // CREATE PUSH NOTIFICATION
        // ========================
        // PARAMS:: String verifyCreatePushNotification(String theName, String theVersion, String theUserToken)
        List<String> clients = new ArrayList<String>();
        clients.add("iOS");
        List<String> deviceTokens = new ArrayList<String>();
        deviceTokens.add(joesDevToken);
        List<String> pushTypes = new ArrayList<String>();
        pushTypes.add("Development");
        String message = "rTeam is cool";
        verifyCreatePushNotification(application, clients, deviceTokens, pushTypes, message);

        // ========================
        // GET LIST OF APPLICATIONS
        // ========================
        // PARAMS:: verifyGetListOfApplications(String theUserToken)
        //verifyGetListOfApplications(token1);
        //verifyGetListOfApplications(token2);

        // ====================
        // GET APPLICATION INFO
        // ====================
        // PARAMS:: verifyGetApplicationInfo(String theApplicationId, String theUserToken)
        //verifyGetApplicationInfo(rteamAppId, token1);
        //verifyGetApplicationInfo("ahJyc2t5Ym94LXN0cmV0Y2hjb21yEQsSC0FwcGxpY2F0aW9uGHYM", token1);
        //verifyGetApplicationInfo("ahRzfnJza3lib3gtc3RyZXRjaGNvbXITCxILQXBwbGljYXRpb24Y9agCDA", token2);

        // ==================
        // UPDATE APPLICATION
        // ==================
        // PARAMS:: verifyUpdateApplication(String theApplicationId, String theNewVersion, String theUserToken)
        //verifyUpdateApplication(applicationId, "1.1", token1);
    }

    private static JSONObject verifyCreatePushNotification(String theApplication, List<String> theClients, List<String> theDeviceIds, List<String> thePushTypes, String theMessage) {
		if(isLoggingEnabled) System.out.println("\n\n verifyCreatePushNotification() starting .....\n");
		String urlStr = REST_BASE_URL + PUSH_NOTIFICATION_RESOURCE_URI;
		JSONObject json = new JSONObject();
		try {
		    if(theApplication != null) json.put("Application", theApplication);
		    if(theMessage != null) json.put("Message", theMessage);
		    if(theClients != null && theDeviceIds != null && thePushTypes != null) {
				JSONArray devicesJsonArray = new JSONArray();
				for(int i=0; i< theClients.size(); i++) {
					JSONObject deviceJsonObj = new JSONObject();
					deviceJsonObj.put("Client", theClients.get(i));
					deviceJsonObj.put("DeviceToken", theDeviceIds.get(i));
					deviceJsonObj.put("PushType", thePushTypes.get(i));
					devicesJsonArray.put(deviceJsonObj);
				}
				if(devicesJsonArray.length() > 0) json.put("Devices", devicesJsonArray);
				
				// TODO add custom payload support
		  }
           

		    System.out.println(json.toString());

            URL url = new URL(urlStr);
            String response = ClientTest.send(url, ClientTest.HTTP_POST, json.toString(), LOGIN, AUTH_TOKEN);
            if(isLoggingEnabled) System.out.println("repStr = " + response);
            json = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("verifyCreatePushNotification() complete");
        }
        return json;
    }

    private static void verifyGetListOfApplications(String theUserToken) {
        System.out.println("\n\n verifyGetListOfApplications() starting .....\n");
        String urlStr = REST_BASE_URL + PUSH_NOTIFICATION_RESOURCE_URI;
        System.out.println("urlStr = " + urlStr + "\n");

        try {
            URL url = new URL(urlStr);
            String response = ClientTest.send(url, ClientTest.HTTP_GET, null, "login", theUserToken);
            if(isLoggingEnabled) System.out.println("repStr = " + response);
            System.out.println("\n");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("verifyGetListOfApplications() complete\n");
        }
    }

    private static void verifyGetApplicationInfo(String theApplicationId, String theUserToken) {
        System.out.println("\n\n verifyGetApplicationInfo() starting .....\n");
        String urlStr = REST_BASE_URL + PUSH_NOTIFICATION_RESOURCE_URI + "/" + theApplicationId;
        System.out.println("urlStr = " + urlStr + "\n");

        try {
            URL url = new URL(urlStr);
            String response = ClientTest.send(url, ClientTest.HTTP_GET, null, "login", theUserToken);
            if(isLoggingEnabled) System.out.println("repStr = " + response);
            System.out.println("\n");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("verifyGetApplicationInfo() complete\n");
        }
    }

    private static void verifyUpdateApplication(String theApplicationId, String theNewVersion, String theUserToken) {
        System.out.println("\n\n verifyUpdateApplication() starting .....\n");
        String urlStr = REST_BASE_URL + PUSH_NOTIFICATION_RESOURCE_URI + "/" + theApplicationId;
        System.out.println("urlStr = " + urlStr + "\n");
        JSONObject json = new JSONObject();

        try {
            json.put("version", theNewVersion);

            URL url = new URL(urlStr);
            String response = ClientTest.send(url, ClientTest.HTTP_PUT, json.toString(), "login", theUserToken);
            if(isLoggingEnabled) System.out.println("repStr = " + response);
            System.out.println("\n");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("verifyUpdateApplication() complete\n");
        }
    }

    // theUrl: complete url
    // thePayload: the JSON payload to send, if any.  Can be null.
    // theHttpMethod: one of GET POST HEAD OPTIONS PUT DELETE TRACE
    static private String send(URL theUrl, String theHttpMethod, String theJsonPayload,
                               String theBasicAuthUserName, String theBasicAuthPassword) {
        System.out.println("ClientTest::send theUrl = " + theUrl.toString());
        System.out.println("ClientTest::send theJsonPayload = " + theJsonPayload);
        System.out.println("ClientTest::send theHttpMethod = " + theHttpMethod);

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
            connection.setRequestMethod(theHttpMethod);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(TEN_SECONDS_IN_MILLIS);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("accept-encoding", "*/*");


            ///////////////////////
            // Basic Authentication
            ///////////////////////
            if(theBasicAuthUserName != null && theBasicAuthPassword != null) {
                StringBuilder buf = new StringBuilder(theBasicAuthUserName);
                buf.append(':');
                buf.append(theBasicAuthPassword);
                byte[] bytes = null;
                try {
                    bytes = buf.toString().getBytes("ISO-8859-1");
                } catch (java.io.UnsupportedEncodingException uee) {
                    System.out.println("base64 encoding failed: " + uee.getMessage());
                }

                String header = "Basic " + Base64.encodeBase64String(bytes);
                //String header = "";
                connection.setRequestProperty("Authorization", header);
            }

            ////////////////////
            // Send HTTP Request
            ////////////////////
            connection.connect();

            if(theJsonPayload == null) {
                theJsonPayload = "{}";
            }
            if(!theHttpMethod.equalsIgnoreCase(HTTP_GET) && !theHttpMethod.equalsIgnoreCase(HTTP_DELETE)) {
                writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                writer.write(theJsonPayload);
                writer.flush();
            }

            ////////////////////
            // Get HTTP response
            ////////////////////
            int responseCode = connection.getResponseCode();
            System.out.println("responseCode = " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                // read-back the response
                reader = new InputStreamReader(connection.getInputStream());
                BufferedReader in = new BufferedReader(reader);
                StringBuffer responseBuffer = new StringBuffer();
                while (true) {
                    String inputLine = in.readLine();
                    if(inputLine == null) {break;}
                    responseBuffer.append(inputLine);
                }
                in.close();
                response = responseBuffer.toString();
            } else // Server returned HTTP error code.
            {
                System.out.println("ClientTest::send() server returned error code: " + responseCode);
            }
            
            String cookie = connection.getHeaderField("Set-Cookie");
            if (cookie != null) {
               cookie = cookie.substring(0, cookie.indexOf(';'));
               System.out.println("cookie: " + cookie);
            }

        } catch (UnsupportedEncodingException ex) {
            System.out.println("ClientTest::send() UnsupportedEncodingException: " + ex);
        } catch (MalformedURLException ex) {
            System.out.println("ClientTest::send() MalformedURLException: " + ex);
        } catch (IOException ex) {
            System.out.println("ClientTest::send() IOException: " + ex);
        } finally {
            try {
                if (writer != null) {writer.close();}
            } catch (Exception ex) {
                System.out.println("ClientTest::send() Exception closing writer: " + ex);
            }
            try {
                if (reader != null) {reader.close();}
            } catch (Exception ex) {
                System.out.println("ClientTest::send() Exception closing reader: " + ex);
            }
            if (connection != null) {connection.disconnect();}
        }

        return response;
    }

    private static String encode(String theInput) {
        String output = "";
        try {
            output = URLEncoder.encode(theInput, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("encode exception = " + e.getMessage());
        }
        return output;
    }
}

