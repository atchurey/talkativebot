package com.atchurey.tools.talkativebot.core.channel;

/**
 * Optional start resolver contract for conversations that should only start in selected scopes.
 */
public interface ScopedConversationStartResolver extends ConversationStartResolver {

    /**
     * Returns whether this resolver can start a conversation in the selected scope.
     *
     * @param scope scope selected by the message router
     * @return true when this resolver should be considered for the scope
     */
    boolean supportsScope(ConversationScope scope);
}
