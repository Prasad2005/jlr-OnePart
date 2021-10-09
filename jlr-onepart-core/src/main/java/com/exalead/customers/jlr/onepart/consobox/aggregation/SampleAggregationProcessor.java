package com.exalead.customers.jlr.onepart.consobox.aggregation;

import org.apache.commons.logging.Log;

import com.exalead.cloudview.consolidationapi.processors.IAggregationDocument;
import com.exalead.cloudview.consolidationapi.processors.java.IJavaAllUpdatesAggregationHandler;
import com.exalead.cloudview.consolidationapi.processors.java.IJavaAllUpdatesAggregationProcessor;

import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.CVComponentDescription;
import com.exalead.mercury.component.config.CVComponentConfigClass;
import com.exalead.cloudview.consolidationapi.processors.java.GraphMatchHelpers;

@PropertyLabel(value = "Sample aggregation processor")
@CVComponentConfigClass(configClass = SampleAggregationProcessorConfig.class)
@CVComponentDescription(value = "This sample processor aggregate meta value from a specific path in the document")
public class SampleAggregationProcessor implements IJavaAllUpdatesAggregationProcessor {
	private SampleAggregationProcessorConfig config;

	public SampleAggregationProcessor(SampleAggregationProcessorConfig config) {
		this.config = config;
	}

	@Override
	public String getAggregationDocumentType() {
		return config.getType();
	}

	@Override
	public void process(IJavaAllUpdatesAggregationHandler handler, IAggregationDocument document) throws Exception {
		//TODO implement here
		// handler.matchPathEnd --> get only leaves
		// handler.match --> get all nodes in paths
		
		handler.matchPathEnd(document,config.getPath()).forEach(node->{
			document.withMeta(config.getMeta(), node.getMeta(config.getMeta()));
		});
	}
}
