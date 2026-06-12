package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ExternalIdentity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String provider;
    private final String externalUserId;
    private final String displayName;
    private final String phone;
    private final String email;

    @JsonCreator
    public ExternalIdentity(
            @JsonProperty("provider") String provider,
            @JsonProperty("externalUserId") String externalUserId,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email
    ) {
        this.provider = provider;
        this.externalUserId = externalUserId;
        this.displayName = displayName;
        this.phone = phone;
        this.email = email;
    }
}
