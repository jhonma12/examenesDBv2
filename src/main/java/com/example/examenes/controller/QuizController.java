// src/main/java/com/example/examenes/controller/QuizController.java
package com.example.examenes.controller;

import com.example.examenes.model.Option;
import com.example.examenes.model.Question;
import com.example.examenes.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class QuizController {

    @Autowired
    private QuestionService questionService;

    // Muestra una pregunta aleatoria
    @GetMapping("/quiz")
    public String showQuiz(Model model, @SessionAttribute(value = "answeredQuestions", required = false) Set<Long> answeredQuestions) {
        List<Question> availableQuestions = questionService.getAvailableQuestions(answeredQuestions);
        if (availableQuestions.isEmpty()) {
            model.addAttribute("message", "No hay más preguntas disponibles.");
            return "quiz_result";
        }

        // Seleccionar una pregunta aleatoria
        Random rand = new Random();
        Question randomQuestion = availableQuestions.get(rand.nextInt(availableQuestions.size()));
        model.addAttribute("question", randomQuestion);
        model.addAttribute("questionType", randomQuestion.getType());

        return "quiz";
    }

    // Procesa la respuesta del usuario
    @PostMapping("/quiz")
    public String processQuiz(@RequestParam("questionId") Long questionId,
                              @RequestParam(value = "optionIds", required = false) List<Long> optionIds,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              @SessionAttribute(value = "answeredQuestions", required = false) Set<Long> answeredQuestions) {

        Optional<Question> optionalQuestion = questionService.getQuestionById(questionId);
        if (!optionalQuestion.isPresent()) {
            model.addAttribute("message", "Pregunta no encontrada.");
            return "quiz_result";
        }

        Question question = optionalQuestion.get();

        // Obtener las opciones correctas
        List<Option> correctOptions = question.getOptions().stream()
                .filter(Option::getIsCorrect)
                .toList();

        // Obtener las opciones seleccionadas por el usuario
        List<Option> selectedOptions = new ArrayList<>();
        if (optionIds != null) {
            selectedOptions = question.getOptions().stream()
                    .filter(option -> optionIds.contains(option.getId()))
                    .toList();
        }

        // Verificar si la respuesta es correcta
        boolean isCorrect;
        if (question.getType() == com.example.examenes.model.QuestionType.TRUE_FALSE ||
                question.getType() == com.example.examenes.model.QuestionType.SINGLE_CHOICE) {
            isCorrect = selectedOptions.size() == 1 &&
                        correctOptions.get(0).getId().equals(selectedOptions.get(0).getId());
        } else { // MULTIPLE_CHOICE
            Set<Long> correctOptionIds = correctOptions.stream()
                    .map(Option::getId)
                    .collect(Collectors.toSet());
            Set<Long> selectedOptionIds = selectedOptions.stream()
                    .map(Option::getId)
                    .collect(Collectors.toSet());
            isCorrect = correctOptionIds.equals(selectedOptionIds);
        }

        // Actualizar las preguntas respondidas en la sesión
        if (answeredQuestions == null) {
            answeredQuestions = new HashSet<>();
        }
        answeredQuestions.add(questionId);
        redirectAttributes.addFlashAttribute("answeredQuestions", answeredQuestions);

        // Preparar los datos para la vista de resultado
        model.addAttribute("question", question);
        model.addAttribute("selectedOptions", selectedOptions);
        model.addAttribute("isCorrect", isCorrect);
        model.addAttribute("correctOptions", correctOptions);

        return "quiz_result";
    }
}
