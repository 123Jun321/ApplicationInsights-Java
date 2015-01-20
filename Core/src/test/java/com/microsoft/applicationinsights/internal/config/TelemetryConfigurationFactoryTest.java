package com.microsoft.applicationinsights.internal.config;

import java.util.Map;
import java.util.HashMap;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.channel.concrete.inprocess.InProcessTelemetryChannel;
import com.microsoft.applicationinsights.internal.channel.stdout.StdOutChannel;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class TelemetryConfigurationFactoryTest {

    private final static String MOCK_CONF_FILE = "mockFileName";
    private final static String MOCK_IKEY = "c9341531-05ac-4d8c-972e-36e97601d5ff";
    private final static String MOCK_ENDPOINT = "MockEndpoint";
    private final static String FACTORY_INSTRUMENTATION_KEY = "InstrumentationKey";
    private final static String LOGGER_SECTION = "SDKLogger";
    private final static String CHANNEL_SECTION = "Channel";
    private final static String CLASS_TYPE_AS_ATTRIBUTE = "type";

    @Test
    public void testInitializeWithFailingParse() throws Exception {
        ConfigFileParser mockParser = createMockParserThatFailsToParse();

        TelemetryConfiguration mockConfiguration = new TelemetryConfiguration();

        initializeWithFactory(mockParser, mockConfiguration);

        assertEquals(mockConfiguration.isTrackingDisabled(), false);
        assertTrue(mockConfiguration.getChannel() instanceof InProcessTelemetryChannel);
    }

    @Test
    public void testInitializeWithNullGetInstrumentationKey() throws Exception {
        ConfigFileParser mockParser = createMockParser(false, true, null);
        Mockito.doReturn(null).when(mockParser).getTrimmedValue(FACTORY_INSTRUMENTATION_KEY);
        ConfigFileParser.StructuredDataResult channelResult = new ConfigFileParser.StructuredDataResult();
        Mockito.doReturn(channelResult).when(mockParser).getStructuredData(CHANNEL_SECTION, CLASS_TYPE_AS_ATTRIBUTE);

        TelemetryConfiguration mockConfiguration = new TelemetryConfiguration();

        initializeWithFactory(mockParser, mockConfiguration);

        assertEquals(mockConfiguration.isTrackingDisabled(), false);
        assertTrue(mockConfiguration.getChannel() instanceof InProcessTelemetryChannel);
    }

    @Test
    public void testInitializeWithEmptyGetInstrumentationKey() throws Exception {
        ConfigFileParser mockParser = createMockParser(false, true, null);
        ConfigFileParser.StructuredDataResult loggerResult = new ConfigFileParser.StructuredDataResult();
        ConfigFileParser.StructuredDataResult channelResult = new ConfigFileParser.StructuredDataResult();
        Mockito.doReturn("").when(mockParser).getTrimmedValue(FACTORY_INSTRUMENTATION_KEY);
        Mockito.doReturn(channelResult).when(mockParser).getStructuredData(LOGGER_SECTION, null);
        Mockito.doReturn(loggerResult).when(mockParser).getStructuredData(CHANNEL_SECTION, CLASS_TYPE_AS_ATTRIBUTE);

        TelemetryConfiguration mockConfiguration = new TelemetryConfiguration();

        initializeWithFactory(mockParser, mockConfiguration);

        assertEquals(mockConfiguration.isTrackingDisabled(), false);
        assertTrue(mockConfiguration.getChannel() instanceof InProcessTelemetryChannel);
    }

    @Test
    public void testInitializeAllDefaults() throws Exception {
        ConfigFileParser mockParser = createMockParser(true, true, null);
        Mockito.doReturn(MOCK_IKEY).when(mockParser).getTrimmedValue(FACTORY_INSTRUMENTATION_KEY);

        TelemetryConfiguration mockConfiguration = new TelemetryConfiguration();

        initializeWithFactory(mockParser, mockConfiguration);

        assertEquals(mockConfiguration.isTrackingDisabled(), false);
        assertEquals(mockConfiguration.getInstrumentationKey(), MOCK_IKEY);
        assertEquals(mockConfiguration.getContextInitializers().size(), 2);
        assertTrue(mockConfiguration.getTelemetryInitializers().isEmpty());
        assertTrue(mockConfiguration.getChannel() instanceof StdOutChannel);
    }

    @Test
    public void testDefaultChannelWithData() {
        HashMap<String, String> channelItems = new HashMap<String, String>();
        channelItems.put("DeveloperMode", "true");
        ConfigFileParser mockParser = createMockParserWithDefaultChannel(true, channelItems);
        Mockito.doReturn(MOCK_IKEY).when(mockParser).getTrimmedValue(FACTORY_INSTRUMENTATION_KEY);

        TelemetryConfiguration mockConfiguration = new TelemetryConfiguration();

        initializeWithFactory(mockParser, mockConfiguration);

        assertEquals(mockConfiguration.getChannel().isDeveloperMode(), true);
    }

    private ConfigFileParser createMockParserThatFailsToParse() {
        ConfigFileParser mockParser = Mockito.mock(ConfigFileParser.class);
        Mockito.doReturn(false).when(mockParser).parse(MOCK_CONF_FILE);
        return mockParser;
    }

    private ConfigFileParser createMockParserWithDefaultChannel(boolean withChannel, Map<String, String> data) {
        return createMockParser(withChannel, false, data);
    }

        // Suppress non relevant warning due to mockito internal stuff
    @SuppressWarnings("unchecked")
    private ConfigFileParser createMockParser(boolean withChannel, boolean setChannel, Map<String, String> data) {
        ConfigFileParser mockParser = Mockito.mock(ConfigFileParser.class);
        Mockito.doReturn(true).when(mockParser).parse(MOCK_CONF_FILE);

        ConfigFileParser.StructuredDataResult loggerResult = new ConfigFileParser.StructuredDataResult();
        Mockito.doReturn(loggerResult).when(mockParser).getStructuredData(LOGGER_SECTION, null);
        if (withChannel) {
            Map<String, String> mockChannel = new HashMap<String, String>();
            mockChannel.put("EndpointAddress", MOCK_ENDPOINT);

            Map<String, String> channelItems = data != null ? data : new HashMap<String, String>();

            String channelType = null;
            if (setChannel) {
                channelType = "com.microsoft.applicationinsights.internal.channel.stdout.StdOutChannel";
            }
            ConfigFileParser.StructuredDataResult channelResult = new ConfigFileParser.StructuredDataResult(channelType, channelItems);
            Mockito.doReturn(channelResult).when(mockParser).getStructuredData(CHANNEL_SECTION, CLASS_TYPE_AS_ATTRIBUTE);
        }

        return mockParser;
    }

    private void initializeWithFactory(ConfigFileParser mockParser, TelemetryConfiguration mockConfiguration) {
        TelemetryConfigurationFactory.INSTANCE.setParserData(mockParser, MOCK_CONF_FILE);
        TelemetryConfigurationFactory.INSTANCE.initialize(mockConfiguration);
    }
}