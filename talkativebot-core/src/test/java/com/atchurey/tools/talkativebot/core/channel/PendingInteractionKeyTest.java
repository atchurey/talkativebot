package com.atchurey.tools.talkativebot.core.channel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PendingInteractionKeyTest {

    @Test
    void shouldUseLegacyAddressKeyForDefaultScope() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");

        String key = PendingInteractionKey.from(address, ConversationScope.DEFAULT);

        assertThat(key).isEqualTo("web:session:session");
    }

    @Test
    void shouldUseLegacyAddressKeyForNullScope() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");

        String key = PendingInteractionKey.from(address, null);

        assertThat(key).isEqualTo("web:session:session");
    }

    @Test
    void shouldAppendNonDefaultScope() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");

        String key = PendingInteractionKey.from(address, ConversationScope.of("checkout"));

        assertThat(key).isEqualTo("web:session:session|scope:checkout");
    }
}
