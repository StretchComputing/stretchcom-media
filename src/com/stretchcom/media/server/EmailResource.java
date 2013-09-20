package com.stretchcom.media.server;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;


public class EmailResource extends ServerResource {
	private static final Logger log = Logger.getLogger(EmailResource.class.getName());
	
	private static final String EMAIL = "email";
	private static final String TEXT = "text";
	
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

    @Post  
    public JsonRepresentation sendEmail(Representation entity) {
    	log.info("sendEmail(@Post) entered ..... ");
    	JSONObject jsonReturn = new JSONObject();
    	
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_CREATED);
    	try {
    		JsonRepresentation jsonRep = new JsonRepresentation(entity);
    		JSONObject json = jsonRep.getJsonObject();
    		
			String toEmailAddress = null;
			if(json.has("toEmailAddress")) {
				toEmailAddress = json.getString("toEmailAddress");
			}
    		
			String fromEmailAddress = null;
			if(json.has("fromEmailAddress")) {
				fromEmailAddress = json.getString("fromEmailAddress");
			}
    		
			String subject = null;
			if(json.has("subject")) {
				subject = json.getString("subject");
			}
    		
			String body = null;
			if(json.has("body")) {
				body = json.getString("body");
			}
    		
			String type = EMAIL;
			if(json.has("type")) {
				type = json.getString("type");
			}
			
			// Enforce Rules
			if(subject == null || subject.length() == 0 || body ==  null || body.length() == 0 ||
			   toEmailAddress == null || toEmailAddress.length() == 0 || fromEmailAddress == null || fromEmailAddress.length() == 0) {
				apiStatus = ApiStatusCode.SUBJECT_BODY_AND_EMAIL_ADDRESSES_REQUIRED;
				log.info("required JSON field missing");
			}
			
			if(!type.equalsIgnoreCase(EMAIL) && !type.equalsIgnoreCase(TEXT)) {
				apiStatus = ApiStatusCode.INVALID_EMAIL_TYPE_PARAMETER;
				log.info("unsupported email type = " + type);
			}
			
			if(!apiStatus.equals(ApiStatusCode.SUCCESS) || !this.getStatus().equals(Status.SUCCESS_CREATED)) {
				jsonReturn.put("apiStatus", apiStatus);
				return new JsonRepresentation(jsonReturn);
			}

    		apiStatus = buildAndSendEmail(subject, body, toEmailAddress, fromEmailAddress, type);
    	} catch (IOException e) {
			log.severe("error extracting JSON object from Post");
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (JSONException e) {
			log.severe("error converting json representation into a JSON object");
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
    
    private String buildAndSendEmail(String theSubject, String theBody, String theToEmailAddress, String theFromEmailAddress, String theType) {
		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		String host = "localhost";
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);	    
	    
	    try {
	    	Message msg = new MimeMessage(session);
	        msg.setFrom(new InternetAddress(theFromEmailAddress));
	        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(theToEmailAddress));
	        //::TODO:: only for testing **********************************************************
	        //msg.addRecipient(Message.RecipientType.BCC, new InternetAddress("joepwro@gmail.com"));
	        //msg.addRecipient(Message.RecipientType.BCC, new InternetAddress("njw438@gmail.com"));
	        //************************************************************************************
	        if(theSubject == null || theSubject.trim().length() == 0) {
	        	theSubject = "...";
	        	log.info("setting subject to ...");
	        }
	        msg.setSubject(theSubject);
	        
	        String contentType = "text/html";
	        if(theType.equalsIgnoreCase(TEXT)) {
	        	contentType = "text/plain";
	        }
	        msg.setContent(theBody, contentType);
	        log.info("sending email to: " + theToEmailAddress + " with subject: " + theSubject);
	        Transport.send(msg);
	    } catch (AddressException e) {
	        log.severe("email Address exception " + e.getMessage());
	        return ApiStatusCode.SEND_EMAIL_FAILURE;
	    } catch (MessagingException e) {
	    	log.severe("email had bad message: " + e.getMessage());
	    	return ApiStatusCode.SEND_EMAIL_FAILURE;
	    } catch (Exception e) {
	    	log.severe("general exception "  + e.getMessage());
	    	return ApiStatusCode.SEND_EMAIL_FAILURE;
	    }
	    
	    return ApiStatusCode.SUCCESS;
    }
}
