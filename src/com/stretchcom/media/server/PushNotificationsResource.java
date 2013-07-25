package com.stretchcom.media.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;

import javax.servlet.ServletContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.stretchcom.media.models.Device;
import com.stretchcom.media.models.PushNotification;

public class PushNotificationsResource extends ServerResource {
	private RskyboxClient log = new RskyboxClient(this);
	private String id;
	private String name;
	private String listStatus;
	
	public final static String ARC_KEY_STORE_PASSWORD = "arc";
	public final static String RTEAM_KEY_STORE_PASSWORD = "rteam";


    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        this.id = (String) getRequest().getAttributes().get("id");
        this.name = (String) getRequest().getAttributes().get("name");
        this.name = Reference.decode(this.name);
        
		Form form = getRequest().getResourceRef().getQueryAsForm();
		for (Parameter parameter : form) {
			log.info("parameter " + parameter.getName() + " = " + parameter.getValue());
			if(parameter.getName().equals("status"))  {
				this.listStatus = (String)parameter.getValue().toLowerCase();
				this.listStatus = Reference.decode(this.listStatus);
				log.info("ClientLogResource() - decoded status = " + this.listStatus);
			}
		}
    }

    // Handles 'Create Push Notification API'
    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.debug("in post");
        return create_push(entity);
    }

    private JsonRepresentation create_push(Representation entity) {
        JSONObject jsonReturn = new JSONObject();

        PushNotification pushNotification = new PushNotification();
        try {
        	JsonRepresentation jsonRep = extractPushNotificationFromJson(pushNotification, entity);
        	if(jsonRep != null) {return jsonRep;}
        	
        	sendPush(pushNotification);
        } catch(Exception e) {
			log.exception("PushNotificationResource:create_push", e.getMessage(), e);
        }
        
	    return Utility.apiSuccess(this, jsonReturn, Status.SUCCESS_CREATED);
    }
    
    private void sendPush(PushNotification thePushNotification) {
    	try {
    		// TODO not supporting sounds yet
    		
    		// separate the deviceTokens into production and development
    		List<String> developmentTokens = new ArrayList<String>();
    		List<String> productionTokens = new ArrayList<String>();
    		for(Device d : thePushNotification.getDevices()) {
    			if(d.isProduction())	{String pt = d.getDeviceToken(); productionTokens.add(pt); log.info("prd token added: " + pt);}
    			else					{String dt = d.getDeviceToken(); developmentTokens.add(dt); log.info("dev token added: " + dt);}
    		}
    		
            PushNotificationPayload payload = PushNotificationPayload.complex();
            payload.addAlert(thePushNotification.getMessage());
            if(thePushNotification.getBadge() != null) payload.addBadge(thePushNotification.getBadge());

            Map<String, String> cps = thePushNotification.getCustomPayloads();
            if(cps != null) {
            	for(String key : cps.keySet()) {
                    payload.addCustomDictionary(key, cps.get(key));
            	}
            }
            
            List<PushedNotification> notifications = null;
            String keyStore = null;
            InputStream keyStoreStream = null;
            ServletContext sc = (ServletContext) getContext().getServerDispatcher().getContext().getAttributes().get( "org.restlet.ext.servlet.ServletContext" );
            String keyStorePassword = null;
            if(developmentTokens.size() > 0) {
            	if(thePushNotification.getApplication().equalsIgnoreCase(PushNotification.ARC_CUSTOMER_APPLICATION)) {
            		keyStore = "/ArcDevCertificate.p12";
            		keyStorePassword = ARC_KEY_STORE_PASSWORD;
            		log.info("processing push for Arc development");
            	} else if(thePushNotification.getApplication().equalsIgnoreCase(PushNotification.ARC_MERCHANT_APPLICATION)) {
            		keyStore = "/ArcMerchantDevCert.p12";
            		keyStorePassword = ARC_KEY_STORE_PASSWORD;
            		log.info("processing push for Merchant App development");
            	} else if(thePushNotification.getApplication().equalsIgnoreCase(PushNotification.RTEAM_APPLICATION))  {
            		keyStore = "/rTeamDevCertificate.p12";
            		keyStorePassword = RTEAM_KEY_STORE_PASSWORD;
            		log.info("processing push for rTeam development");
            	}
            	//keyStoreStream = this.getClass().getResourceAsStream(keyStore);
            	keyStoreStream = sc.getResourceAsStream(keyStore);
            	notifications = Push.payload(payload, keyStoreStream, keyStorePassword, false, developmentTokens);
            }
            if(productionTokens.size() > 0) {
            	if(thePushNotification.getApplication().equalsIgnoreCase(PushNotification.ARC_CUSTOMER_APPLICATION)) {
            		keyStore = "/ArcProdCertificate.p12";
            		keyStorePassword = ARC_KEY_STORE_PASSWORD;
            		log.info("processing push for Arc production");
            	} else if(thePushNotification.getApplication().equalsIgnoreCase(PushNotification.ARC_MERCHANT_APPLICATION)) {
            		keyStore = "/ArcMerchantProdCert.p12";
            		keyStorePassword = ARC_KEY_STORE_PASSWORD;
            		log.info("processing push for Merchant App production");
            	} else if(thePushNotification.getApplication().equalsIgnoreCase(PushNotification.RTEAM_APPLICATION)) {
            		keyStore = "/rTeamProdCertificate.p12";
            		keyStorePassword = RTEAM_KEY_STORE_PASSWORD;
            		log.info("processing push for rTeam production");
            	}
            	keyStoreStream = sc.getResourceAsStream(keyStore);
            	notifications = Push.payload(payload, keyStoreStream, keyStorePassword, true, productionTokens);
            }
        	logPushNotifications(notifications);
		} catch (CommunicationException e) {
			log.exception("PushNotificationResource:sendPush communication exception", e.getMessage(), e);
		} catch (KeystoreException e) {
			log.exception("PushNotificationResource:sendPush keystore exception", e.getMessage(), e);
		} catch (JSONException e) {
			log.exception("PushNotificationResource:sendPush json exception", e.getMessage(), e);
		}
    }
    
    private void logPushNotifications(List<PushedNotification> theNotifications) {
    	for(PushedNotification pn : theNotifications) {
    		log.info("returned notification: " + pn.toString());
    	}
    }
    
    // @Returns null if successful or JsonReprentation that contains the error code
    private JsonRepresentation extractPushNotificationFromJson(PushNotification pushNotification, Representation entity) {
        try {
            JSONObject json = new JsonRepresentation(entity).getJsonObject();

            if(json.has(ApiJson.APPLICATION)) {
            	pushNotification.setApplication(json.getString(ApiJson.APPLICATION));
            	if(!PushNotification.isApplicationValid(pushNotification.getApplication())) {
            		return Utility.apiError(this, ApiStatusCode.INVALID_APPLICATION_PARAMETER);
            	}
    		} else {
            	return Utility.apiError(this, ApiStatusCode.APPLICATION_REQUIRED);
            }
            
            if(json.has(ApiJson.DEVICES)) {
                List<Device> devices = pushNotification.getDevices();
    			JSONArray devicesJsonArray = json.getJSONArray(ApiJson.DEVICES);
    			int arraySize = devicesJsonArray.length();
    			log.debug("devices json array length = " + arraySize);
    			if(arraySize == 0) {
    				return Utility.apiError(this, ApiStatusCode.DEVICES_REQUIRED);
    			}
    			
    			for(int i=0; i<arraySize; i++) {
    				JSONObject deviceJsonObj = devicesJsonArray.getJSONObject(i);
    				Device d = new Device();
    				
    				// Client, DeviceId and PushType are required. If any are missing, silently ignore this grouping
    				if(deviceJsonObj.has(ApiJson.CLIENT)) {
    					d.setClient(deviceJsonObj.getString(ApiJson.CLIENT));
    	            	if(!PushNotification.isClientValid(d.getClient())) {
    	            		return Utility.apiError(this, ApiStatusCode.INVALID_CLIENT_PARAMETER);
    	            	}
    				} else {
    					continue;
    				}
    				if(deviceJsonObj.has(ApiJson.DEVICE_TOKEN)) {
    					d.setDeviceToken(deviceJsonObj.getString(ApiJson.DEVICE_TOKEN));
    				} else {
    					continue;
    				}
    				if(deviceJsonObj.has(ApiJson.PUSH_TYPE)) {
    					d.setPushType(deviceJsonObj.getString(ApiJson.PUSH_TYPE));
    	            	if(!Device.isPushTypeValid(d.getPushType())) {
    	            		return Utility.apiError(this, ApiStatusCode.INVALID_PUSH_TYPE_PARAMETER);
    	            	}
    				} else {
    					continue;
    				}
    				devices.add(d);
    			}
    			pushNotification.setDevices(devices);
    		} else {
            	return Utility.apiError(this, ApiStatusCode.DEVICES_REQUIRED);
            }
            
            boolean messageNotPresent = false;
            if(json.has(ApiJson.MESSAGE)) {
            	pushNotification.setMessage(json.getString(ApiJson.MESSAGE));
            	if(pushNotification.getMessage().trim().length() == 0) {
            		return Utility.apiError(this, ApiStatusCode.INVALID_MESSAGE_PARAMETER);
            	}
    		} else {
    			messageNotPresent = true;
            }
            
            if(json.has(ApiJson.BADGE)) {
            	try {
                	pushNotification.setBadge(json.getInt(ApiJson.BADGE));
            	} catch(JSONException e) {
            		return Utility.apiError(this, ApiStatusCode.INVALID_BADGE_PARAMETER);
            	}
    		} else if(messageNotPresent) {
            	return Utility.apiError(this, ApiStatusCode.MESSAGE_OR_BADGE_REQUIRED);
    		}
            
            if(json.has(ApiJson.CUSTOM_PAYLOAD)) {
            	String payloadKey = null;
            	String payloadValue = null;
                Map<String, String> customPayloads = new HashMap<String, String>();
    			JSONArray customPayloadsJsonArray = json.getJSONArray(ApiJson.CUSTOM_PAYLOAD);
    			int arraySize = customPayloadsJsonArray.length();
    			log.debug("custom payload json array length = " + arraySize);
    			for(int i=0; i<arraySize; i++) {
    				JSONObject customPayloadJsonObj = customPayloadsJsonArray.getJSONObject(i);
    				
    				if(customPayloadJsonObj.has(ApiJson.PAYLOAD_KEY)) {
    					payloadKey = customPayloadJsonObj.getString(ApiJson.PAYLOAD_KEY);
    				} else {
    					continue;
    				}
    				if(customPayloadJsonObj.has(ApiJson.PAYLOAD_VALUE)) {
    					payloadValue = customPayloadJsonObj.getString(ApiJson.PAYLOAD_VALUE);
    				} else {
    					continue;
    				}
    				customPayloads.put(payloadKey, payloadValue);
    			}
            	pushNotification.setCustomPayloads(customPayloads);
    		}
        } catch (JSONException e) {
			log.exception("PushNotificationResource:extractPushNotificationFromJson", e.getMessage(), e);
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
            return new JsonRepresentation(new JSONObject());
        } catch (IOException e) {
			log.exception("PushNotificationResource:extractPushNotificationFromJson", e.getMessage(), e);
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
            return new JsonRepresentation(new JSONObject());
        } finally {
        }
        return null;
    }

}
