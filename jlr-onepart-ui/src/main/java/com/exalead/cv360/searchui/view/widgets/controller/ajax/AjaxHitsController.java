/**
 * 
 * Created on : 21 juin 2014
 * Author     : i5o
 * 
 */
package com.exalead.cv360.searchui.view.widgets.controller.ajax;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.exalead.access.client.AccessClient;
import com.exalead.access.client.AccessClientFactory;
import com.exalead.access.client.AccessParameter;
import com.exalead.access.client.AccessRequest;
import com.exalead.access.feedapi.Entry;
import com.exalead.access.feedapi.ResultFeed;
import com.exalead.cv360.config.CM;
import com.exalead.cv360.config.NotificationListener;
import com.exalead.cv360.config.elements.CustomComponentParameter;
import com.exalead.cv360.customcomponents.CustomComponent;
import com.exalead.cv360.searchui.config.MashupConfigurationProvider;
import com.exalead.cv360.searchui.config.MashupConfigurationResolver;
import com.exalead.cv360.searchui.configuration.v10.MashupUI;
import com.exalead.cv360.searchui.configuration.v10.Widget;
import com.exalead.cv360.searchui.security.MashupSecurityManager;
import com.exalead.cv360.searchui.view.widgets.controller.export.CloudviewDateUtils;
import com.exalead.cv360.service.messages.Notification;
import com.exalead.cv360.service.messages.NotifyConfigurationUpdate;

@CustomComponent(displayName = "Controller getting result feed of a page filtering on feed and hits meta", description = "Calls the Access page and transform result into JSON filtering on hit meta names or facet names<br>"
		+ "URL of controller is http://host:baseport/mashup-ui/ajax/hitsenrich/{pagename}/{feedname}?{params}<br>"
		+ "Possible parameters of controller URL :<br>"
		+ "filter: comma separated names of filters (can be meta names or facet names)<br>"
		+ "representation: 'full' or 'leaf' (for facet representation)<br>"
		+ "url: hit  node url to generate<br>"
		+ "faceturl: facet node url to generate<br>"
		+ "hiturl: hit url to generate<br>"
		+ "sort: type of sort (ASC OR DESC)<br>"
		+ "fieldToSort: Field used to sort the result, for example DESCRIPTION<br>"
		+ "by default, parameters are not mandatory, it is just processing options of result")
@Controller
public class AjaxHitsController implements NotificationListener, MessageSourceAware {

	private static final Logger logger = Logger.getLogger(AjaxHitsController.class);
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private MashupUI mashupUI = null;
	private final AccessClient accessClient = AccessClientFactory.createAccessClient(MashupConfigurationProvider
			.getInstance().getApplicationId());
	private MessageSource messageSource;

	public AjaxHitsController() throws Exception {
		this.init();
		CM.registerNotificationListener(this);
	}

	private void init() throws Exception {
		this.lock.writeLock().lock();
		try {
			this.mashupUI = MashupConfigurationProvider.getInstance().loadApplicationConfiguration(MashupUI.class);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public void handleNotification(Notification notification) throws Exception {
		if (notification instanceof NotifyConfigurationUpdate) {
			if (((NotifyConfigurationUpdate) notification).isUpdated(MashupConfigurationProvider.getInstance()
					.getApplicationId(), MashupUI.class)) {
				this.init();
			}
		}
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@RequestMapping("/ajax/hitsenrich/**")
	public void jsonHits(HttpServletRequest request, HttpServletResponse response, @RequestParam("wuid") String wuid,
			@RequestParam("page") String page) throws Exception {
		response.setCharacterEncoding("UTF-8");
		try {
			String[] lst = page.split("/");
			String pageName = lst[lst.length - 1];
			AccessRequest accessreq = this.getSimilarAccessRequest(request, "/ajax/hitsenrich/");
			Widget widget;
			this.lock.readLock().lock();
			try {
				widget = MashupConfigurationResolver.getWidgetByWUID(this.mashupUI, pageName, wuid);
				if (widget == null) {
					throw new Exception("Widget with given wuid doesn't exist");
				}
			} finally {
				this.lock.readLock().unlock();
			}

			Map<String, String> typeOfOperation = new HashMap<>();
			Map<String, String> formatters = new HashMap<>();
			if (widget != null && widget.getParameter("format") != null) {
				for (CustomComponentParameter param : widget.getParameter("format")) {
					typeOfOperation.put(param.getValue().split("\\|\\|")[0], param.getValue().split("\\|\\|")[1]);
					formatters.put(param.getValue().split("\\|\\|")[0], param.getValue().split("\\|\\|")[2]);
				}
			}

			ResultFeed resultFeed = this.accessClient.getResultFeed(accessreq);
			response.setContentType("application/json");

			ResultFeed hitsFeed = getFeedFromID(getFeedName(request, "/ajax/hitsenrich/"), resultFeed);

			JSONObject result = hitsToJSON(hitsFeed, typeOfOperation, formatters, request);
			response.getOutputStream().write(result.toString().getBytes());

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private JSONObject hitsToJSON(ResultFeed feed, Map<String, String> typeOfOperation, Map<String, String> formatters,
			HttpServletRequest request) throws JSONException {
		JSONObject jsonHits = new JSONObject();

		jsonHits.put("hits", new JSONArray());

		for (Entry entry : feed.getEntries()) {
			jsonHits.getJSONArray("hits").put(getJSONEntry(entry, typeOfOperation, formatters, request));
		}

		return jsonHits;
	}

	private JSONObject getJSONEntry(Entry entry, Map<String, String> typeOfOperation, Map<String, String> formatters,
			HttpServletRequest request) throws JSONException {
		JSONObject jsonEntry = new JSONObject();

		for (String metaName : entry.getMetas().keySet()) {

			String firstValue = entry.getMetas().get(metaName).getFirstValue();

			if (typeOfOperation.containsKey(metaName) && !StringUtils.isEmpty(firstValue)) {
				String typeOfFormat = typeOfOperation.get(metaName);
				String typeFormat = formatters.get(metaName);

				if (typeOfFormat.contains("date")) {
					firstValue = CloudviewDateUtils.dateConverter(firstValue, typeFormat);
				} else if (typeOfFormat.contains("operation")) {
					if (Double.parseDouble(firstValue) >= 0) {
						firstValue = "yes";
					} else {
						firstValue = "no";
					}

				} else if (typeOfFormat.contains("format")) {
					DecimalFormat format = new DecimalFormat(typeFormat, new DecimalFormatSymbols(
							RequestContextUtils.getLocale(request)));
					firstValue = format.format(Double.parseDouble(firstValue));
				}
			}

			jsonEntry.put(metaName, firstValue);
		}

		return jsonEntry;
	}

	private String getFeedName(HttpServletRequest request, String subpath) {
		String pageName = request.getRequestURI().substring(request.getContextPath().length() + subpath.length());
		String feedName = pageName.substring(pageName.lastIndexOf('/') + 1);

		if (feedName.indexOf('?') != -1)
			return feedName.substring(0, feedName.indexOf('?'));

		return feedName;
	}

	private ResultFeed getFeedFromID(String name, ResultFeed feed) {
		if (feed.getId().equals(name))
			return feed;
		else {
			for (Entry entry : feed.getEntries()) {
				if (entry.getSubfeeds() != null) {
					for (ResultFeed sub : entry.getSubfeeds()) {
						ResultFeed matchingfeed = getFeedFromID(name, sub);
						if (matchingfeed != null)
							return matchingfeed;
					}
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private AccessRequest getSimilarAccessRequest(HttpServletRequest request, String subpath) {
		String pageName = request.getRequestURI().substring(request.getContextPath().length() + subpath.length());
		AccessRequest accessreq = new AccessRequest();
		accessreq.setPage(pageName);

		for (String param : request.getParameterMap().keySet()) {
			accessreq.addParameters(param, Arrays.asList(request.getParameterMap().get(param)));
		}

		if (MashupSecurityManager.getInstance().isLoggedIn(request.getSession(false))) {
			List<String> securityTokens = MashupSecurityManager.getSecurityModel(request.getSession()).getTokens();
			if (securityTokens != null && securityTokens.isEmpty() == false) {
				accessreq.addParameters(AccessParameter.SECURITY, securityTokens);
			}
		} else if (request.getAttribute(AccessParameter.SECURITY) != null) {
			accessreq.addParameters(AccessParameter.SECURITY,
					((List<String>) request.getAttribute(AccessParameter.SECURITY)));
		}
		return accessreq;
	}

}
