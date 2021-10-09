package com.exalead.customers.jlr.onepart.security;

import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.CVComponent;
import com.exalead.mercury.component.CVComponentDescription;
import com.exalead.mercury.component.config.CVComponentConfigClass;
import com.exalead.security.sources.common.AuthenticationResult;
import com.exalead.security.sources.common.SecurityException;
import com.exalead.security.sources.common.SecuritySource;
import com.exalead.security.sources.common.SecurityToken;

@PropertyLabel(value = "FakeSecuritySource")
@CVComponentConfigClass(configClass = FakeSecuritySourceConfig.class)
@CVComponentDescription(value = "Sample security source implementation, this security source only return login (and everybody if configured) security tokens without authentication")
public class FakeSecuritySource extends SecuritySource implements CVComponent {
	private FakeSecuritySourceConfig config;
	
	public FakeSecuritySource(FakeSecuritySourceConfig config) {
		this.config = config;
	}

	@Override
	public AuthenticationResult authenticate(String login, String password, boolean needPassword) throws SecurityException {
		AuthenticationResult result = new AuthenticationResult();
		result.setSuccess(true);
		result.setUserId(login);
		result.setUserDisplayName(login);
		
		result.getSecurityToken().add(new SecurityToken(login));
		
		if (this.config.isPublicToken()){
			result.getSecurityToken().add(new SecurityToken("everybody"));
		}
		
		return result;
	}

	@Override
	public void reset() throws SecurityException {
		// Rien
	}
}
