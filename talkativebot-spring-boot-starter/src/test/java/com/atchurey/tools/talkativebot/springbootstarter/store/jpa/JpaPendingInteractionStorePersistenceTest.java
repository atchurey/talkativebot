package com.atchurey.tools.talkativebot.springbootstarter.store.jpa;

import com.atchurey.tools.talkativebot.core.channel.*;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

/** Real repository and transaction boundaries: mocks cannot detect managed primary-key mutation. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop", showSql = false)
@ContextConfiguration(classes = JpaPendingInteractionStorePersistenceTest.Config.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaPendingInteractionStorePersistenceTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = PendingInteractionEntity.class)
    @EnableJpaRepositories(basePackageClasses = JpaPendingInteractionRepository.class)
    static class Config {
        @Bean ObjectMapper objectMapper() { return Jackson2ObjectMapperBuilder.json().build(); }
        @Bean JpaPendingInteractionStore store(JpaPendingInteractionRepository repository, ObjectMapper mapper) {
            return new JpaPendingInteractionStore(repository, mapper);
        }
    }

    @Autowired JpaPendingInteractionStore store;
    @Autowired JpaPendingInteractionRepository repository;
    final ConversationAddress address = new ConversationAddress("web", "buyer", "session");
    final ConversationScope scope = ConversationScope.of("order");

    @BeforeEach void clearRows() { repository.deleteAllInBatch(); }

    @Test void expiredQuestionIsReplacedWithNewIdPayloadAndExpiry() {
        PendingInteraction old = question("old");
        store.save(old, scope, Duration.ofMinutes(20));
        var row = repository.findById(old.getId()).orElseThrow();
        row.setExpiresAt(Instant.parse("2000-01-01T00:00:00Z"));
        repository.saveAndFlush(row);
        assertThat(store.findByAddress(address, scope)).isEmpty();
        PendingInteraction next = question("next");
        Instant before = Instant.now();
        store.save(next, scope, Duration.ofMinutes(30));
        assertThat(repository.existsById(old.getId())).isFalse();
        assertThat(repository.count()).isEqualTo(1);
        var replacement = repository.findById(next.getId()).orElseThrow();
        assertThat(replacement.getExpiresAt()).isAfterOrEqualTo(before.plus(Duration.ofMinutes(30)));
        assertThat(replacement.getPayloadJson()).contains(next.getId()).doesNotContain(old.getId());
        var restored = store.findByAddress(address, scope).orElseThrow();
        assertThat(restored.getId()).isEqualTo(next.getId());
        assertThat(restored.getQuestion().getText()).isEqualTo("next");
        assertThat((String) restored.getFacts().get("answer")).isEqualTo("next");
        assertThat(restored.getCreatedAt()).isEqualTo(next.getCreatedAt());
    }

    @Test void activeReplacementPreservesOtherScopesAndAddresses() {
        var old = question("old"); store.save(old, scope, Duration.ofMinutes(20));
        var otherScope = ConversationScope.of("other");
        var other = question("other"); store.save(other, otherScope, Duration.ofMinutes(20));
        var otherAddress = new ConversationAddress("web", "another", "another-session");
        var separate = new PendingInteraction(otherAddress, "conversation", "topic", Question.text("separate"), new Facts());
        store.save(separate, scope);
        var next = question("next"); store.save(next, scope);
        assertThat(repository.existsById(old.getId())).isFalse();
        assertThat(store.findByAddress(address, scope).orElseThrow().getId()).isEqualTo(next.getId());
        assertThat(store.findByAddress(address, otherScope).orElseThrow().getId()).isEqualTo(other.getId());
        assertThat(store.findByAddress(otherAddress, scope).orElseThrow().getId()).isEqualTo(separate.getId());
        assertThat(repository.count()).isEqualTo(3);
        assertThat(repository.findById(next.getId()).orElseThrow().getExpiresAt()).isNull();
    }

    @Test void failedInsertionRollsBackDeletionOfPreviousQuestion() {
        var old = question("old"); store.save(old, scope, Duration.ofMinutes(20));
        var invalid = new PendingInteraction(address, "x".repeat(600), "topic", Question.text("invalid"), new Facts());
        assertThatThrownBy(() -> store.save(invalid, scope, Duration.ofMinutes(20))).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(repository.existsById(invalid.getId())).isFalse();
        assertThat(repository.count()).isEqualTo(1);
        assertThat(store.findByAddress(address, scope).orElseThrow().getId()).isEqualTo(old.getId());
    }

    @Test void normalResumeDeleteThenSaveAndSameIdRetryWork() {
        var old = question("old"); store.save(old, scope);
        store.deleteByAddress(address, scope);
        var next = question("next"); store.save(next, scope, Duration.ofMinutes(20));
        store.save(next, scope, Duration.ofMinutes(30));
        assertThat(repository.count()).isEqualTo(1);
        assertThat(store.findByAddress(address, scope).orElseThrow().getId()).isEqualTo(next.getId());
        store.deleteByAddress(address, scope);
        assertThat(repository.count()).isZero();
    }

    private PendingInteraction question(String text) {
        var facts = new Facts(); facts.put("answer", text);
        return new PendingInteraction(address, "conversation", "topic", Question.text(text), facts);
    }
}
