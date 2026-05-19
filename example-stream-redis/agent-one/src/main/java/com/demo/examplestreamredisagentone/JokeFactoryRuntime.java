package com.demo.examplestreamredisagentone;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class JokeFactoryRuntime {

	private final String[] jokeTypes = {"general", "knock-knock", "programming",  "dad"};
	private final Map<Integer, Joke> jokes = new HashMap<>();

	private final WebClient webClient;

	public JokeFactoryRuntime(WebClient webClient) {
		this.webClient = webClient;
	}

	public Joke generateJoke(String type) {

		String matchedType = Arrays.stream(jokeTypes)
				.filter(a -> a.equalsIgnoreCase(type))
				.findFirst().orElse("general");

		List<Joke> jokes =  webClient.get()
				.uri("/jokes/" + matchedType + "/random")
				.retrieve()
				.bodyToMono(
						new ParameterizedTypeReference<List<Joke>>() {}
				)
				.block();

		Joke joke = jokes.get(0);
		this.jokes.put(joke.id, joke);
		return joke;
	}

	public Joke getJoke(int id) throws IllegalArgumentException {
		return Optional.ofNullable(jokes.get(id))
				.orElseThrow(() ->
						new IllegalArgumentException(
								"Joke not found: " + id
						));
	}


	@Getter
	@Setter
	public static class Joke {
		private int id;
		private String type;
		private String setup;
		private String punchline;
	}
}