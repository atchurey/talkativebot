package com.atchurey.talkativebot.core.topic;

import com.atchurey.talkativebot.core.channel.SelectedAnswer;
import com.atchurey.talkativebot.core.conversation.Facts;

import java.io.Serializable;

public interface ConversationTopic extends Serializable {

    String getKey();

    String getName();

    String getDescription();

    int getOrder();

    String getNextTopicKey();

    boolean canReplay();

    boolean isPlayable(Facts facts);

    void play();

    void onInput(SelectedAnswer selectedAnswer);

    boolean isClosed();

    void close();

    void reset();
}