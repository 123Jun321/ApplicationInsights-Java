package com.microsoft.applicationinsights.datacontracts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.applicationinsights.implementation.*;
import com.microsoft.applicationinsights.util.MapUtil;

import com.google.common.base.Strings;

/**
 * Represents a context for sending telemetry to the Application Insights service.
 */
public class TelemetryContext implements JsonSerializable
{
    private Map<String,String> properties;
    private Map<String,String> tags;

    private String instrumentationKey;
    private ComponentContext component;
    private DeviceContext device;
    private SessionContext session;
    private UserContext user;
    private OperationContext operation;
    private LocationContext location;
    private InternalContext internal;

    public TelemetryContext()
    {
        // TODO: create a SnapshottingMap, just like the .NET version has.
        this(new HashMap<String, String>(), new HashMap<String, String>());
    }

    public TelemetryContext(Map<String, String> properties, Map<String, String> tags)
    {
        if (properties == null)
            throw new IllegalArgumentException("properties cannot be null");
        if (tags == null)
            throw new IllegalArgumentException("tags cannot be null");
        this.properties = properties;
        this.tags = tags;
    }

    public ComponentContext getComponent()
    {
        if (component == null) component = new ComponentContext(tags);
        return component;
    }

    public DeviceContext getDevice()
    {
        if (device == null) device = new DeviceContext(tags);
        return device;
    }

    public SessionContext getSession()
    {
        if (session == null) session = new SessionContext(tags);
        return session;
    }

    public UserContext getUser()
    {
        if (user == null) user = new UserContext(tags);
        return user;
    }

    public OperationContext getOperation()
    {
        if (operation == null) operation = new OperationContext(tags);
        return operation;
    }

    public LocationContext getLocation()
    {
        if (location == null) location = new LocationContext(tags);
        return location;
    }

    public InternalContext getInternal()
    {
        if (internal == null) internal = new InternalContext(tags);
        return internal;
    }

    public String getInstrumentationKey()
    {
        return instrumentationKey;
    }

    public void setInstrumentationKey(String instrumentationKey)
    {
        this.instrumentationKey = instrumentationKey;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public Map<String, String> getTags()
    {
        return tags;
    }

    @Override
    public void serialize(JsonWriter writer) throws IOException
    {
        writer.writeProperty("iKey", this.getInstrumentationKey());
        writer.writeProperty("device", this.getDevice());
        writer.writeProperty("application", this.getComponent());
        writer.writeProperty("user", this.getUser());
        writer.writeProperty("operation", this.getOperation());
        writer.writeProperty("session", this.getSession());
        writer.writeProperty("location", this.getLocation());
        writer.writeProperty("internal", this.getInternal());
    }

    public void Initialize(TelemetryContext source)
    {
        if (!Strings.isNullOrEmpty(source.getInstrumentationKey()))
            setInstrumentationKey(source.getInstrumentationKey());

        if (source.tags != null && source.tags.size() > 0)
        {
            MapUtil.copy(source.tags, this.tags);
        }
        if (source.properties != null && source.properties.size() > 0)
        {
            MapUtil.copy(source.properties, this.properties);
        }
    }
}
