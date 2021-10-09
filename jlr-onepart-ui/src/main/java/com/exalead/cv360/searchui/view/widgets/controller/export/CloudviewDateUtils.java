package com.exalead.cv360.searchui.view.widgets.controller.export;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class CloudviewDateUtils {

	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(CloudviewDateUtils.class);

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

}
