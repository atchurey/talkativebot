package com.demo.examplestreamredisagentone;

import com.atchurey.tools.talkativebot.core.channel.IncomingMessage;
import com.atchurey.tools.talkativebot.core.channel.InputMessageHandler;
import com.atchurey.tools.talkativebot.springbootstarter.channels.stream.SpringCloudStreamInputConsumer;
import com.atchurey.tools.talkativebot.springbootstarter.channels.stream.SpringCloudStreamOutputChannel;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class MessagingStream {

	@Bean
	SpringCloudStreamOutputChannel sendToAgentTwo(StreamBridge streamBridge) {
		return new SpringCloudStreamOutputChannel(streamBridge, "agentOneOutput-out-0");
	}

	@Bean
	public Consumer<IncomingMessage> agentOneInput(InputMessageHandler inputMessageHandler) {
		return new SpringCloudStreamInputConsumer(inputMessageHandler);
	}

}
