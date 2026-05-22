package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;

import java.util.Optional;

public interface OptionSelector {

    Optional<Option> select(Question question, IncomingMessage message);
}
