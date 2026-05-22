package com.demo.examplestreamredisagenttwo;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.IncomingMessage;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@SpringBootApplication
public class AgentTwoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgentTwoApplication.class, args);
	}

	@Component
	public static class JokeLoverConversationRunner implements ApplicationRunner {

		private final StreamBridge streamBridge;
		public JokeLoverConversationRunner(StreamBridge streamBridge) {
			this.streamBridge = streamBridge;
		}

		@Override
		public void run(ApplicationArguments args) {

			// Agent-Two (The Joke Lover Service) ----spring cloud stream channel----> Agent-One (The Joke Factory Service)
			// Agent-Two as soon as the application starts, will manually trigger a JokeLoverConversation.
			// An initial message reaches Agent-One who generates and sends a joke to Agent-Two, and there the show begins.
			ConversationAddress channelAddress = new ConversationAddress(
					"stream", // The channel type. To use the Sprinng Cloud Stream input/output channel provided by TalkativeBot, set this to "spring-cloud-stream" or "stream".
					"some-user-id", // Your user identifier
					"some-session-id", // The user's session identifier'
					"some-conversation-id" // A unique conversation identifier
			);

			IncomingMessage incomingMessage = new IncomingMessage(channelAddress, "/Start");
			streamBridge.send("agentTwoOutput-out-0", incomingMessage, MimeType.valueOf("application/json"));

		}
	}
}
