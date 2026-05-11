package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.tools.talkativebot.springbootstarter.store.jpa.JpaPendingInteractionRepository;
import com.atchurey.tools.talkativebot.springbootstarter.store.jpa.JpaPendingInteractionStore;
import com.atchurey.tools.talkativebot.springbootstarter.store.jpa.PendingInteractionEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration(before = TalkativebotAutoConfiguration.class)
@ConditionalOnClass({
        EntityManager.class,
        JpaRepository.class,
})
@EntityScan(basePackageClasses = { PendingInteractionEntity.class})
@EnableJpaRepositories(basePackageClasses = { JpaPendingInteractionRepository.class })
public class TalkativebotJpaAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(PendingInteractionStore.class)
	@ConditionalOnProperty(
			prefix = "atchurey.tools.talkativebot.pending-interaction",
			name = "store",
			havingValue = "database"
	)
	public PendingInteractionStore jpaPendingInteractionStore(
			JpaPendingInteractionRepository repository,
			ObjectMapper objectMapper
	) {
		return new JpaPendingInteractionStore(repository, objectMapper);
	}
}
