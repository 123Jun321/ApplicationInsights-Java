package com.microsoft.applicationinsights.internal.channel.samplingV2;

import com.microsoft.applicationinsights.agent.internal.common.StringUtils;
import com.microsoft.applicationinsights.extensibility.TelemetryProcessor;
import com.microsoft.applicationinsights.internal.annotation.BuiltInProcessor;
import com.microsoft.applicationinsights.internal.logger.InternalLogger;
import com.microsoft.applicationinsights.telemetry.SupportSampling;
import com.microsoft.applicationinsights.telemetry.Telemetry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dhaval Doshi Oct 2017
 * This processor is used to Perform Sampling on User specified sampling rate
 *
 * How to use in ApplicationInsights Configuration :
 *
 * <BuiltInProcessors>
     <Processor type = "FixedRateSamplingTelemetryProcessor">
         <Add name = "SamplingPercentage" value = "50" />
         <Add name = "ExcludedTypes" value="Trace" />
        <Add name = "IncludedTypes" value="Trace;Request" />
     </Processor>
 </BuiltInProcessors>
 */
@BuiltInProcessor("FixedRateSamplingTelemetryProcessor")
public final class FixedRateSamplingTelemetryProcessor implements TelemetryProcessor {

    private final String dependencyTelemetryName = "Dependency";
    private static final String eventTelemetryName = "Event";
    private static final String exceptionTelemetryName = "Exception";
    private static final String pageViewTelemetryName = "PageView";
    private static final String requestTelemetryName = "Request";
    private static final String traceTelemetryName = "Trace";

    private static final String listSeparator = ";";
    private static Map<String, Class> allowedTypes;

    private String excludedTypesString;
    private Set<Class> excludedTypesHashSet;

    private String includedTypesString;
    private Set<Class> includedTypesHashSet;

    /// All sampling percentage must be in a ratio of 100/N where N is a whole number (2, 3, 4, …). E.g. 50 for 1/2 or 33.33 for 1/3.
    /// Failure to follow this pattern can result in unexpected / incorrect computation of values in the portal.
    private double samplingPercentage;

    /**
     * constructor is responsible of initializing this processor
     * to default settings
     */
    public FixedRateSamplingTelemetryProcessor() {
        this.samplingPercentage = 100.00;
        this.includedTypesHashSet = new HashSet<Class>();
        this.excludedTypesHashSet = new HashSet<Class>();
        try {
            this.allowedTypes = new HashMap<String, Class>() {{
                put(dependencyTelemetryName, Class.forName("com.microsoft.applicationinsights.telemetry.RemoteDependencyTelemetry"));
                put(eventTelemetryName, Class.forName("com.microsoft.applicationinsights.telemetry.EventTelemetry"));
                put(exceptionTelemetryName, Class.forName("com.microsoft.applicationinsights.telemetry.ExceptionTelemetry"));
                put(pageViewTelemetryName, Class.forName("com.microsoft.applicationinsights.telemetry.PageViewTelemetry"));
                put(requestTelemetryName, Class.forName("com.microsoft.applicationinsights.telemetry.RequestTelemetry"));
                put(traceTelemetryName, Class.forName("com.microsoft.applicationinsights.telemetry.TraceTelemetry"));
            }};
        } catch (ClassNotFoundException e) {
            InternalLogger.INSTANCE.trace("Unable to locate telemetry classes");
        }
    }

    /**
     * This method returns a set of classes of excluded types specified by user
     *
     * @return
     */
    public Set<Class> getExcludedTypesHashSet() {
        return excludedTypesHashSet;
    }

    /**
     * This method returns a set of classes of included types specified by user
     *
     * @return
     */
    public Set<Class> getIncludedTypesHashSet() {
        return includedTypesHashSet;
    }

    /**
     * This method takes a string of user specified excluded
     * types and parses it to into set containing respective references
     *
     * @param value
     */
    public void setExcludedTypes(String value) {
        this.excludedTypesString = value;
        Set<Class> newExcludedTypesHashSet = new HashSet<Class>();
        setIncludedOrExcludedTypes(value, newExcludedTypesHashSet);
        excludedTypesHashSet = newExcludedTypesHashSet;
    }

    /**
     * This method takes a string of user specified included
     * types and parses it to into set containing respective references
     *
     * @param value
     */
    public void setIncludedTypes(String value) {
        this.includedTypesString = value;
        Set<Class> newIncludedTypesHashSet = new HashSet<Class>();
        setIncludedOrExcludedTypes(value, newIncludedTypesHashSet);
        includedTypesHashSet = newIncludedTypesHashSet;
    }

    private void setIncludedOrExcludedTypes(String value, Set<Class> typeSet) {
        if (!StringUtils.isNullOrEmpty(value)) {
            String[] splitList = value.split(listSeparator);
            for (String item : splitList) {
                item.trim();
                if (!StringUtils.isNullOrEmpty(item) && allowedTypes.containsKey(item)) {
                    typeSet.add(allowedTypes.get(item));
                } else {
                    InternalLogger.INSTANCE.error("item is either not allowed to sample or is empty");
                }
            }
        }
    }

    /**
     * Gets the sample rate currently set
     */
    double getSamplingPercentage() {
        return samplingPercentage;
    }

    /**
     * Sets the user defined sampling percentage
     *
     * @param samplingPercentage
     */
    public void setSamplingPercentage(String samplingPercentage) {
        this.samplingPercentage = Double.valueOf(samplingPercentage);
    }

    /**
     * This method determines if the telemetry needs to be sampled or not.
     *
     * @param telemetry
     * @return
     */
    @Override
    public boolean process(Telemetry telemetry) {

        double samplingPercentage = this.samplingPercentage;

        if (telemetry instanceof SupportSampling) {

            if (isSamplingApplicable(telemetry.getClass())) {

                SupportSampling samplingSupportingTelemetry = ((SupportSampling) telemetry);

                if (samplingSupportingTelemetry.getSamplingPercentage() == null) {

                    samplingSupportingTelemetry.setSamplingPercentage(samplingPercentage);

                    if (SamplingScoreGeneratorV2.getSamplingScore(telemetry) >= samplingPercentage) {

                        InternalLogger.INSTANCE.info("Item %s sampled out", telemetry.getClass());
                        return false;
                    }
                } else {
                    InternalLogger.INSTANCE.info("Item has sampling percentage already set to :"
                            + samplingSupportingTelemetry.getSamplingPercentage());
                }
            } else {
                InternalLogger.INSTANCE.trace("Skip sampling since %s type is not sampling applicable", telemetry.getClass());
            }
        }

        return true;
    }

    /**
     * Determines if the argument is applicable for sampling
     *
     * @param item : Denotes the class item to be determined applicable for sampling
     * @return boolean
     */
    private boolean isSamplingApplicable(Class item) {

        if (excludedTypesHashSet.size() > 0 && excludedTypesHashSet.contains(item)) {
            return false;
        }

        if (includedTypesHashSet.size() > 0 && !includedTypesHashSet.contains(item)) {
            return false;
        }

        return true;
    }
}
