# `Talkativebot`

`Talkativebot` is a Java 17 / Spring Boot 3 library for building stateful conversation flows across multiple channels.

## Why?

Getting your application AI-ready is not only about plugging in a model or adding a chatbot. A good first step is giving your product a clean way to hold structured, stateful conversations.
The goal is to make AI-driven bots just another client of your application.

Many business flows eventually need human interaction:

- collecting missing information
- confirming a decision
- selecting from available options
- approving or rejecting an action
- clarifying an exception
- resuming a workflow after a user response

If this logic is handled directly inside controllers, services, message consumers, or bot-specific integrations, the application quickly becomes coupled to a particular channel or provider.
Do not shoot yourself in the foot in your attempt to bring AI capabilities to your users by messing up your already complex business logic.

`Talkativebot` provides a conversation orchestration layer between your application and the bot engines, messaging platforms, and AI tools around it.

Wherever your business flow needs human input, you can delegate that interaction to a bot while keeping your core application logic independent from the transport layer.

``Talkativebot`` helps you model the interaction as a conversation:

- ask a question
- wait for an answer
- persist the pending interaction
- resume the flow when the answer arrives
- route messages through the appropriate channel

The goal is not to replace AI engines or chat platforms. The goal is to give your application a predictable layer for managing conversations across them.
`Talkativebot` provides these abstractions without tying your conversation logic to a specific transport.

## Modules

| Module | Purpose |
|---|---|
| talkativebot-core | Framework-independent conversation engine |
| talkativebot-spring-boot-starter | Spring Boot auto-configuration and integrations |

## Features

- Conversation flow abstraction
- Question/answer handling
- Pending interaction persistence
- Spring Boot auto-configuration
- Console channel implementation for quick testing
- Spring Cloud Stream channel implementation and integration
- Rest API channel implementation
- Ability to add custom channels (eg. WhatsApp, Facebook Messenger, Telegram, etc.)
- Default in-memory store for development/testing
- Redis store implementation
- JPA store implementation
- Ability to add custom stores (eg. File, DynamoDB, etc.)

## Requirements
- Java 17+
- Spring Boot 3.0+

## Installation
### Maven

```xml
<dependency>
    <groupId>com.atchurey.tools</groupId>
    <artifactId>talkativebot-spring-boot-starter</artifactId>
    <version>0.0.2</version>
</dependency>
```


## Spring Boot Configuration

```properties
atchurey.tools.talkativebot.pending-interaction.store=memory | redis | jpa
atchurey.tools.talkativebot.pending-interaction.ttl=30m
atchurey.tools.talkativebot.topic-base-package=com.example.basepackage
atchurey.tools.talkativebot.channels.enabled=true
atchurey.tools.talkativebot.channels.console-enabled=true
```
> **Important:** Configure `atchurey.talkativebot.tools.topic-base-package`
> to enable topic scanning.

## Persistence Options

1. `memory` - In-memory store.
2. `redis` - Redis store.
3. `jpa` - JPA store.

## Quick Start
Manually play a conversation by calling `Talkativebot.play(...)`.
```java
@Autowired
Talkativebot talkativebot;

ConversationAddress consoleAddress = new ConversationAddress(
		"console",
		"console-user-1",
		"console-session-1",
		"conversation-id-1" 
);
talkativebot.play(new SaleConversation(talkativebot, consoleAddress));
```

Or automatically trigger a conversation flow when a user sends a message to the bot by implementing a `ConversationStartResolver`.
```java
@Component
public class SaleConversationStartResolver implements ConversationStartResolver {

    @Override
    public Optional<ConversationStartRequest> resolve(IncomingMessage message) {
        if (!"/start".equalsIgnoreCase(message.getText().trim())) {
            return Optional.empty();
        }

        return Optional.of(new ConversationStartRequest(
                message.getAddress(),
                SaleConversation.class.getName(),
                "/start",
                Map.of(
                        "source_channel", message.getAddress().getChannel(),
                        "external_user_id", valueOrEmpty(message.getAddress().getUserId())
                )
        ));
    }

    private Serializable valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
```

## Conversation Sample
Conversations are stateful. A `Conversation` orchestratrates the flow based on the associated `Topic` definitions. It is defined by a class that extends `AbstractConversation`. 
You can simply only override the `onTopicPlayed` method to handle topic events. If you need to handle conversation-level flow completion, 
override the `onConversationClosed` callback,

```java
public class SaleConversation extends AbstractConversation<String> {

    public SaleConversation(`Talkativebot` bot, ConversationAddress address) {
        super(bot, address);
    }

    @Override
    protected CompletableFuture<String> onTopicPlayed(ConversationTopic topic) {
        if (isClosed()) {
            return onConversationClosed();
        }

        return CompletableFuture.completedFuture("Played topic: " + topic.getKey());
    }

    @Override
    protected CompletableFuture<String> onConversationClosed() {
        return bot.reply("Sale conversation completed.");
    }
    
}
```

## Topic Sample

For a given conversation, you can define multiple topics. Each topic is a stateful conversation flow. 
1. The topic `key` is used to identify the topic in the conversation state.
2. The topic `name` can be a user/developer friendly text only used for display.
3. The topic `description` is to provide a brief description of the topic.
4. The topic `conversation` is the class that this `Topic` is part of.
5. The topic `canReplay` flag indicates whether the topic can be replayed after it has been closed/completed.
6. The topic `next` is the key of the next topic to be played.
7. The topic `order` is the order in which the topic is played (when using `next`, `order` is ignored).

```java
@Topic(
		key = "payment_method",
		name = "Payment Method",
		description = "Choose your payment method",
		conversation = SaleConversation.class,
		canReplay = true,
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
		Question question = new Question("Choose your payment method.",
				new Option[]{
						new Option(0, "CARD"),
						new Option(1, "MOBILE_MONEY")
				}
		);

		getBot().ask(conversation, this, question);
	}

	@Override
	public void onInput(SelectedAnswer selectedAnswer) {
		logger.info("User selected payment method: {}", selectedAnswer.getText());
	}
}
```

## License