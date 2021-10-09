package com.exalead.customers.jlr.onepart.custom.mashup.trigger;

import java.util.Date;

import com.exalead.access.feedapi.AccessException;
import com.exalead.access.feedapi.Feed;
import com.exalead.access.feedapi.FeedTrigger;
import com.exalead.access.feedapi.QueryContext;
import com.exalead.access.feedapi.ResultFeed;
import com.exalead.cv360.customcomponents.CustomComponent;

@CustomComponent(displayName = "jlr-onepart Execution time trigger")
public class ExecTimeTrigger implements FeedTrigger {

	@Override
	public Result beforeQuery(Feed feed, QueryContext context) throws AccessException {
		Date d = new Date();
		context.setLocalVariable("exec_start_time", d);
		return Result.CONTINUE;
	}

	@Override
	public Result afterQuery(Feed feed, QueryContext context, ResultFeed resultFeed) throws AccessException {
		Date dStart = context.getLocalVariable("exec_start_time");
		Date dEnd = new Date();
		resultFeed.addInfo("Execution time", String.valueOf(dEnd.getTime() - dStart.getTime()));
		return Result.CONTINUE;
	}
}
