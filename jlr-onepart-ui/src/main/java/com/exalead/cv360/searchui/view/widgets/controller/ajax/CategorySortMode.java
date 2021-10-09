package com.exalead.cv360.searchui.view.widgets.controller.ajax;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class CategorySortMode {

	public static final CategorySortMode ASC_SORT = new CategorySortMode(1, "ASC");
	public static final CategorySortMode DESC_SORT = new CategorySortMode(-1, "DESC");

	private static final Map<String, CategorySortMode> mapTypeSortMode;
	static {
		mapTypeSortMode = new HashMap<String, CategorySortMode>();
		mapTypeSortMode.put(ASC_SORT.getDescription(), ASC_SORT);
		mapTypeSortMode.put(DESC_SORT.getDescription(), DESC_SORT);
	}

	// Fields
	private final int sort;
	private final String description;

	private CategorySortMode(int value, String description) {
		sort = value;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public int getSort() {
		return sort;
	}

	public static CategorySortMode getModeSort(String description) {
		if (StringUtils.isEmpty(description)) {
			return null;
		}
		String toUpperDescription = description.toUpperCase();

		return mapTypeSortMode.get(toUpperDescription);
	}
}
