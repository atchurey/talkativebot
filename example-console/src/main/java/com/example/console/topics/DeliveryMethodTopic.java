package com.example.console.topics;

import com.atchurey.tools.talkativebot.core.channel.SelectedAnswer;
import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.topic.interfaces.Topic;
import com.atchurey.tools.talkativebot.springbootstarter.topic.AbstractTopic;
import com.example.console.conversations.SaleConversation;
import org.springframework.lang.NonNull;

@Topic(
		key = "delivery_method",
		name = "Delivery Method",
		description = "Choose your delivery method",
		conversation = SaleConversation.class,
		next = "shipping_address",
		order = 2
)
public class DeliveryMethodTopic extends AbstractTopic {

	public DeliveryMethodTopic(Conversation<?> conversation) {
		super(conversation);
	}

	@Override
	protected boolean canPlayWithFacts(@NonNull Facts facts) {
		return true;
	}

	@Override
	protected void doPlay(Facts facts) {
		Option[] options = new Option[]{
				new Option(0, "PICKUP"),
				new Option(1, "HOME_DELIVERY")};

		Question question = new Question("Choose your delivery method.", options);

		fact("current_topic", getKey());

		getBot().ask(conversation, this, question);
	}

	@Override
	public void onInput(SelectedAnswer selectedAnswer) {
		fact("delivery_method", selectedAnswer.getText());
		super.onInput(selectedAnswer);
	}
}