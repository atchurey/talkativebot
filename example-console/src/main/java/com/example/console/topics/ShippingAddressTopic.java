package com.example.console.topics;

import com.atchurey.tools.talkativebot.core.channel.SelectedAnswer;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.topic.interfaces.Topic;
import com.atchurey.tools.talkativebot.springbootstarter.topic.AbstractTopic;
import com.example.console.conversations.SaleConversation;

@Topic(
		key = "shipping_address",
		name = "Shipping Address",
		description = "Collects user's shipping address",
		conversation = SaleConversation.class,
		next = "payment_method",
		order = 3
)
public class ShippingAddressTopic extends AbstractTopic {

	@Override
	protected void doPlay(Facts facts) {
		// We can also ask free input questions. No need for options then
		ask(Question.text("What is your shipping address?: "));
	}

	@Override
	public void onInput(SelectedAnswer selectedAnswer) {
		fact("shipping_address", selectedAnswer.getRawInput());
		super.onInput(selectedAnswer);
	}
}