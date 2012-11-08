package com.stretchcom.media.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;


public class VitalsResource extends ServerResource {
	private static final Logger log = Logger.getLogger(VitalsResource.class.getName());
	
	//String messageArchiver;

    @Override  
    protected void doInit() throws ResourceException {  
    	log.info("CronResource::doInit() entered");
    	
//        this.messageArchiver = (String)getRequest().getAttributes().get("messageArchiver");
//        if(this.messageArchiver != null) {
//            this.messageArchiver = Reference.decode(this.messageArchiver);
//            log.info("UserResource:doInit() - decoded messageArchiver = " + this.messageArchiver);
//        }
    }  

    @Get("json")  
    public JsonRepresentation alive(Variant variant) {
    	Date checkPoint = new Date();
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	log.info("alive() entered at time = " + df.format(checkPoint));
    	JSONObject jsonReturn = new JSONObject();
    	
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_CREATED);
    	try {
    		jsonReturn.put("isAlive", true);
    	} catch (Exception e) {
			log.severe("alive(): should not happen. exception = " + e.getMessage());
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} finally {
		}
    	
		try {
			jsonReturn.put("apiStatus", apiStatus);
		} catch (JSONException e) {
			log.severe("error creating JSON return object");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return new JsonRepresentation(jsonReturn);
    }
}
