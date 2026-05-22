package com.example.console.topics;

import com.atchurey.tools.talkativebot.core.channel.SelectedAnswer;
import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.topic.interfaces.Topic;
import com.atchurey.tools.talkativebot.springbootstarter.topic.AbstractTopic;
import com.example.console.conversations.CheckoutConversation;
import org.springframework.lang.NonNull;

@Topic(
		key = "greetings",
		name = "Greet Customer",
		description = "Say hello to customer",
		conversation = CheckoutConversation.class,
		next = "cart_review",
		order = 0
)
public class GreetingsTopic extends AbstractTopic {

	public GreetingsTopic(Conversation<?> conversation) {
		super(conversation);
	}

	@Override
	protected boolean canPlayWithFacts(@NonNull Facts facts) {
		return true;
	}

	@Override
	protected void doPlay(Facts facts) {

		Option[] options = new Option[]{
				new Option(0, "Check out"),
				new Option(1, "Not yet")};

		Question question = new Question(
				"Hi! How was shopping today?! Can I help you checkout?", options);

		getBot().ask(conversation, this, question);
	}

	@Override
	public void onInput(SelectedAnswer selectedAnswer) {
		if (0 == selectedAnswer.getValue()) {
			fact("agreed_to_checkout", true);
			super.onInput(selectedAnswer);
		} else {
			abandonConversation();
		}
	}
}