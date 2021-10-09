package com.exalead.cv360.searchui.view.jspapi.custom.taglib;

import javax.servlet.jsp.JspException;

import com.exalead.access.feedapi.AbstractCategory;
import com.exalead.cv360.customcomponents.CustomComponent;
import com.exalead.cv360.searchui.view.jspapi.common.I18NUtils;
import com.exalead.cv360.searchui.view.jspapi.tagext.TagSupport;

@CustomComponent(displayName = "Get the pass leaf", description = "Get the pass leaf")
public class CategoryGetPathLeaf extends TagSupport {
	private static final long serialVersionUID = -4949410193752318971L;
	private String var;
	private AbstractCategory<?> category;
	private String categoryPath;

	@Override
	protected void init() {
		super.init();
		this.var = null;
		this.category = null;
		this.categoryPath = null;
	}

	@Override
	public int doStartTag() throws JspException {
		String path = null;
		if (this.category != null) {
			path = getLeaf(this.category.getPath());
		} else if (this.categoryPath != null) {
			path = getLeaf(this.categoryPath);
		}
		if (path != null) {
			output(this.var, i18n(I18NUtils.getCodeForCategory(path), path));
		}
		return super.doStartTag();
	}

	public void setVar(String var) {
		this.var = var;
	}

	public void setCategory(AbstractCategory<?> category) {
		this.category = category;
	}

	public void setCategoryPath(String categoryPath) {
		this.categoryPath = categoryPath;
	}

	private String getLeaf(String path) {
		if (path.lastIndexOf('/') != -1) {
			return path.substring(path.lastIndexOf('/') + 1);
		}
		return path;
	}
}
