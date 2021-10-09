package com.exalead.customers.jlr.onepart.consobox.transformation;

import com.exalead.config.bean.IsHiddenUI;
import com.exalead.config.bean.IsMandatory;
import com.exalead.config.bean.PropertyDescription;
import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.config.CVComponentConfigClass;
import com.exalead.mercury.component.config.CVComponentConfig;

@PropertyLabel(value = "Sample transformation processor") 
@CVComponentConfigClass(configClass = SampleTransformationProcessorConfig.class) 
public class SampleTransformationProcessorConfig implements CVComponentConfig {
	private String meta;
	/**
	 * Document type to process (SDC TYPE)
	 */
	private String type;

	public String getMeta() {
		return meta;
	}

	public String getType() {
		return type;
	}

	@IsMandatory(false)
	@PropertyLabel("Nodes type")
	@PropertyDescription("Nodes type to process (directive SDC:TYPE); by default node type is DataModel class set by the connector.")
	public void setType(String type) {
		this.type = type;
	}
	
	@IsHiddenUI
	@IsMandatory(false)
	@PropertyLabel("Metadata name used for target URI")
	@PropertyDescription("Metadata name used for target URI")
	public void setTypeHierarchyMetaName(String meta) {
		this.meta = meta;
	}

	@Override public String toString() {
		return "SampleTransformationProcessorConfig{" + "meta name='" + meta + '\'' + '}';
	}
}
