<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="config" uri="http://www.exalead.com/jspapi/config"%>
<%@ taglib prefix="render" uri="http://www.exalead.com/jspapi/render"%>
<%@ taglib prefix="widget" uri="http://www.exalead.com/jspapi/widget"%>
<%@ taglib prefix="string" uri="http://www.exalead.com/jspapi/string"%>
<%@ taglib prefix="url" uri="http://www.exalead.com/jspapi/url"%>
<%@ taglib prefix="list" uri="http://www.exalead.com/jspapi/list"%>
<%@ taglib prefix="request" uri="http://www.exalead.com/jspapi/request"%>
<%@ taglib prefix="search" uri="http://www.exalead.com/jspapi/search"%>
<%@ taglib prefix="ajax" uri="http://www.exalead.com/jspapi/ajax"%>

<render:import varWidget="widget" varFeeds="feeds" />

<widget:widget varCssId="cssId" extraCss="facetingList"
	disableStyles="true">

	<widget:header>
		<config:getOption name="title" defaultValue="" />
	</widget:header>

	<c:choose>
		<c:when test="${search:hasFeeds(feeds) == false}">
			<%-- If widget has no Feed --%>
			<render:definition name="noFeeds">
				<render:parameter name="widget" value="${widget}" />
				<render:parameter name="showSuggestion" value="false" />
			</render:definition>
			<%-- /If widget has no Feed --%>
		</c:when>

		<c:otherwise>
			<config:getOption var="facetId" name="facetId" />
			<search:getFacet var="facet" facetId="${facetId}" feeds="${feeds}" />

			<c:choose>
				<c:when test="${facet != null}">
					<div class="exa-faceting-list">
						<config:getOption var="sortMode" name="sortMode"
							defaultValue="default" />
						<config:getOption var="aggregation" name="aggregation"
							defaultValue="count" />
						<config:getOption var="enablePagination" name="enablePagination"
							defaultValue="true" />
						<config:getOption var="paginationSize" name="paginationSize"
							defaultValue="10" />
						<config:getOption var="enableFiltering" name="enableFiltering"
							defaultValue="true" />
						<config:getOptions var="forceRefineOnFeeds"
							name="forceRefineOnFeeds" />
						<config:getOption var="sourcePage" name="sourcePage"
							defaultValue="defaultPage" />
						<config:getOption var="sourceFeed" name="sourceFeed"
							defaultValue="defaultFeed" />
						<config:getOption var="localFeed" name="localFeed"
							defaultValue="defaultFeed" />
						<config:getOption var="foldUnrefined" name="foldUnrefined"
							defaultValue="false" />

						<c:set var="isDisjunctive"
							value="${facet.refinementPolicy == 'DISJUNCTIVE' }" />
						<c:set var="displayAllCategories" value="false" />

						<c:set var="refine_count" value="0" />							
						<c:set var="displayed_count" value="0" />							
						<c:set var="autre" value="" />							
						<search:forEachCategory root="${facet}" var="category_scan"
									varLevel="depthLevel" iterationMode="ALL">
								<search:getCategoryState var="state_count" category="${category_scan}" />
								<c:choose>
										<c:when test="${state_count == 'REFINED'}">
											<c:set var="refine_count" value="${refine_count + 1}" />										
										</c:when>
										<c:when test="${state_count == 'DISPLAYED'}">
											<c:set var="displayed_count" value="${displayed_count + 1}" />										
										</c:when>
										<c:otherwise>
											<c:set var="autre" value="${state_count}" />																		
										</c:otherwise>
								</c:choose>							
						</search:forEachCategory>

						<h3>
							<span class="collapsable"></span>
							<search:getFacetLabel facet="${facet}" />
							<c:set var="display_count" value=""/>
							<c:choose>
								<c:when test="${refine_count == 1}">
									<c:set var="display_count" value="${refine_count} affinage" />									
								</c:when>	
								<c:when test="${refine_count > 1}">
									<c:set var="display_count" value="${refine_count} affinages" />									
								</c:when>	
								<c:otherwise>
									<c:set var="display_count" value="" />																		
								</c:otherwise>
							</c:choose>	
							<span class="infos" style="display: inline;float:right;font-size: 10px"> ${display_count}</span>
							
						</h3>
						<div id="${cssId}_content">
							<ul>
								<c:set var="refined_list" value="" />
								
								<search:forEachCategory root="${facet}" var="category"
									varLevel="depthLevel" iterationMode="ALL">
									<search:getCategoryState var="state" category="${category}" />
									<c:choose>
										<c:when test="${state == 'REFINED'}">
											<search:getCategoryUrl var="cat_url" category="${category}"
												feeds="${feeds}" zapRefinements="true"
												forceRefineOn="${forceRefineOnFeeds}" />
											<search:getCategoryLabel var="catLabel" category="${category}" />
											
											<c:set var="refined_list" value="${refined_list},${category.getId()}" />
											
											<li class="exa-faceting-list-li refinecontainer category depthLevel_0 odd ">
												<input type="checkbox"
													 checked="checked" disabled>
														${catLabel}
											</li>

										</c:when>
										<c:otherwise>
											<c:set var="displayAllCategories" value="true" />
										</c:otherwise>
									</c:choose>

								</search:forEachCategory>
							</ul>

							
								<div id="${cssId}_categories"></div>
								<render:renderScript position="READY">
									(function () {
										<c:set var="isFirst" value="true" />	
										
										var data = [
										<search:forEachCategory root="${facet}" var="category"
										iterationMode="FLAT" sortMode="${sortMode}" varStatus="status">
										<search:getCategoryState var="state" category="${category}" />
										<c:if test="${!status.first}">,</c:if>
										<render:template template="subCategories.jsp">
											<render:parameter name="category" value="${category}" />
											<render:parameter name="aggregation" value="${aggregation}" />
											<render:parameter name="feeds" value="${feeds}" />
											<render:parameter name="isDisjunctive"
												value="${isDisjunctive}" />
											<render:parameter name="forceRefineOnFeeds"
												value="${forceRefineOnFeeds}" />
											<render:parameter name="sortMode" value="${sortMode}" />
										</render:template>
									</search:forEachCategory>
										];
										
										var list = new FacetingList(data, 										
											'${refined_list}',
											'${sourcePage}',
											'${sourceFeed}',
											'${sourceFeed}',
											'${facetId}',											
											'/mashup-ui/',
											'${localFeed}',{
											enablePagination:${enablePagination},
											paginationSize: ${paginationSize},
											enableFiltering:${enableFiltering}
										});
										list.render(document.getElementById('${cssId}_categories'));

									})();
								
								</render:renderScript>
							
						</div>
						
						<c:if test="${refine_count == 0 && foldUnrefined}">
						<render:renderScript position="READY">
								$(this).find('#${cssId}').find('.collapsable').toggleClass('collapsed');								
								$('#${cssId}_content').toggle();							
						</render:renderScript>
						</c:if>
						
						<render:renderScript position="READY">
							$('#${cssId} h3').bind('click', function () {
								$(this).find('.collapsable').toggleClass('collapsed');
								$('#${cssId}_content').toggle();
							});
						</render:renderScript>
					
						
						<render:renderScript position="READY">
							refinesWidget.initCheckboxes($('#${cssId}')); 
						</render:renderScript>
						
					</div>
				</c:when>
				<c:otherwise>
					<%-- If all feeds have no results --%>
					<config:getOption var="noResultsJspPathHit"
						name="noResultsJspPathHit"
						defaultValue="/WEB-INF/jsp/commons/noFacets.jsp" />
					<render:template template="${noResultsJspPathHit}">
						<render:parameter name="accessFeeds" value="${feeds}" />
						<render:parameter name="showSuggestion" value="true" />
					</render:template>
					<%-- /If all feeds have no results --%>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
</widget:widget>
