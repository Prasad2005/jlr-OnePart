package com.exalead.cv360.searchui.view.widgets.controller.ajax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

@CustomComponent(displayName = "Controller getting result feed of a page filtering on feed and hits meta or facet", description = "Calls the Access page and transform result into JSON filtering on hit meta names or facet names<br>"
		+ "URL of controller is http://host:baseport/mashup-ui/ajax/[result|hits|facet]/{pagename}/{feedname}?{params}<br>"
		+ "Possible parameters of controller URL :<br>"
		+ "filter: comma separated names of filters (can be meta names or facet names)<br>"
		+ "representation: 'full' or 'leaf' (for facet representation)<br>"
		+ "url: hit or facet node url to generate<br>"
		+ "faceturl: facet node url to generate<br>"
		+ "hiturl: hit url to generate<br>"
		+ "sort: type of sort (ASC OR DESC)<br>"
		+ "fieldToSort: Field used to sort the result, for example DESCRIPTION<br>"
		+ "by default, parameters are not mandatory, it is just processing options of result")
@Controller
public class AjaxJsonController implements NotificationListener, MessageSourceAware {

	private static final Logger logger = Logger.getLogger(AjaxJsonController.class);
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private MashupUI mashupUI = null;
	private final AccessClient accessClient = AccessClientFactory.createAccessClient(MashupConfigurationProvider
			.getInstance().getApplicationId());
	private MessageSource messageSource;

	public AjaxJsonController() throws Exception {
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

	@RequestMapping("/ajax/facets/**")
	public void jsonFacets(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");

		try {
			AccessRequest accessreq = this.getSimilarAccessRequest(request, "/ajax/facets/");
			ResultFeed resultFeed = this.accessClient.getResultFeed(accessreq);
			response.setContentType("application/json");

			ResultFeed facetFeed = getFeedFromID(getFeedName(request, "/ajax/facets/"), resultFeed);
			JSONObject result = facetToJSON(facetFeed.getFacet(request.getParameter("filter")),
					request.getParameter("sort"), request.getParameter("fieldToSort"));
			response.getOutputStream().write(result.toString().getBytes());

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping("/ajax/hits/**")
	public void jsonHits(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");

		try {
			AccessRequest accessreq = this.getSimilarAccessRequest(request, "/ajax/hits/");
			ResultFeed resultFeed = this.accessClient.getResultFeed(accessreq);
			response.setContentType("application/json");

			ResultFeed hitsFeed = getFeedFromID(getFeedName(request, "/ajax/hits/"), resultFeed);

			String[] filters = null;
			if (request.getParameter("filter") != null && !"".equals(request.getParameter("filter").trim())) {
				filters = request.getParameter("filter").trim().split("[,;]");
			}
			JSONObject result = hitsToJSON(hitsFeed, filters);
			response.getOutputStream().write(result.toString().getBytes());

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private JSONObject hitsToJSON(ResultFeed feed, String[] filters) throws JSONException {
		JSONObject jsonHits = new JSONObject();

		jsonHits.put("hits", new JSONArray());

		for (Entry entry : feed.getEntries()) {
			jsonHits.getJSONArray("hits").put(getJSONEntry(entry, filters));
		}

		return jsonHits;
	}

	private JSONObject facetToJSON(Facet facet, String sort, String fieldToSort) throws JSONException {
		JSONObject jsonFacet = new JSONObject();

		jsonFacet.put("categories", new JSONArray());

		List<Category> categories = new ArrayList<Category>();
		if (facet != null && facet.getCategories() != null) {
			for (Category child : facet.getCategories())
				getLeafCategories(categories, child);
		}

		// Sort categories
		CategorySortMode sortMode = CategorySortMode.getModeSort(sort);
		CategoryFieldSortable fieldSortable = CategoryFieldSortable.getField(fieldToSort);

		if (sortMode != null && fieldSortable != null) {
			logger.info("Sort categories by " + fieldToSort + " in " + sortMode);
			Collections.sort(categories, new CategorySorter(sortMode, fieldSortable));
		}

		for (Category cat : categories) {
			JSONObject jsonCat = new JSONObject();
			jsonCat.put("name", cat.getDescription());
			jsonCat.put("path", cat.getPath());
			jsonCat.put("node", cat.getPath().substring(cat.getPath().lastIndexOf('/') + 1));
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

	private JSONObject getJSONEntry(Entry entry, String[] filters) throws JSONException {
		JSONObject jsonEntry = new JSONObject();

		for (String metaName : entry.getMetas().keySet()) {
			if (filters != null) {
				for (String filter : filters) {
					if (filter.trim().equalsIgnoreCase(metaName)) {
						jsonEntry.put(metaName, entry.getMetas().get(metaName).getFirstValue());
					}
				}
			} else {
				jsonEntry.put(metaName, entry.getMetas().get(metaName).getFirstValue());
			}
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
