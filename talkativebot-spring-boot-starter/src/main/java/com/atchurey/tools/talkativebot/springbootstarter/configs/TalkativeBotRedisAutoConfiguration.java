package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.tools.talkativebot.springbootstarter.store.redis.RedisPendingInteractionStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisOperations;

@AutoConfiguration(before = TalkativeBotAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
public class TalkativeBotRedisAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(PendingInteractionStore.class)
	@ConditionalOnBean(RedisOperations.class)
	@ConditionalOnProperty(
			prefix = "atchurey.tools.talkativebot.pending-interaction",
			name = "store",
			havingValue = "redis"
	)
	public PendingInteractionStore redisPendingInteractionStore(
			RedisOperations<String, Object> redisOperations,
			ObjectMapper objectMapper
	) {
		return new RedisPendingInteractionStore(redisOperations, objectMapper);
	}
}