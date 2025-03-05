// src/main/java/com/example/examenes/service/PokemonService.java
package com.example.examenes.service;

import com.example.examenes.model.PokemonQuizQuestion;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PokemonService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2";

    private final Random random = new Random();

    public PokemonQuizQuestion generateQuizQuestion() {
        while (true) {
            // Obtener un Pokémon aleatorio
            String randomPokemon = getRandomPokemonName();
            String evolutionChainUrl = getEvolutionChainUrl(randomPokemon);
            List<String> evolutions = getEvolutionsFromChain(evolutionChainUrl);

            // Verificar si el Pokémon tiene al menos una evolución posterior
            if (evolutions.size() < 2) {
                continue; // Seleccionar otro Pokémon
            }

            // Encontrar la posición del Pokémon en la cadena de evoluciones
            int index = evolutions.indexOf(randomPokemon.toLowerCase());
            if (index == -1 || index >= evolutions.size() - 1) {
                continue; // El Pokémon está en la última evolución o no se encontró, seleccionar otro
            }

            // Obtener la evolución correcta
            String correctEvolution = evolutions.get(index + 1);

            Set<String> options = new HashSet<>();
            options.add(capitalize(correctEvolution));

            // Añadir opciones incorrectas
            while (options.size() < 4) {
                String option = getRandomPokemonName();
                String capitalizedOption = capitalize(option);
                if (!options.contains(capitalizedOption) && !evolutions.contains(option)) {
                    options.add(capitalizedOption);
                }
            }

            // Convertir el conjunto a una lista y mezclar
            List<String> shuffledOptions = new ArrayList<>(options);
            Collections.shuffle(shuffledOptions);

            // Obtener la imagen del Pokémon base
            String imageUrl = getPokemonImageUrl(randomPokemon);

            // Crear la pregunta
            PokemonQuizQuestion question = new PokemonQuizQuestion();
            question.setPokemonName(capitalize(randomPokemon));
            question.setOptions(shuffledOptions);
            question.setCorrectAnswer(capitalize(correctEvolution));
            question.setImageUrl(imageUrl); // Asignar la URL de la imagen

            return question;
        }
    }

    public boolean checkAnswer(String selectedAnswer, String correctAnswer) {
        if (selectedAnswer == null || correctAnswer == null) {
            return false;
        }
        return selectedAnswer.equalsIgnoreCase(correctAnswer);
    }

    private String getRandomPokemonName() {
        // PokeAPI tiene 1010 Pokémon hasta ahora. Ajusta según sea necesario.
        int id = random.nextInt(1010) + 1;
        String url = POKEAPI_BASE_URL + "/pokemon/" + id;
        try {
            @SuppressWarnings("unchecked")
			Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return (String) response.get("name");
        } catch (Exception e) {
            // En caso de error, retornar un Pokémon específico
            return "pikachu";
        }
    }

    private String getEvolutionChainUrl(String pokemonName) {
        String url = POKEAPI_BASE_URL + "/pokemon-species/" + pokemonName;
        try {
            @SuppressWarnings("unchecked")
			Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            @SuppressWarnings("unchecked")
			Map<String, String> evolutionChain = (Map<String, String>) response.get("evolution_chain");
            return evolutionChain.get("url");
        } catch (Exception e) {
            // En caso de error, retornar null
            return null;
        }
    }

    private List<String> getEvolutionsFromChain(String evolutionChainUrl) {
        if (evolutionChainUrl == null) {
            return Collections.emptyList();
        }

        try {
            @SuppressWarnings("unchecked")
			Map<String, Object> chainResponse = restTemplate.getForObject(evolutionChainUrl, Map.class);
            @SuppressWarnings("unchecked")
			Map<String, Object> chain = (Map<String, Object>) chainResponse.get("chain");
            List<String> evolutions = new ArrayList<>();
            traverseEvolutionChain(chain, evolutions);
            return evolutions;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void traverseEvolutionChain(Map<String, Object> chain, List<String> evolutions) {
        @SuppressWarnings("unchecked")
		String speciesName = ((Map<String, String>) chain.get("species")).get("name");
        evolutions.add(speciesName);
        @SuppressWarnings("unchecked")
		List<Map<String, Object>> evolvesTo = (List<Map<String, Object>>) chain.get("evolves_to");
        if (evolvesTo != null && !evolvesTo.isEmpty()) {
            // Para simplificar, tomamos la primera evolución
            traverseEvolutionChain(evolvesTo.get(0), evolutions);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1);
    }

    private String getPokemonImageUrl(String pokemonName) {
        String url = POKEAPI_BASE_URL + "/pokemon/" + pokemonName;
        try {
            @SuppressWarnings("unchecked")
			Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            @SuppressWarnings("unchecked")
			Map<String, Object> sprites = (Map<String, Object>) response.get("sprites");
            String imageUrl = (String) sprites.get("front_default");
            return imageUrl;
        } catch (Exception e) {
            return null; // O podrías retornar una URL por defecto
        }
    }
}
