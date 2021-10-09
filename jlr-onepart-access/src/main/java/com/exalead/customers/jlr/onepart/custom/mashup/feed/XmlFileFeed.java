package com.exalead.customers.jlr.onepart.custom.mashup.feed;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.exalead.access.client.ResultFeedHandler;
import com.exalead.access.feedapi.AccessException;
import com.exalead.access.feedapi.Entry;
import com.exalead.access.feedapi.Feed;
import com.exalead.access.feedapi.QueryContext;
import com.exalead.access.feedapi.ResultFeed;
import com.exalead.access.feedapi.v10.Parameter;
import com.exalead.access.feedapi.v10.Parameter.Arity;
import com.exalead.cv360.customcomponents.CustomComponent;

@CustomComponent(displayName = "jlr-onepart XML loader feed", description="jlr-onepart - Load result feed from file")
public class XmlFileFeed extends Feed {

	private static final Log logger = LogFactory.getLog(XmlFileFeed.class);
	private static final String GROUP_NAME = "jlr-onepart";
	
	@Override
	public String getDisplayName() {
		return "jlr-onepart XML loader feed";
	}
	
	@Override
	public String getGroup() {
		return GROUP_NAME;
	}

	@Override
	public ResultFeed execute(QueryContext context) throws AccessException {
		String fileLocation = this.getEvaluatedParameter(context, "location");
		String feedid = this.getEvaluatedParameter(context, "feedid");
		ResultFeed result = null;
		
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			InputStream inputStream= new FileInputStream(fileLocation);
			Reader reader = new InputStreamReader(inputStream,"UTF-8");
			ResultFeedHandler resHandler = new ResultFeedHandler(xr);
			xr.setContentHandler(resHandler);
			xr.setErrorHandler(resHandler);
			
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			xr.parse(is);
			
			if ( feedid == null || feedid.trim().equals("") )
				result = resHandler.getResultFeed();
			else
				result = getFeedFromId(resHandler.getResultFeed(),feedid);
		} catch (Exception e) {
			logger.error("Error loading result feed from file [" + fileLocation + "]",e);
		}
		
		if ( result != null ){
			result.setId(this.getId());
		}	
		
		return result;
	}

	private ResultFeed getFeedFromId (ResultFeed feed, String id){
		if ( feed.getId().equals(id) )
			return feed;
		
		if ( feed.getEntries() != null ){
			for ( Entry entry : feed.getEntries() ){
				if ( entry.getSubfeeds() != null ){
					for ( ResultFeed subfeed : entry.getSubfeeds() ){
						ResultFeed ff = getFeedFromId(subfeed, id);
						if ( ff != null )
							return ff;
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public ResultFeed get(QueryContext context, String id)
			throws AccessException {
		return null;
	}

	@Override
	protected Parameter[] getSupportedParameters() {
		return new Parameter[] {
				new Parameter("location", Arity.ONE,
						Parameter.Override.OVERRIDABLE_IN_QUERY, false),
				new Parameter("feedid", Arity.ZERO_OR_ONE,
						Parameter.Override.OVERRIDABLE_IN_QUERY, false)
						
		};
	}

}
