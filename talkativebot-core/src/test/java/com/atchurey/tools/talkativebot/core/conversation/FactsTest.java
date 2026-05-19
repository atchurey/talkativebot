package com.atchurey.tools.talkativebot.core.conversation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FactsTest {

    private Facts facts;

    @BeforeEach
    void setUp() {
        facts = new Facts();
    }

    @Test
    void shouldPutAndGetValues() {
        facts.put("key1", "value1");
        facts.put("key2", 123);

        assertThat(facts.<String>get("key1")).isEqualTo("value1");
        assertThat(facts.<Integer>get("key2")).isEqualTo(123);
    }

    @Test
    void shouldRemoveValueWhenPuttingNull() {
        facts.put("key", "value");
        assertThat(facts.contains("key")).isTrue();

        facts.put("key", null);
        assertThat(facts.contains("key")).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenPuttingNullKey() {
        assertThatThrownBy(() -> facts.put(null, "value"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("key must not be null");
    }

    @Test
    void shouldAddValues() {
        facts.add("key", "value");
        assertThat(facts.<String>get("key")).isEqualTo("value");
    }

    @Test
    void shouldRemoveValues() {
        facts.put("key", "value");
        facts.remove("key");
        assertThat(facts.contains("key")).isFalse();
    }

    @Test
    void shouldCheckIfContains() {
        assertThat(facts.contains("key")).isFalse();
        facts.put("key", "value");
        assertThat(facts.contains("key")).isTrue();
    }

    @Test
    void shouldClearValues() {
        facts.put("key1", "value1");
        facts.put("key2", "value2");
        assertThat(facts.size()).isEqualTo(2);

        facts.clear();
        assertThat(facts.isEmpty()).isTrue();
        assertThat(facts.size()).isEqualTo(0);
    }

    @Test
    void shouldReportSizeAndEmpty() {
        assertThat(facts.isEmpty()).isTrue();
        assertThat(facts.size()).isEqualTo(0);

        facts.put("key", "value");
        assertThat(facts.isEmpty()).isFalse();
        assertThat(facts.size()).isEqualTo(1);
    }

    @Test
    void shouldIterateOverValues() {
        facts.put("key1", "value1");
        facts.put("key2", "value2");

        AtomicInteger count = new AtomicInteger();
        facts.forEach((k, v) -> count.incrementAndGet());

        assertThat(count.get()).isEqualTo(2);
    }

    @Test
    void shouldReturnAsMap() {
        facts.put("key", "value");
        Map<String, Object> map = facts.asMap();

        assertThat(map).containsEntry("key", "value");
        assertThatThrownBy(() -> map.put("new", "val"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldInitializeWithMap() {
        Map<String, Object> initialValues = new HashMap<>();
        initialValues.put("key1", "value1");
        initialValues.put("key2", 2);

        Facts factsWithValues = new Facts(initialValues);
        assertThat(factsWithValues.size()).isEqualTo(2);
        assertThat(factsWithValues.<String>get("key1")).isEqualTo("value1");
        assertThat(factsWithValues.<Integer>get("key2")).isEqualTo(2);
    }

    @Test
    void shouldInitializeWithNullMap() {
        Facts factsWithNull = new Facts(null);
        assertThat(factsWithNull.isEmpty()).isTrue();
    }

    @Test
    void shouldHaveToString() {
        facts.put("key", "value");
        assertThat(facts.toString()).contains("key=value");
    }
}
