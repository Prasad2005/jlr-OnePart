package com.exalead.customers.jlr.onepart.analysis;

import com.exalead.config.bean.IsMandatory;
import com.exalead.config.bean.PropertyDescription;
import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.config.CVComponentConfig;

public class SampleDocumentProcessorConfig implements CVComponentConfig {
	private String param;

	public String getParam() {
		return param;
	}

	@IsMandatory(value = true)
	@PropertyLabel(value = "Config parameter")
	@PropertyDescription(value = "Sample configuration parameter UI description")
	public void setParam(String param) {
		this.param = param;
	}

	@Override
	public String toString() {
		return "SampleDocumentProcessorConfig [param=" + param + "]";
	}
}
