package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

@Getter
public class ReferralContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String type;
    private final String sourceId;
    private final String sourceUrl;
    private final String targetType;
    private final String targetId;
    private final String campaignId;
    private final Map<String, Serializable> metadata;

    @JsonCreator
    public ReferralContext(
            @JsonProperty("type") String type,
            @JsonProperty("sourceId") String sourceId,
            @JsonProperty("sourceUrl") String sourceUrl,
            @JsonProperty("targetType") String targetType,
            @JsonProperty("targetId") String targetId,
            @JsonProperty("campaignId") String campaignId,
            @JsonProperty("metadata") Map<String, Serializable> metadata
    ) {
        this.type = type;
        this.sourceId = sourceId;
        this.sourceUrl = sourceUrl;
        this.targetType = targetType;
        this.targetId = targetId;
        this.campaignId = campaignId;
        this.metadata = metadata == null ? Collections.emptyMap() : Map.copyOf(metadata);
    }
}
