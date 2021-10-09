package com.exalead.customers.jlr.onepart.custom.mashup.trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.exalead.access.feedapi.AccessException;
import com.exalead.access.feedapi.AggregationValue;
import com.exalead.access.feedapi.Category;
import com.exalead.access.feedapi.Facet;
import com.exalead.access.feedapi.Feed;
import com.exalead.access.feedapi.FeedTrigger;
import com.exalead.access.feedapi.QueryContext;
import com.exalead.access.feedapi.ResultFeed;
import com.exalead.cv360.config.elements.CustomComponentConfiguration;
import com.exalead.cv360.customcomponents.CustomComponent;
import com.exalead.cv360.customcomponents.ParameterDescription;
import com.exalead.cv360.customcomponents.ParameterDescription.Arity;
import com.exalead.cv360.customcomponents.ParameterDescription.ContextMenuBehavior;

@CustomComponent(displayName = "jlr-onepart Cumulate values", description = "Cumulate aggregation values of facets.", parameters = {
		@ParameterDescription(displayName = "Facet name", name = "facet", arity = Arity.ONE, defaultValues = "", description = "Name of the facet", contextMenuBehaviors = ContextMenuBehavior.FACETS),
		@ParameterDescription(displayName = "Aggregation function(s)", name = "aggregations", arity = Arity.MANY, defaultValues = "", description = "Aggregation functions to cumulate") })
public class CumulateValuesTrigger implements FeedTrigger {

	private static final Log logger = LogFactory
			.getLog(CumulateValuesTrigger.class);
	private final String facet;
	private final List<String> aggregations;

	public CumulateValuesTrigger(CustomComponentConfiguration conf) {
		facet = conf.getParameterValue("facet");
		aggregations = conf.getParameterValues("aggregations");
	}

	@Override
	public Result beforeQuery(Feed feed, QueryContext context)
			throws AccessException {
		return Result.CONTINUE;
	}

	@Override
	public Result afterQuery(Feed feed, QueryContext context,
			ResultFeed resultFeed) throws AccessException {
		Facet facetObj = resultFeed.getFacet(facet);

		if (facetObj != null) {
			double total = 0d;
			String aggName = aggregations.get(0);

			Map<String, Double> cumulatedValues = new HashMap<String, Double>();

			// get first level categories
			for (Category catLevel1 : facetObj.getCategories()) {
				total += catLevel1.getAggregations().get(aggName)
						.getValueAsDouble();
				AggregationValue aggTotal = new AggregationValue(total);
				catLevel1.getAggregations().put("cumulated_" + aggName,
						aggTotal);

				if (catLevel1.getCategories() != null
						&& catLevel1.getCategories().size() > 0) {
					List<Category> categories = new ArrayList<>();
					getCategoriesLeaves(catLevel1, categories);

					for (Category catLeaf : categories) {
						double newValue = catLeaf.getAggregations()
								.get(aggName).getValueAsDouble();
						logger.debug("Category [" + catLeaf.getId()
								+ "] aggregation value [" + newValue + "]");
						if (cumulatedValues.containsKey(catLeaf
								.getDescription())) {
							newValue += cumulatedValues.get(catLeaf
									.getDescription());
						}
						cumulatedValues.put(catLeaf.getDescription(), newValue);
						AggregationValue aggNew = new AggregationValue(newValue);
						catLeaf.getAggregations().put("cumulated_" + aggName,
								aggNew);
						logger.debug("Category [" + catLeaf.getId()
								+ "] aggregation value cumulated [" + newValue
								+ "]");
					}
				}
			}

			AggregationValue aggFacet = new AggregationValue(total);
			facetObj.getAggregations().put("cumulated_" + aggName, aggFacet);
		} else {
			logger.warn("Aggregate values --> Facet [" + facet
					+ "] not found in feed (aggregation ignored)");
		}

		return Result.CONTINUE;
	}

	private void getCategoriesLeaves(Category cat, List<Category> categories) {
		if (cat.getCategories() != null && cat.getCategories().size() > 0) {
			for (Category child : cat.getCategories()) {
				getCategoriesLeaves(child, categories);
			}
		} else {
			categories.add(cat);
		}
	}
}
