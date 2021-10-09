package com.exalead.customers.jlr.onepart.prefixhandlers;

import java.util.Arrays;

import com.exalead.config.bean.EnumFieldType;
import com.exalead.config.bean.IsMandatory;
import com.exalead.config.bean.PropertyDescription;
import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.config.CVComponentConfig;
import com.exalead.config.bean.PossibleValueType;

public class SamplePrefixHandlerConfig implements CVComponentConfig {
	private String[] fieldName;

	public String[] getFieldName() {
		return fieldName;
	}

	@IsMandatory(value = true)
	@PropertyLabel(value = "Field(s) names")
	@PropertyDescription(value = "Field(s) names")
	public void setFieldName(String[] fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public String toString() {
		return "SamplePrefixHandlerConfig [fieldNames=" + Arrays.toString(fieldName) + "]";
	}
}
