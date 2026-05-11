package com.atchurey.talkativebot.core.channel;

import com.atchurey.talkativebot.core.questions.Option;
import com.atchurey.talkativebot.core.questions.Question;

import java.util.Optional;

public interface OptionSelector {

    Optional<Option> select(Question question, IncomingMessage message);
}
