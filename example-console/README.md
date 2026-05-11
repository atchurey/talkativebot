# `TalkativeBot Console/In-memory Store Example`


## What we have here
A simple console demo to play a checkout flow. We will use an in-memory store for pending interactions and play
the conversation manually.

### Highlighted Features
- Defining a simple conversation (SaleConversation)
- Defining topics for a checkout flow, topics are annoteted with `@Topic`
- TalkativeBot properties configuration for this scenario
- Spring Boot starter for `TalkativeBot` autoconfigures the bot, scanning for topics for the conversation
- An in-memory store for pending interactions is autoconfigured
- Console input/output channels autoconfigured


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

## TalkativeBot Configuration

```properties
atchurey.tools.talkativebot.pending-interaction.store=memory
atchurey.tools.talkativebot.topic-base-package=com.example.console.topics
atchurey.tools.talkativebot.channels.enabled=true
atchurey.tools.talkativebot.channels.console-enabled=true
```
> **Important:** Configure `atchurey.tools.talkativebot.topic-base-package`
> to enable topic scanning.

## Run This Demo
You can manually build and run or use the command below.
```bash
./mvnw spring-boot:run -pl :example-console-memory -am -DskipTests
```

