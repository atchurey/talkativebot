package com.atchurey.tools.talkativebot.core.channel;

import java.util.Objects;

/**
 * Builds the storage key used to locate a pending interaction for an address and
 * optional workflow scope.
 * <p>
 * The default scope intentionally returns {@link ConversationAddress#persistenceKey()}
 * unchanged so existing persisted interactions remain readable after scoped
 * pending interactions are introduced.
 */
public final class PendingInteractionKey {

    private static final String SCOPE_SEPARATOR = "|scope:";

    private PendingInteractionKey() {
    }

    /**
     * Creates a pending-interaction key for the address/scope pair.
     *
     * @param address external delivery/correlation address
     * @param scope internal workflow lane; {@code null} is treated as
     *              {@link ConversationScope#DEFAULT}
     * @return legacy address key for default scope, otherwise address key with
     * the non-default scope suffix
     */
    public static String from(
            ConversationAddress address,
            ConversationScope scope
    ) {
        Objects.requireNonNull(address, "address must not be null");

        ConversationScope resolvedScope = scope == null ? ConversationScope.DEFAULT : scope;
        String addressKey = address.persistenceKey();

        if (ConversationScope.DEFAULT.equals(resolvedScope)) {
            return addressKey;
        }

        return addressKey + SCOPE_SEPARATOR + resolvedScope.value();
    }
}
