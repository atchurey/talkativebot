package com.demo.examplestreamredisagentone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@SpringBootApplication
public class AgentOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgentOneApplication.class, args);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(
			RedisConnectionFactory connectionFactory,
			ObjectMapper objectMapper) {

		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		GenericJackson2JsonRedisSerializer jsonSerializer =
				new GenericJackson2JsonRedisSerializer(objectMapper);

		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(jsonSerializer);
		template.setHashValueSerializer(jsonSerializer);

		template.afterPropertiesSet();

		return template;
	}

}
