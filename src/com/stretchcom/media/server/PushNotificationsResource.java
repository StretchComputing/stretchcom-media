package com.stretchcom.media.server;

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

import com.stretchcom.media.models.PushNotification;
import com.stretchcom.rskybox.server.ApiStatusCode;
import com.stretchcom.rskybox.server.Utility;

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
        log.info("in post");
        return create_push(entity);
    }

    private JsonRepresentation create_push(Representation entity) {
        JSONObject json = new JsonRepresentation(entity).getJsonObject();
        JSONObject jsonReturn = new JSONObject();

        PushNotification pushNotification = null;
		String apiStatus = ApiStatusCode.SUCCESS;
        this.setStatus(Status.SUCCESS_CREATED);
        
        try {
            if(json.has(PushNotification.TYPE_JSON)) {
    		} else {
            	return Utility.apiError(this, ApiStatusCode.TYPE_REQUIRED);
            }
        } catch(Exception e) {
			log.exception("PushNotificationResource:create_push", e.getMessage(), e);
        }
        
	    return null;
    }
}
