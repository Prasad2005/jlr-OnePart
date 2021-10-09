package com.exalead.customers.jlr.onepart.security;

import com.exalead.config.bean.IsMandatory;
import com.exalead.config.bean.PropertyDescription;
import com.exalead.config.bean.PropertyLabel;
import com.exalead.security.sources.common.SecuritySourceConfig;

public class FakeSecuritySourceConfig extends SecuritySourceConfig {

	private boolean publicToken = true;

	public boolean isPublicToken() {
		return publicToken;
	}

	@IsMandatory(false)
    @PropertyLabel("Public token")
    @PropertyDescription("Add 'everybody' security token to tokens list")
	public void setPublicToken(boolean publicToken) {
		this.publicToken = publicToken;
	}

	@Override
	public String toString() {
		return "FakeSecuritySourceConfig [publicToken=" + publicToken + "]";
	}	
}
