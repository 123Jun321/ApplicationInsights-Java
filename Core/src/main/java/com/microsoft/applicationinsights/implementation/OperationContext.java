package com.microsoft.applicationinsights.implementation;

import com.microsoft.applicationinsights.datacontracts.*;
import com.microsoft.applicationinsights.extensibility.model.ContextTagKeys;
import com.microsoft.applicationinsights.util.MapUtil;

import java.io.IOException;
import java.util.Map;

public class OperationContext implements JsonSerializable
{
    private final Map<String, String> tags;

    public OperationContext(Map<String, String> tags)
    {
        this.tags = tags;
    }

    String getId()
    {
        return MapUtil.getValueOrNull(tags, ContextTagKeys.getKeys().getOperationId());
    }

    public void setId(String id)
    {
        MapUtil.setStringValueOrRemove(tags, ContextTagKeys.getKeys().getOperationId(), id);
    }

    String getName()
    {
        return MapUtil.getValueOrNull(tags, ContextTagKeys.getKeys().getOperationName());
    }

    public void setName(String name)
    {
        MapUtil.setStringValueOrRemove(tags, ContextTagKeys.getKeys().getOperationName(), name);
    }

    @Override
    public void serialize(com.microsoft.applicationinsights.datacontracts.JsonWriter writer) throws IOException
    {
        writer.writeStartObject();
        writer.writeProperty("id", this.getId());
        writer.writeProperty("name", this.getName());
        writer.writeEndObject();
    }
}