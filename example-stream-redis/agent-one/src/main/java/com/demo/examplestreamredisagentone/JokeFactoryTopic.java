package com.demo.examplestreamredisagentone;

import com.atchurey.tools.talkativebot.core.channel.SelectedAnswer;
import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.topic.interfaces.Topic;
import com.atchurey.tools.talkativebot.springbootstarter.topic.AbstractTopic;
import org.springframework.lang.NonNull;

@Topic(
		key = "joke_factory_topic",
		name = "Joke Factory",
		description = "Generate jokes to tell",
		canReplay = true,
		conversation = JokeFactoryConversation.class
)
public class JokeFactoryTopic extends AbstractTopic {

	private final JokeFactoryConversation conversation;

	public JokeFactoryTopic(Conversation<?> conversation) {
		super(conversation);
		this.conversation = (JokeFactoryConversation) conversation;	}

	@Override
	protected boolean canPlayWithFacts(@NonNull Facts facts) {
		return true;
	}

	@Override
	protected void doPlay(Facts facts) {
		JokeFactoryRuntime runtime = conversation.jokeFactoryRuntime();

		JokeFactoryRuntime.Joke joke;
		Question question;
		if (!facts.contains("action") || facts.get("action").equals("joke_requested")) {
			joke = runtime.generateJoke(facts.get("joke_type"));
			question = Question.choice(joke.getSetup(),
					new Option[]{
							new Option(joke.getId(), "What?"), // Give user the option to ask to deliver the punchline, use joke_id as reference
					});
		} else {
			try {
				joke = runtime.getJoke(facts.get("joke_id"));
				question = Question.text(joke.getPunchline());
			} catch (IllegalArgumentException e) {
				question = Question.text("Oh, this is embarrassing! I don't dont remember this joke.");
			}
		}

		getBot().ask(conversation, this, question);
	}

	@Override
	public void onInput(SelectedAnswer selectedAnswer) {
		if ("/Start".equalsIgnoreCase(selectedAnswer.getText()) // Conversation just started
				|| selectedAnswer.getValue() <= 0) { // user requested a joke
			fact("action", "joke_requested");
			fact("joke_type", selectedAnswer.getText()); // joke type was set in text
		} else { // Joke was setup, now deliver the punchline
			fact("action", "deliver_punchline");
			fact("joke_id", selectedAnswer.getValue());
		}
	}

}