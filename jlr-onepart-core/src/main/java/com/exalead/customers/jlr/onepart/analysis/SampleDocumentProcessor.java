package com.exalead.customers.jlr.onepart.analysis;

import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.CVComponent;
import com.exalead.mercury.component.CVComponentDescription;
import com.exalead.mercury.component.config.CVComponentConfigClass;
import com.exalead.pdoc.ProcessableDocument;
import com.exalead.pdoc.analysis.CustomDocumentProcessor;
import com.exalead.pdoc.analysis.DocumentProcessingContext;

@PropertyLabel(value = "Sample custom document processor")
@CVComponentConfigClass(configClass = SampleDocumentProcessorConfig.class)
@CVComponentDescription(value = "Copy metadata into other context names")
public class SampleDocumentProcessor extends CustomDocumentProcessor implements CVComponent {
	//private static final Log logger = LogFactory.getLog(SampleDocumentProcessor.class);
	private SampleDocumentProcessorConfig config;

	public SampleDocumentProcessor(SampleDocumentProcessorConfig config) throws Exception {
		super(config);
		this.config = config;
	}

	@Override
	public void process(DocumentProcessingContext ctx, ProcessableDocument document) throws Exception {
		//TODO implement here 
		document.addMeta(config.getParam(), "A value");
	}
}
