package com.atchurey.tools.talkativebot.core.topic;

import java.util.Objects;

public class TopicDescriptor {

	private final String key;
	private final String name;
	private final String description;
	private final boolean canReplay;
	private final Class<?> conversationType;
	private final String nextTopicKey;
	private final int order;
	private final Class<? extends ConversationTopic> topicType;

	public TopicDescriptor(
			String key,
			String name,
			String description,
			boolean canReplay,
			Class<?> conversationType,
			String nextTopicKey,
			int order,
			Class<? extends ConversationTopic> topicType
	) {
		this.key = Objects.requireNonNull(key, "key must not be null");
		this.name = name;
		this.description = description;
		this.canReplay = canReplay;
		this.conversationType = Objects.requireNonNull(conversationType, "conversationType must not be null");
		this.nextTopicKey = nextTopicKey;
		this.order = order;
		this.topicType = Objects.requireNonNull(topicType, "topicType must not be null");
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isCanReplay() {
		return canReplay;
	}

	public Class<?> getConversationType() {
		return conversationType;
	}

	public String getNextTopicKey() {
		return nextTopicKey;
	}

	public int getOrder() {
		return order;
	}

	public Class<? extends ConversationTopic> getTopicType() {
		return topicType;
	}
}