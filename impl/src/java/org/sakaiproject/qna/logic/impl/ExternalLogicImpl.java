package org.sakaiproject.qna.logic.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.qna.logic.ExternalLogic;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class ExternalLogicImpl implements ExternalLogic {
	
	private final static String PROP_SITE_CONTACT_EMAIL = "contact-email";

	private final static String PROP_SITE_CONTACT_NAME = "contact-name";
	
	private static Log log = LogFactory.getLog(ExternalLogicImpl.class);
	
    private FunctionManager functionManager;
    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }
    
    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}
    
	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
    
    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    private EntityBroker entityBroker;
    public void setEntityBroker(EntityBroker entityBroker) {
        this.entityBroker = entityBroker;
    }
    
    public void init() {
    	log.debug("init");
        // register Sakai permissions for this tool
        functionManager.registerFunction(QNA_READ);
        functionManager.registerFunction(QNA_NEW_QUESTION);
        functionManager.registerFunction(QNA_NEW_ANSWER);
        functionManager.registerFunction(QNA_NEW_CATEGORY);
        functionManager.registerFunction(QNA_UPDATE);
    }
    
	public String getCurrentLocationId() {
		try {
			
			if (toolManager.getCurrentPlacement() == null)
			{
				return NO_LOCATION;
			}

			Site s = siteService.getSite(toolManager.getCurrentPlacement().getContext());
			return s.getReference();
		} catch (IdUnusedException e) {
			return NO_LOCATION;
		}
	}

	public String getLocationTitle(String locationId) {
        try {
			 Site site = (Site) entityBroker.fetchEntity(locationId);
			 return site.getTitle();
        } catch (Exception e) {
            // invalid site reference
            log.debug("Invalid site reference:" + locationId);
            return "----------";
        }
	}
    
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	public String getUserDisplayName(String userId) {
		try {
			return userDirectoryService.getUser(userId).getDisplayName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get user displayname for id: " + userId);
			return "--------";
		}
	}
	
	public boolean isUserAdmin(String userId) {
		return securityService.isSuperUser(userId);
	}

	public boolean isUserAllowedInLocation(String userId, String permission, String locationId) {
		if ( securityService.unlock(userId, permission, locationId) ) {
			return true;
		}
		return false;
	}

	public String getSiteContactEmail(String locationId) {
		Site site = (Site) entityBroker.fetchEntity(locationId);
		if (site.getProperties().getProperty(PROP_SITE_CONTACT_EMAIL) != null) {
			return site.getProperties().getProperty(PROP_SITE_CONTACT_EMAIL);
		} else {
			return site.getCreatedBy().getEmail();
		}
	}

	public String getSiteContactName(String locationId) {
		Site site = (Site) entityBroker.fetchEntity(locationId);
		if (site.getProperties().getProperty(PROP_SITE_CONTACT_NAME) != null) {
			return site.getProperties().getProperty(PROP_SITE_CONTACT_NAME);
		} else {
			return site.getCreatedBy().getDisplayName();
		}
	}

	@SuppressWarnings("unchecked")
	public Set<User> getSiteUsersWithPermission(String locationId, String permission) {
		Site site = (Site) entityBroker.fetchEntity(locationId);
		Set<User> users = site.getUsers();
		
		Set<User> usersWithPermission = new HashSet<User>();
		for (User user : users) {
			if (isUserAllowedInLocation(user.getId(), permission, locationId)) {
				usersWithPermission.add(user);
			}
		}
		return usersWithPermission;
	}

}
