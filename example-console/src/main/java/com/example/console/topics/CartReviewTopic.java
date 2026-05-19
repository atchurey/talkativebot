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
		key = "cart_review",
		name = "Cart Review",
		description = "Review your cart",
		conversation = CheckoutConversation.class,
		next = "delivery_method",
		order = 1
)
public class CartReviewTopic extends AbstractTopic {

	public CartReviewTopic(Conversation<?> conversation) {
		super(conversation);
	}

	@Override
	protected boolean canPlayWithFacts(@NonNull Facts facts) {
		// This is a good place to determine if this topic should play (or be skipped)
		// depending on your business logic and based on the available facts.
		return true;
	}

	@Override
	protected void doPlay(Facts facts) {
		// Here we prepare what question to ask the user. To determine what this topic should ask,
		// you can perform whatever actions necessary here like making API calls to your business logic
		Option[] options = new Option[]{
				new Option(0, "Yes"),
				new Option(1, "Not Yet")};

		Question question = new Question("Your cart is ready for checkout. Let's proceed?", options);

		// You can always add whatever facts you need to the conversation's facts.
		fact("current_topic", getKey());

		getBot().ask(conversation, this, question);
	}

	@Override
	public void onInput(SelectedAnswer selectedAnswer) {
		// You can immediately access the selected answer here and do whatever you want with it,
		// including saving it to the conversation's facts so that it can be used later, perhaps
		// in a different topic or at the end of the conversation.

		fact("cart_reviewed", true);
		if (0 == selectedAnswer.getValue()) {
			// If the topic played satisfactorily, remember to call super.onInput(selectedAnswer)
			// to close this topic and move to the next one.
			fact("proceed_to_checkout", true);
			super.onInput(selectedAnswer);
		} else {
			fact("proceed_to_checkout", false);
			// Don't close, leave the topic open
		}
	}

}