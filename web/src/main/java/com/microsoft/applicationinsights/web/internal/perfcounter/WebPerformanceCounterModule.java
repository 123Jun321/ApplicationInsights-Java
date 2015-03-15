package com.microsoft.applicationinsights.web.internal.perfcounter;

import com.microsoft.applicationinsights.internal.annotation.PerformanceModule;
import com.microsoft.applicationinsights.internal.perfcounter.AbstractPerformanceCounterModuleBase;

/**
 * Created by gupele on 3/12/2015.
 */
@PerformanceModule("BuiltIn")
public final class WebPerformanceCounterModule extends AbstractPerformanceCounterModuleBase {
    public WebPerformanceCounterModule() {
        this(new DefaultWebPerformanceCountersFactory());
    }

    WebPerformanceCounterModule(WebPerformanceCountersFactory factory) {
        super(factory);
    }
}
