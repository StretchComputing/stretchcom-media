package com.stretchcom.media.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;

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
    		InputStream keyStoreStream = this.getClass().getResourceAsStream("/ArcMerchantDevCert.p12");
    		// TODO only support one device in devices right now
    		String deviceToken = thePushNotification.getDevices().get(0).getDeviceToken();
			Push.alert(thePushNotification.getMessage(), keyStoreStream, "arc", thePushNotification.isProduction(), deviceToken);
		} catch (CommunicationException e) {
			log.debug("communication exception = " + e.getMessage());
		} catch (KeystoreException e) {
			log.debug("keystore exception = " + e.getMessage());
		}
    }
    
    // @Returns null if successful or JsonReprentation that contains the error code
    private JsonRepresentation extractPushNotificationFromJson(PushNotification pushNotification, Representation entity) {
        try {
            JSONObject json = new JsonRepresentation(entity).getJsonObject();

            if(json.has(ApiJson.TYPE)) {
            	pushNotification.setServerType(json.getString(ApiJson.TYPE));
            	if(!PushNotification.isTypeValid(pushNotification.getServerType())) {
            		return Utility.apiError(this, ApiStatusCode.INVALID_TYPE_PARAMETER);
            	}
    		} else {
            	return Utility.apiError(this, ApiStatusCode.TYPE_REQUIRED);
            }
            
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
    			for(int i=0; i<arraySize; i++) {
    				JSONObject deviceJsonObj = devicesJsonArray.getJSONObject(i);
    				Device d = new Device();
    				
    				// both Client and DeviceId are required. If either is missing, silently ignore this pair
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
    				devices.add(d);
    			}
    		} else {
            	return Utility.apiError(this, ApiStatusCode.DEVICES_REQUIRED);
            }
            
            if(json.has(ApiJson.MESSAGE	)) {
            	pushNotification.setMessage(json.getString(ApiJson.MESSAGE));
            	if(pushNotification.getMessage().trim().length() == 0) {
            		return Utility.apiError(this, ApiStatusCode.INVALID_MESSAGE_PARAMETER);
            	}
    		} else {
            	return Utility.apiError(this, ApiStatusCode.MESSAGE_REQUIRED);
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
