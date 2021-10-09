package com.exalead.cv360.searchui.view.widgets.controller.ajax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.exalead.access.client.AccessClient;
import com.exalead.access.client.AccessClientFactory;
import com.exalead.access.client.AccessParameter;
import com.exalead.access.client.AccessRequest;
import com.exalead.access.feedapi.Category;
import com.exalead.access.feedapi.Entry;
import com.exalead.access.feedapi.Facet;
import com.exalead.access.feedapi.ResultFeed;
import com.exalead.cv360.config.CM;
import com.exalead.cv360.config.NotificationListener;
import com.exalead.cv360.customcomponents.CustomComponent;
import com.exalead.cv360.searchui.config.MashupConfigurationProvider;
import com.exalead.cv360.searchui.configuration.v10.MashupUI;
import com.exalead.cv360.searchui.security.MashupSecurityManager;
import com.exalead.cv360.service.messages.Notification;
import com.exalead.cv360.service.messages.NotifyConfigurationUpdate;

@CustomComponent(displayName = "Controller getting result feed of a page filtering on feed and hits meta or facet", description = "Calls the Access page and transform result into JSON filtering on hit meta names or facet names<br>")
@Controller
public class AjaxFacetController implements NotificationListener, MessageSourceAware {

	private static final Logger logger = Logger.getLogger(AjaxFacetController.class);
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private MashupUI mashupUI = null;
	private final AccessClient accessClient = AccessClientFactory.createAccessClient(MashupConfigurationProvider
			.getInstance().getApplicationId());
	private MessageSource messageSource;

	public AjaxFacetController() throws Exception {
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

	@RequestMapping("/ajax/refinePage/**")
	public void jsonFacets(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");

		try {
			String[] fulltFacetName = request.getParameter("facetName").split("/");
			String facetName = fulltFacetName[fulltFacetName.length - 1];

			logger.debug("jsonFacets Controller. Facet: " + facetName);
			AccessRequest accessreq = this.getSimilarAccessRequest(request, "/ajax/refinePage/");
			ResultFeed resultFeed = this.accessClient.getResultFeed(accessreq);
			response.setContentType("application/json");

			ResultFeed facetFeed = getFeedFromID(getFeedName(request, "/ajax/refinePage/"), resultFeed);
			JSONObject result = facetToJSON(facetFeed.getFacet(facetName));
			response.getOutputStream().write(result.toString().getBytes());

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private JSONObject facetToJSON(Facet facet) throws JSONException {
		JSONObject jsonFacet = new JSONObject();

		jsonFacet.put("categories", new JSONArray());

		List<Category> categories = new ArrayList<Category>();
		if (facet != null && facet.getCategories() != null) {
			for (Category child : facet.getCategories())
				getLeafCategories(categories, child);
		}

		for (Category cat : categories) {
			JSONObject jsonCat = new JSONObject();
			jsonCat.put("name", cat.getDescription());
			jsonCat.put("path", cat.getPath());
			jsonCat.put("node", cat.getPath().substring(cat.getPath().lastIndexOf('/') + 1));
			jsonCat.put("state", cat.getState());
			jsonCat.put("id", cat.getId());

			jsonFacet.getJSONArray("categories").put(jsonCat);
		}

		return jsonFacet;
	}

	private void getLeafCategories(List<Category> categories, Category cat) {
		if (cat.getCategories() == null || cat.getCategories().size() == 0)
			categories.add(cat);
		else {
			for (Category child : cat.getCategories())
				getLeafCategories(categories, child);
		}
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
		logger.debug("getSimilarAccessRequest - pageName " + pageName);
		AccessRequest accessreq = new AccessRequest();
		accessreq.setPage(pageName);

		for (String param : request.getParameterMap().keySet()) {
			logger.debug("getSimilarAccessRequest - adding parameter " + param + ": "
					+ Arrays.asList(request.getParameterMap().get(param)).get(0));
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
