package com.exalead.cv360.searchui.view.widgets.triggers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.exalead.access.feedapi.ResultFeed;
import com.exalead.cv360.customcomponents.CustomComponent;
import com.exalead.cv360.searchui.configuration.v10.Cell;
import com.exalead.cv360.searchui.configuration.v10.Row;
import com.exalead.cv360.searchui.configuration.v10.Table;
import com.exalead.cv360.searchui.configuration.v10.Widget;
import com.exalead.cv360.searchui.triggers.MashupWidgetTrigger;
import com.exalead.cv360.searchui.triggers.TriggerContext;
import com.exalead.cv360.searchui.view.widgets.tag.Functions;
import com.exalead.cv360.searchui.widgets.DataWidgetWrapper;

/**
 * to apply on a Widget. If the widget doesn't have result, it will be hidden
 *
 * @author i5o
 *
 */
@CustomComponent(displayName = "Remove if entries", description = "Hides the widget when its sources contain entries.")
public class RemoveIfEntries implements MashupWidgetTrigger {

	private static final Logger logger = Logger.getLogger(RemoveIfEntries.class);

	@Override
	public boolean beforeRendering(DataWidgetWrapper dww, TriggerContext triggerContext) {

		// if access feeds is provided then search for entries inside first
		if (Functions.hasResults(dww.getResultFeeds())) {
			return false;
		}

		// otherwise, retrieve page's feeds and search recursively within sub widgets if entries exists
		for (Widget widget : getWidgets(dww.getWidget())) {
			if (this.hasEntries(widget, dww.getParentResultEntry().getSubfeeds()) == true) {
				return false;
			}
		}

		// no entries found thus discard widget render
		if (logger.isDebugEnabled()) {
			logger.debug("Discarding render of widget: " + dww.getWidget().getId());
		}
		return true;
	}

	private boolean hasEntries(Widget widget, List<ResultFeed> resultFeeds) {

		// Search within self's feeds if it has entries
		List<String> feedIds = widget.getUseFeeds().getFeedsId();
		if (feedIds.isEmpty() == false) {
			for (ResultFeed resultFeed : resultFeeds) {
				if (feedIds.contains(resultFeed.getId())) {
					if (resultFeed.getTotalEntries() > 0) {
						return true;
					}
				}
			}
		}

		// If not found search within child's widgets
		for (Widget subWidget : getWidgets(widget)) {
			if (this.hasEntries(subWidget, resultFeeds) == true) {
				return true;
			}
		}

		// No entries found
		return false;
	}

	private List<Widget> getWidgets(Widget widget) {
		if (widget.getLayout() != null) {
			List<Widget> ret = new ArrayList<Widget>();
			for (Table table : widget.getLayout().getTables()) {
				for (Row row : table.getRows()) {
					for (Cell cell : row.getCells()) {
						ret.addAll(cell.getWidgets());
					}
				}
			}
			return ret;
		}
		return widget.getWidgets();
	}

	@Override
	public void afterRendering(DataWidgetWrapper dww, TriggerContext triggerContext) {

	}
}
