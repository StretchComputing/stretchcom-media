package com.stretchcom.media.models;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

public class RskyboxLog {
	private static final Logger log = Logger.getLogger(RskyboxLog.class.getName());
	
	// Log Levels
	public static final String DEBUG_LEVEL = "debug";
	public static final String INFO_LEVEL = "info";
	public static final String ERROR_LEVEL = "error";
	public static final String EXCEPTION_LEVEL = "exception";
	
	// Status Levels
	public static final String ACTIVE_STATUS = "active";
	public static final String INACTIVE_STATUS = "inactive";

	private String logLevel;
	private String logName;
	private Date lastModifiedGmtDate;
	private String userName;
	private String status;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

	public Date getLastModifiedGmtDate() {
		return lastModifiedGmtDate;
	}

	public void setLastModifiedGmtDate(Date lastModifiedGmtDate) {
		this.lastModifiedGmtDate = lastModifiedGmtDate;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	
	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}
	
	public static Boolean isLogLevelValid(String theLogLevel) {
		if(theLogLevel.equalsIgnoreCase(DEBUG_LEVEL) ||
		   theLogLevel.equalsIgnoreCase(INFO_LEVEL) ||
		   theLogLevel.equalsIgnoreCase(ERROR_LEVEL) ||
		   theLogLevel.equalsIgnoreCase(EXCEPTION_LEVEL)) {
			return true;
		}
		return false;
	}
	
    // returns the RskyboxLog matching the specified logName; null if not found
	public static RskyboxLog getLog(String theLogName) {
    	return null;
    }
	
	public static Boolean isStatusValid(String theStatus) {
		if(theStatus.equals(RskyboxLog.ACTIVE_STATUS) || theStatus.equals(RskyboxLog.INACTIVE_STATUS)) return true;
		return false;
	}

	// set the status of the rskyboxLog.  If it doesn't exist yet, create it.
	public static RskyboxLog setLog(String theLogName, String theStatus) {
    	return null;
    }
	
	public Boolean isEnabled() {
		if(this.status == null || this.status.equalsIgnoreCase(INACTIVE_STATUS)) {return false;}
		return true;
	}
}
