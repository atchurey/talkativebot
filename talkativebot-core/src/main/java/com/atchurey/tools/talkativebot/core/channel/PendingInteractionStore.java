package com.atchurey.tools.talkativebot.core.channel;

import java.time.Duration;
import java.util.Optional;

public interface PendingInteractionStore {

    void save(PendingInteraction interaction);

    void save(PendingInteraction interaction, Duration ttl);

    Optional<PendingInteraction> findByAddress(ConversationAddress address);

    void deleteByAddress(ConversationAddress address);
}
