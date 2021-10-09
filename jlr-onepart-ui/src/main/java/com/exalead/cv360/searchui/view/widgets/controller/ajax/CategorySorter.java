package com.exalead.cv360.searchui.view.widgets.controller.ajax;

import java.util.Comparator;

import com.exalead.access.feedapi.Category;

/**
 * Sorter for cloudview Category
 * 
 * @author ddacosta
 * 
 */
public class CategorySorter implements Comparator<Category> {

	private final CategorySortMode sortMode;
	private final CategoryFieldSortable fieldSortable;

	public CategorySorter(CategorySortMode sortMode, CategoryFieldSortable fieldSortable) {
		if (sortMode == null) {
			throw new IllegalArgumentException("Parameters is null");
		}

		if (fieldSortable == null) {
			fieldSortable = CategoryFieldSortable.ID;
		}

		this.sortMode = sortMode;
		this.fieldSortable = fieldSortable;
	}

	@Override
	public int compare(Category cat1, Category cat2) {
		if (cat1 == null || cat2 == null) {
			return sortMode.getSort();
		}

		int compare = 0;

		switch (fieldSortable) {
		case DESCRIPTION:
			compare = cat1.getDescription().compareTo(cat2.getDescription());
			break;
		case ID:
			compare = cat1.compareTo(cat2);
			break;
		}

		return sortMode.getSort() * compare;
	}
}
