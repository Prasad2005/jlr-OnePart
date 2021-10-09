package com.exalead.customers.jlr.onepart.papifilter;

import org.apache.log4j.Logger;

import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.CVComponent;
import com.exalead.mercury.component.CVComponentDescription;
import com.exalead.mercury.component.config.CVComponentConfigClass;
import com.exalead.papi.framework.connectors.PushAPIFilter;
import com.exalead.papi.helper.Document;
import com.exalead.papi.helper.Meta;
import com.exalead.papi.helper.PushAPI;
import com.exalead.papi.helper.PushAPIException;
import com.exalead.papi.helper.pipe.PipedPushAPI;

@PropertyLabel(value = "Sample PAPI Filter")
@CVComponentConfigClass(configClass = SamplePAPIFilterConfig.class)
@CVComponentDescription(value = "Sample implementation of custom PAPI filter")
public class SamplePAPIFilter extends PipedPushAPI implements CVComponent, PushAPIFilter {
    private static final Logger logger = Logger.getLogger(SamplePAPIFilter.class);
	private SamplePAPIFilterConfig config;
	
    public SamplePAPIFilter(PushAPI parent, SamplePAPIFilterConfig config) {
        super(parent);
		this.config = config;
    }

    /*
     * Process document
     */
    private void processDocument(Document document) {
		logger.debug("Process document [" + document.getUri() + "]");
        //TODO implement here processing
    }

    @Override
    public void addDocument(Document document) throws PushAPIException {
        processDocument(document);
        super.addDocument(document);
    }

    @Override
    public void addDocumentList(Document[] documents) throws PushAPIException {
		for (Document document : documents) {
			if (document != null) {
				processDocument(document);
			}
		}
        super.addDocumentList(documents);
    }
	
	/*
	 * Others methods can be implemented here to catch and override delete document, start/stop push session ... calls.
	 */
}
