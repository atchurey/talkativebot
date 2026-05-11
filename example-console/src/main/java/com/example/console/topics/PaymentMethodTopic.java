package com.example.console.topics;

import com.atchurey.tools.talkativebot.core.channel.SelectedAnswer;
import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.topic.interfaces.Topic;
import com.atchurey.tools.talkativebot.springbootstarter.topic.AbstractTopic;
import com.example.console.conversations.SaleConversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

@Topic(
		key = "payment_method",
		name = "Payment Method",
		description = "Choose your payment method",
		conversation = SaleConversation.class,
		next = "complete_sale",
		order = 4
)
public class PaymentMethodTopic extends AbstractTopic {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public PaymentMethodTopic(Conversation<?> conversation) {
		super(conversation);
	}

	@Override
	protected boolean canPlayWithFacts(@NonNull Facts facts) {
		return true;
	}

	@Override
	protected void doPlay(Facts facts) {
		Option[] options = new Option[]{
				new Option(0, "CARD"),
				new Option(1, "MOBILE_MONEY")
		};
		Question question = new Question("Choose your payment method.", options);

		getBot().ask(conversation, this, question);
	}

	@Override
	public void onInput(SelectedAnswer selectedAnswer) {
		fact("payment_method", selectedAnswer.getText());
		super.onInput(selectedAnswer);
	}
}