package com.exalead.customers.jlr.onepart.consobox.transformation;

import com.exalead.cloudview.consolidationapi.processors.IMutableTransformationDocument;
import com.exalead.cloudview.consolidationapi.processors.java.IJavaAllUpdatesTransformationHandler;
import com.exalead.cloudview.consolidationapi.processors.java.IJavaAllUpdatesTransformationProcessor;
import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.CVComponentDescription;
import com.exalead.mercury.component.config.CVComponentConfigClass;

@PropertyLabel(value = "Sample transformation processor") 
@CVComponentConfigClass(configClass = SampleTransformationProcessorConfig.class) 
@CVComponentDescription(value = "This sample transformation processor add an arc to target document which uri is the value of the document metadata") 
public class SampleTransformationProcessor implements IJavaAllUpdatesTransformationProcessor {
	private SampleTransformationProcessorConfig config;

	public SampleTransformationProcessor(SampleTransformationProcessorConfig config) {
		this.config = config;
	}

	@Override
	public String getTransformationDocumentType() {
		return config.getType();
	}

	@Override
	public void process(IJavaAllUpdatesTransformationHandler handler, IMutableTransformationDocument document)
			throws Exception {
		//TODO implement here
		String targetURI = document.getMeta(config.getMeta());
		if (targetURI != null){
			document.addArcTo("sampleARC", targetURI);
		}
	}
}
