// src/main/java/com/example/examenes/model/PokemonQuizQuestion.java
package com.example.examenes.model;

import java.util.List;

public class PokemonQuizQuestion {
    private String pokemonName;
    private List<String> options;
    private String correctAnswer;
    private String selectedAnswer;
    private String imageUrl; // Nuevo campo para la URL de la imagen

    // Getters y Setters

    public String getPokemonName() {
        return pokemonName;
    }

    public void setPokemonName(String pokemonName) {
        this.pokemonName = pokemonName;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public String getImageUrl() { // Getter para imageUrl
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) { // Setter para imageUrl
        this.imageUrl = imageUrl;
    }
}
