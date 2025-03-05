// src/main/java/com/example/examenes/service/QuestionService.java
package com.example.examenes.service;

import com.example.examenes.model.Question;
import com.example.examenes.model.QuestionType;
import com.example.examenes.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    // Obtener todas las preguntas
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    // Obtener preguntas disponibles (no respondidas)
    public List<Question> getAvailableQuestions(Set<Long> answeredQuestions) {
        if (answeredQuestions == null || answeredQuestions.isEmpty()) {
            return getAllQuestions();
        }
        return questionRepository.findAll().stream()
                .filter(question -> !answeredQuestions.contains(question.getId()))
                .collect(Collectors.toList());
    }

    // Obtener una pregunta por su ID con opciones
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findByIdWithOptions(id);
    }

    // Guardar una pregunta (crear o actualizar)
    public void saveQuestion(Question question) {
        questionRepository.save(question);
    }

    // Eliminar una pregunta por su ID
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    // Métodos para paginación y filtros

    public Page<Question> getQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    public Page<Question> getQuestionsByCategory(String category, Pageable pageable) {
        return questionRepository.findByCategory(category, pageable);
    }

    public Page<Question> getQuestionsByType(QuestionType type, Pageable pageable) {
        return questionRepository.findByType(type, pageable);
    }

    public Page<Question> getQuestionsByCategoryAndType(String category, QuestionType type, Pageable pageable) {
        return questionRepository.findByCategoryAndType(category, type, pageable);
    }

    // Obtener todas las categorías disponibles
    public List<String> getAllCategories() {
        return questionRepository.findAll().stream()
                .map(Question::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }
}
