package com.exalead.customers.jlr.onepart.consobox.aggregation;

import com.exalead.config.bean.IsMandatory;
import com.exalead.config.bean.PropertyDescription;
import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.config.CVComponentConfig;
import com.exalead.mercury.component.config.CVComponentConfigClass;

/**
 * Sample aggregation processor config
 */
@PropertyLabel(value = "Sample aggregation processor") 
@CVComponentConfigClass(configClass = SampleAggregationProcessorConfig.class) 
public class SampleAggregationProcessorConfig implements CVComponentConfig {
	/**
	 * Document type to process (SDC TYPE)
	 */
	private String type;
	/** Metadata name to fetch */
	private String meta;
	/** Aggregation path */
	private String path;
	
	public String getType() {
		return type;
	}

	@IsMandatory(false)
	@PropertyLabel("Nodes type")
	@PropertyDescription("Nodes type to process (directive SDC:TYPE); by default node type is DataModel class set by the connector.")
	public void setType(String type) {
		this.type = type;
	}

	public String getMeta() {
		return meta;
	}

	@IsMandatory(true)
	@PropertyLabel("Metadata name")
	@PropertyDescription("Metadata name")
	public void setMeta(String meta) {
		this.meta = meta;
	}
	
	public String getPath() {
		return path;
	}

	@IsMandatory(true)
	@PropertyLabel("Graph matching path")
	@PropertyDescription("Graph matching path")
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "SampleAggregationProcessorConfig{" + "type='" + type + '\'' + ", metadata name='" + meta + '\'' + ", path='" + path + "'}";
	}
}
