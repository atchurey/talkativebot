package com.demo.examplestreamredisagenttwo;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.IncomingMessage;
import com.atchurey.tools.talkativebot.core.channel.OutgoingMessage;
import com.atchurey.tools.talkativebot.core.questions.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.util.Arrays;
import java.util.function.Consumer;

@Component
public class MessagingStream {
	private final Logger logger = LoggerFactory.getLogger(MessagingStream.class);

	final StreamBridge streamBridge;

	public MessagingStream(StreamBridge streamBridge) {
		this.streamBridge = streamBridge;
	}

	@Bean
	public Consumer<OutgoingMessage> agentTwoInput() {
		return talkativeBotOutputMessage -> {
			// This could happen if TalkativeBot couldn't resolve an input message,
			// For example, if the user input didn't match any pending interactions (invalid input from user?)
			// And if the input message didn't match any ConversationStartResolver.
			// In this case, the error might be returned in talkativeBotOutputMessage.getText()
			if (talkativeBotOutputMessage.getQuestion() == null) {
				logger.info("STREAM: TalkativeBot couldn't continue/start Conversation:{}", talkativeBotOutputMessage.getText());
				return;
			}

			if (talkativeBotOutputMessage.getQuestion().isChoice()) {
				logger.info("STREAM: Joke: {}", talkativeBotOutputMessage.getQuestion().getText());
			} else {
				logger.info("STREAM: Punchline: {}\n", talkativeBotOutputMessage.getQuestion().getText());
			}

			// Here you can process the message from TalkativeBot
			// Example: You could send this message to a third party bot or chat like (Facebook Messenger, Whatsapp, Slack, ...)
			// Then wait for the response from the third party bot or chat
			// And then send the response back to TalkativeBot
			String userInput;
			try {
				Thread.sleep(1000);
				// In place of a real user input, let's always respond with the first option
				Option firstOption = Arrays.stream(talkativeBotOutputMessage.getQuestion().getOptions()).findFirst().orElse(new  Option(0, ""));
				userInput = String.valueOf(firstOption.getValue());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			// Send the response back to TalkativeBot
			ConversationAddress channelAddress = new ConversationAddress(
					"stream", // The channel type. To use the Sprinng Cloud Stream input/output channel provided by TalkativeBot, set this to "spring-cloud-stream" or "stream".
					"some-user-id", // Your user identifier
					"some-session-id", // The user's session identifier'
					"some-conversation-id" // A unique conversation identifier
			);

			// Message is sent to the input channel of TalkativeBot
			IncomingMessage incomingMessage = new IncomingMessage(channelAddress, userInput);
			streamBridge.send("agentTwoOutput-out-0", incomingMessage, MimeType.valueOf("application/json"));
		};
	}
	

}
