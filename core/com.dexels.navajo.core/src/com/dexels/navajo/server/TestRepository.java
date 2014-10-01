package com.dexels.navajo.server;

import java.util.Map;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.AuthorizationException;
import com.dexels.navajo.script.api.SystemException;

public class TestRepository extends SimpleRepository {

	@Override
	public Access authorizeUser(String username, String password,
			String service, Navajo inMessage, Object certificate, String accessID)
			throws SystemException, AuthorizationException {
		
		if ( "myself".equals(username) && "mysecret".equals(password) ) {
			
			if ( service.equals("forbiddenservice") ) {
				throw new AuthorizationException(false, true, username, "Not allowed to use this service");
			}
			
			return new Access(accessID, 1, 1, username, service, "Navajo Client", "1.1.1.1", "myhost", null);
			
		} else {
			throw new AuthorizationException(true, false, username, "Unknown user");
		}
	}
	
//	public String[] getServices(Access access)  {
//		return new String[]{"MyTestService1", "YetAnotherNiceService"};
//	}
	
	@Override
	public void initGlobals(String service, String username, Navajo inMessage, 
			                Map<String,String> extraParams) throws NavajoException {

		if ( "YetAnotherNiceService".equals(service)  ) {
			Message msg = NavajoFactory.getInstance().createMessage(inMessage, "globals");
			inMessage.addMessage(msg);
			Property p = NavajoFactory.getInstance().createProperty(inMessage, "MailServer", Property.STRING_PROPERTY, 
					"mail.navajo.com", 0,"","out");
			msg.addProperty(p);
		}
	}
}
