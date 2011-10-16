package com.stretchcom.rteam.smsServer;

import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

public class AuthorizationFilter extends Filter{
	private static final Logger log = Logger.getLogger(AuthorizationFilter.class.getName());
	
	public AuthorizationFilter(Context context) {
		super(context);
	}
	
	protected int beforeHandle(Request request, Response response) {
		if(validAuthentication(request)) {
			return Filter.CONTINUE;
		} else {
			// return HTTP 401 status - unauthorized
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return Filter.STOP;
		}
	}
	
	private boolean validAuthentication(Request request) {
		Map<String, Object> map = request.getAttributes();
		Object obj = map.get("org.restlet.http.headers");
		log.info("obj = " + obj.toString());
		
		String url = request.getResourceRef().getIdentifier();
		Method method = request.getMethod();
		String urlLower = url.toLowerCase();

		log.info("validAuthentication(): urlLower = " + urlLower);
		log.info("validAuthentication(): method = " + method.getName());

//		if(urlLower.endsWith("users") && method.equals(Method.POST)) {
//			// API 'Create a New User' does not use token authorization
//			log.info("validAuthentication(): API 'Create a New User' does not use token authorization");
//			return true;
//		}
		
		if(request.getChallengeResponse() == null) {
			log.severe("unrecognized request: either URL is bad or HTTP Command is wrong");
			return false;
		}
		
		String login = request.getChallengeResponse().getIdentifier();
		String token = new String(request.getChallengeResponse().getSecret());
		log.info("login = " + login);
		log.info("token = " + token);
		log.info("returning from validAuthentication()");
		
		if(!token.equals("test123")) {
			log.info("token did not match - authentication has failed");
		}
		
		return true;
	}
}
