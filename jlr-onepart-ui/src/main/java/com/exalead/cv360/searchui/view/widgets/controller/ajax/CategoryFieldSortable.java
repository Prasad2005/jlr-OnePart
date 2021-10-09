package com.exalead.cv360.searchui.view.widgets.controller.ajax;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Enumeration of field sortable.
 * 
 * @author ddacosta
 * 
 */
public enum CategoryFieldSortable {
	DESCRIPTION, ID;

	private static final Map<String, CategoryFieldSortable> mapFieldsSortable;
	static {
		mapFieldsSortable = new HashMap<String, CategoryFieldSortable>();
		mapFieldsSortable.put("description", DESCRIPTION);
		mapFieldsSortable.put("id", ID);
	}

	public static CategoryFieldSortable getField(String field) {
		if (StringUtils.isEmpty(field)) {
			return null;
		}

		String lowerField = field.toLowerCase();

		return mapFieldsSortable.get(lowerField);
	}
}
