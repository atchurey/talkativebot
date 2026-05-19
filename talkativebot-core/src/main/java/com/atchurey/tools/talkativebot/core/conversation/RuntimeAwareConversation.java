package com.atchurey.tools.talkativebot.core.conversation;

public interface RuntimeAwareConversation {

    void setRuntime(ConversationRuntime runtime);

    ConversationRuntime getRuntime();
}