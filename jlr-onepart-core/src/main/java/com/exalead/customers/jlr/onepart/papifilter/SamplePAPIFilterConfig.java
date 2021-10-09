package com.exalead.customers.jlr.onepart.papifilter;

import com.exalead.config.bean.PropertyLabel;
import com.exalead.mercury.component.config.CVComponentConfigClass;
import com.exalead.papi.framework.connectors.PushAPIFilterConfig.DefaultImpl;

@PropertyLabel("Sample PAPI Filter Config")
@CVComponentConfigClass(configClass = SamplePAPIFilterConfig.class)
public class SamplePAPIFilterConfig extends DefaultImpl {
    public SamplePAPIFilterConfig() throws Exception {
        setComponentClassName(SamplePAPIFilter.class.getName());
    }
}
