package com.atchurey.tools.talkativebot.core.conversation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Facts implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, Object> values = new LinkedHashMap<>();

	public Facts() {
	}

	@JsonCreator
	public Facts(Map<String, Object> values) {
		if (values != null) {
			this.values.putAll(values);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) values.get(key);
	}

	public <T> void put(String key, T value) {
		Objects.requireNonNull(key, "key must not be null");

		if (value == null) {
			values.remove(key);
		} else {
			values.put(key, value);
		}
	}

	public void add(String key, Object value) {
		put(key, value);
	}

	public void remove(String key) {
		values.remove(key);
	}

	public boolean contains(String key) {
		return values.containsKey(key);
	}

	public void clear() {
		values.clear();
	}

	public boolean isEmpty() {
		return values.isEmpty();
	}

	public int size() {
		return values.size();
	}

	public void forEach(BiConsumer<String, Object> consumer) {
		values.forEach(consumer);
	}

	@JsonValue
	public Map<String, Object> asMap() {
		return Collections.unmodifiableMap(values);
	}

	@Override
	public String toString() {
		return values.toString();
	}
}