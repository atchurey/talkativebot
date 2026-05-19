# TalkativeBot

> A Java 17 / Spring Boot 3 library for building stateful conversation flows across multiple channels.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green.svg)](https://spring.io/projects/spring-boot)

## Table of Contents

- [Overview](#overview)
- [What It Is Not](#what-it-is-not)
- [Why TalkativeBot?](#why-talkativebot)
- [Key Features](#key-features)
- [Modules](#modules)
- [Requirements](#requirements)
- [Installation](#installation)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Core Concepts](#core-concepts)
    - [Conversations](#conversations)
    - [Topics](#topics)
    - [Channels](#channels)
- [Persistence](#persistence)
- [Examples](#examples)
- [License](#license)

## Overview

TalkativeBot provides a conversation orchestration layer that decouples your business logic from chat platforms,
messaging systems, and AI engines. Build structured, stateful conversations once and deploy them across multiple
channels without rewriting your interaction logic.

## What It Is Not
TalkativeBot is **not a workflow engine, chatbot platform, or AI framework**. It's a lightweight Java/Spring conversation state manager for human-in-the-loop workflows. It focuses on asking questions, persisting pending interactions, resuming conversations, and keeping transport-specific code outside business logic.

## Architecture

TalkativeBot acts as a central orchestration hub that decouples business conversation logic from the underlying messaging channels and infrastructure.

## Why TalkativeBot?

Getting your application AI-ready is not only about plugging in a model or adding a chatbot. A good first step is giving
your product a clean way to hold structured, stateful conversations—making AI-driven bots just another client of your
application.

### The Problem

Many business flows eventually need human interaction:

- Collecting missing information
- Confirming a decision
- Selecting from available options
- Approving or rejecting an action
- Clarifying an exception
- Resuming a workflow after a user response

When this logic is handled directly inside controllers, services, message consumers, or bot-specific integrations, the
application quickly becomes coupled to a particular channel or provider.

**Don't shoot yourself in the foot** in your attempt to bring AI capabilities to your users by messing up your already
complex business logic.

### The Solution

TalkativeBot provides a conversation orchestration layer between your application and the bot engines, messaging
platforms, and AI tools around it.

Wherever your business flow needs human input, you can delegate that interaction to a bot while keeping your core
application logic independent from the transport layer.

**TalkativeBot helps you model interactions as conversations:**

- Ask a question
- Wait for an answer
- Persist the pending interaction
- Resume the flow when the answer arrives
- Route messages through the appropriate channel

**The goal is not to replace AI engines or chat platforms.** The goal is to give your application a predictable layer
for managing conversations across them—providing these abstractions without tying your conversation logic to a specific
transport.

## Key Features

- **Conversation Flow Abstraction** – Define complex, stateful conversations as reusable components
- **Multi-Channel Support** – Deploy the same conversation across Console, REST API, Spring Cloud Stream, and custom
  channels
- **Flexible Persistence** – Choose from in-memory, Redis, or JPA storage, or implement your own
- **Question/Answer Handling** – Built-in support for text input, single/multiple choice, and custom question types
- **Spring Boot Integration** – Auto-configuration and starter for seamless integration
- **Extensible Architecture** – Add custom channels (WhatsApp, Telegram, Facebook Messenger) and storage backends

## Modules

| Module                             | Purpose                                         |
|------------------------------------|-------------------------------------------------|
| `talkativebot-core`                | Framework-independent conversation engine       |
| `talkativebot-spring-boot-starter` | Spring Boot auto-configuration and integrations |
| `talkativebot-docs`                | Reference documentation (Asciidoctor)           |

## Documentation

Comprehensive reference documentation is available in the `talkativebot-docs` module.

### Building Documentation

To generate the HTML reference guide and aggregate Javadocs, run:

```bash
./mvnw clean prepare-package javadoc:aggregate -DskipTests
```

The generated documentation will be available at:
- Reference Guide: `talkativebot-docs/target/generated-docs/index.html`
- Javadocs: `target/site/apidocs/index.html`

## Requirements
- Java 17+
- Spring Boot 3.0+

## Installation
### Maven

```xml
<dependency>
    <groupId>com.atchurey.tools</groupId>
    <artifactId>talkativebot-spring-boot-starter</artifactId>
    <version>0.0.3-SNAPSHOT</version>
</dependency>
```


## Spring Boot Configuration

```properties
atchurey.tools.talkativebot.pending-interaction.store=memory | redis | database
atchurey.tools.talkativebot.pending-interaction.ttl=30m
atchurey.tools.talkativebot.topic-base-package=com.example.basepackage
atchurey.tools.talkativebot.channels.enabled=true
atchurey.tools.talkativebot.channels.console-enabled=true
```
> **Important:** Configure `atchurey.tools.talkativebot.topic-base-package`
> to enable topic scanning.

## Persistence Options

1. `memory` - In-memory store.
2. `redis` - Redis store.
3. `database` - JPA store.

## Quick Start
Manually play a conversation by calling `TalkativeBot.play(...)`.
```java
@Autowired
TalkativeBot talkativebot;

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
Conversations are stateful. A `Conversation` orchestrates the flow based on the associated `Topic` definitions. It is defined by a class that extends `AbstractConversation`. 
You can simply only override the `onTopicPlayed` method to handle topic events. If you need to handle conversation-level flow completion, 
override the `onConversationClosed` callback,

```java
public class SaleConversation extends AbstractConversation<String> {

    public SaleConversation(TalkativeBot bot, ConversationAddress address) {
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
						new Option(1, "CASH")
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