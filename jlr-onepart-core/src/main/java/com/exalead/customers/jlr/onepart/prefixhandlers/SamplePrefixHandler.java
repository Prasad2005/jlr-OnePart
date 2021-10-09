package com.exalead.customers.jlr.onepart.prefixhandlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.CVComponent;
import com.exalead.mercury.component.CVComponentDescription;
import com.exalead.mercury.component.config.CVComponentConfigClass;
import com.exalead.search.query.QueryContext;
import com.exalead.search.query.QueryProcessingException;
import com.exalead.search.query.node.AlphanumValue;
import com.exalead.search.query.node.Node;
import com.exalead.search.query.node.NodeVisitor;
import com.exalead.search.query.node.Or;
import com.exalead.search.query.node.PrefixNode;
import com.exalead.search.query.node.UserQueryChunk;
import com.exalead.search.query.prefix.CustomPrefixHandler;
import com.exalead.search.query.util.NodeInspector;

@PropertyLabel(value = "Sample prefix handler")
@CVComponentConfigClass(configClass = SamplePrefixHandlerConfig.class)
@CVComponentDescription(value = "This sample prefix handler dispatch query parameter on several fields and add pattern search by prefix but lower relevance")
public class SamplePrefixHandler extends CustomPrefixHandler implements CVComponent {
	private static Log LOGGER = LogFactory.getLog(SamplePrefixHandler.class);
	private SamplePrefixHandlerConfig config;

	public SamplePrefixHandler(SamplePrefixHandlerConfig config) {
		super(config);
		this.config = config;
	}

	@Override
	public Node handlePrefix(Phase phase, PrefixNode node, NodeVisitor visitor, QueryContext context)
			throws QueryProcessingException {
		if (phase == Phase.POST_PARSE) {
			Node resultNode = null;
			String query = getValue(node).toLowerCase();

			LOGGER.info("Query content [" + query + "]");
			List<Node> nodes = new ArrayList<>();
			resultNode = new Or();
			for (String fieldName : this.config.getFieldName()) {
				AlphanumValue val1 = new AlphanumValue(fieldName, query);
				val1.setPatternSearch(true);
				val1.setWeight(1000);
				nodes.add(val1);

				AlphanumValue val2 = new AlphanumValue(fieldName, query + "*");;
				val2.setPatternSearch(true);
				val2.setWeight(10);
				nodes.add(val2);
			}

			((Or) resultNode).setContent(nodes);
			return resultNode;
		} else {
			return node;
		}
	}

	private String getValue(PrefixNode node) {
		for (UserQueryChunk chunk : NodeInspector.userQueryChunks(node)) {
			return chunk.value;
		}

		return null;
	}
}
