/**
 * 
 * Created on : 28 avril 2014
 * Author     : I5O
 * 
 */
package com.exalead.cv360.searchui.view.jspapi.custom.taglib;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

import com.exalead.cv360.customcomponents.CustomComponent;

/**
 * The Class ExaleadDateFormater.
 * 
 * This class allow to format a exalead date to a new format
 * 
 * Available parameters :
 * - url
 * - metaValue : name of the meta to sort
 * - sense : asc or desc
 * - sortParam : page parameter 
 */
@CustomComponent(displayName = "Exalead Date Formater", description = "Tag to format a date")
public class ExaleadDateFormater extends BodyTagSupport {

	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(ExaleadDateFormater.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6358269331031487503L;

	/** The var. */
	protected String var;

	/** The meta name. */
	private String metaValue;

	/** The date format. */
	private String dateFormat;

	/**
	 * Instantiates a new otrace tag.
	 */
	public ExaleadDateFormater() {
		metaValue = null;
		dateFormat = null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@Override
	public int doStartTag() throws JspException {

		if (dateFormat != null && metaValue != null) {

			String str = dateConverter(metaValue, dateFormat);

			if (str != null) {
				output(this.var, str);
			}
		}

		return super.doStartTag();
	}

	/**
	 * Output.
	 *
	 * @param varName the var name
	 * @param value the value
	 * @throws JspException the jsp exception
	 */
	public void output(String varName, Object value) throws JspException {
		if (varName == null) {
			try {
				this.pageContext.getOut().print(value);
			} catch (IOException e) {
				throw new JspException(e);
			}
		} else {
			this.exposeVariable(varName, value);
		}
	}

	/**
	 * Expose variable.
	 *
	 * @param varName the var name
	 * @param value the value
	 */
	public void exposeVariable(String varName, Object value) {
		this.exposeVariable(varName, value, PageContext.PAGE_SCOPE);
	}

	/**
	 * Expose variable.
	 *
	 * @param varName the var name
	 * @param value the value
	 * @param scope the scope
	 */
	public void exposeVariable(String varName, Object value, int scope) {
		this.pageContext.setAttribute(varName, value, scope);
	}

	/**
	 * Date converter.
	 *
	 * @param date the date
	 * @param df the df
	 * @return the string
	 */
	public static String dateConverter(String date, String format) {

		String result = date;
		int dateToFormat = 0;
		try {
			dateToFormat = Integer.parseInt(date);
		} catch (NumberFormatException e1) {
			logger.debug("Number Format Exception :[" + date + "] " + e1);
		}

		Date d = null;
		SimpleDateFormat inputFormatDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat outputFormatDate = null;
		try {
			outputFormatDate = new SimpleDateFormat(format);
		} catch (Exception e1) {
			logger.debug("format Exception :" + format);
			outputFormatDate = new SimpleDateFormat("yyyy/MM/dd");
		}

		if (dateToFormat > 706800) {

			int year = 0;
			int month = 0;

			int constYear = 372;
			int constMonth = 31;

			year = dateToFormat / constYear;
			dateToFormat -= year * constYear;
			month = dateToFormat / constMonth;

			dateToFormat -= month * constMonth;
			month += 1;
			dateToFormat += 1;

			if (dateToFormat < 10) {
				result = "0" + dateToFormat + "/";
			} else {
				result = dateToFormat + "/";
			}
			if (month < 10) {
				result += "0" + month + "/" + year;
			} else {
				result += month + "/" + year;
			}
			try {
				d = inputFormatDate.parse(result);
			} catch (ParseException e) {
				logger.error(e);
			}

			result = outputFormatDate.format(d);
		}
		return result;
	}

	/**
	 * @return the metaValue
	 */
	public String getMetaValue() {
		return metaValue;
	}

	/**
	 * @param metaValue the metaValue to set
	 */
	public void setMetaValue(String metaValue) {
		this.metaValue = metaValue;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @return the var
	 */
	public String getVar() {
		return var;
	}

	/**
	 * @param var the var to set
	 */
	public void setVar(String var) {
		this.var = var;
	}
}
