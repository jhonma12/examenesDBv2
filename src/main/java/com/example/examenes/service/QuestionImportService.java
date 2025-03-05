// src/main/java/com/example/examenes/service/QuestionImportService.java
package com.example.examenes.service;

import com.example.examenes.model.Option;
import com.example.examenes.model.Question;
import com.example.examenes.model.QuestionType;
import com.example.examenes.repository.QuestionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class QuestionImportService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private Validator validator;

    /**
     * Importa preguntas desde un InputStream y devuelve una lista de errores encontrados durante la importación.
     *
     * @param inputStream El InputStream que contiene las preguntas a importar.
     * @return Lista de mensajes de error. Vacía si la importación fue exitosa.
     */
    public List<String> importQuestions(InputStream inputStream) {
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            Question currentQuestion = null;
            List<Option> currentOptions = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    // Fin de la pregunta, guardar si es válida
                    if (currentQuestion != null) {
                        // Validar que haya al menos dos opciones
                        if (currentOptions.size() < 2) {
                            errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - Debe tener al menos dos opciones.");
                        } else {
                            currentQuestion.setOptions(currentOptions);
                            Set<ConstraintViolation<Question>> violations = validator.validate(currentQuestion);
                            if (violations.isEmpty()) {
                                questionRepository.save(currentQuestion);
                            } else {
                                for (ConstraintViolation<Question> violation : violations) {
                                    errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - " + violation.getMessage());
                                }
                            }
                        }
                    }
                    // Reset para la siguiente pregunta
                    currentQuestion = null;
                    currentOptions = new ArrayList<>();
                    continue;
                }

                if (line.startsWith("Q:")) {
                    if (currentQuestion != null) {
                        // Guardar la pregunta anterior si no terminó con una línea en blanco
                        currentQuestion.setOptions(currentOptions);
                        Set<ConstraintViolation<Question>> violations = validator.validate(currentQuestion);
                        if (violations.isEmpty()) {
                            questionRepository.save(currentQuestion);
                        } else {
                            for (ConstraintViolation<Question> violation : violations) {
                                errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - " + violation.getMessage());
                            }
                        }
                        // Reset para la nueva pregunta
                        currentOptions = new ArrayList<>();
                    }
                    currentQuestion = new Question();
                    currentQuestion.setText(line.substring(2).trim());
                } else if (line.startsWith("T:")) {
                    if (currentQuestion != null) {
                        String typeStr = line.substring(2).trim().toUpperCase();
                        // Mapeo de cadenas legibles a enum
                        QuestionType type = mapTypeStringToEnum(typeStr);
                        if (type != null) {
                            currentQuestion.setType(type);
                        } else {
                            errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - Tipo de pregunta inválido: " + typeStr);
                        }
                    } else {
                        errors.add("Línea de tipo encontrada sin una pregunta previa: " + line);
                    }
                } else if (line.startsWith("category:")) { // Asegurarse de que coincide con 'category:'
                    if (currentQuestion != null) {
                        String category = line.substring(9).trim();
                        if (!category.isEmpty()) {
                            currentQuestion.setCategory(category);
                        } else {
                            errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - Categoría no puede estar vacía.");
                        }
                    } else {
                        errors.add("Línea de categoría encontrada sin una pregunta previa: " + line);
                    }
                } else if (line.startsWith("A:")) {
                    if (currentQuestion != null) {
                        Option option = new Option();
                        option.setText(line.substring(2).trim());
                        option.setQuestion(currentQuestion);
                        option.setIsCorrect(false); // Por defecto, falso
                        currentOptions.add(option);
                    } else {
                        errors.add("Línea de opción encontrada sin una pregunta previa: " + line);
                    }
                } else if (line.startsWith("C:")) {
                    if (currentQuestion != null) {
                        String indicesStr = line.substring(2).trim();
                        String[] indices = indicesStr.split(",");
                        for (String indexStr : indices) {
                            try {
                                int index = Integer.parseInt(indexStr.trim()) - 1;
                                if (index >= 0 && index < currentOptions.size()) {
                                    currentOptions.get(index).setIsCorrect(true);
                                } else {
                                    errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - Índice de opción correcto inválido: " + indexStr);
                                }
                            } catch (NumberFormatException e) {
                                errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - Índice de opción correcto no es un número: " + indexStr);
                            }
                        }
                    } else {
                        errors.add("Línea de opciones correctas encontrada sin una pregunta previa: " + line);
                    }
                } else {
                    errors.add("Línea desconocida o malformada: " + line);
                }
            }

            // Guardar la última pregunta si el archivo no termina con una línea en blanco
            if (currentQuestion != null) {
                // Validar que haya al menos dos opciones
                if (currentOptions.size() < 2) {
                    errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - Debe tener al menos dos opciones.");
                } else {
                    currentQuestion.setOptions(currentOptions);
                    Set<ConstraintViolation<Question>> violations = validator.validate(currentQuestion);
                    if (violations.isEmpty()) {
                        questionRepository.save(currentQuestion);
                    } else {
                        for (ConstraintViolation<Question> violation : violations) {
                            errors.add("Pregunta: \"" + currentQuestion.getText() + "\" - " + violation.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            errors.add("Error al leer el archivo: " + e.getMessage());
        }

        return errors;
    }

    /**
     * Mapea una cadena de texto al enum QuestionType.
     *
     * @param typeStr Cadena que representa el tipo de pregunta.
     * @return Correspondiente QuestionType o null si no coincide.
     */
    private QuestionType mapTypeStringToEnum(String typeStr) {
        switch (typeStr) {
            case "TRUE_FALSE":
            case "VERDADERO/FALSO":
                return QuestionType.TRUE_FALSE;
            case "SINGLE_CHOICE":
            case "SELECCIÓN ÚNICA":
                return QuestionType.SINGLE_CHOICE;
            case "MULTIPLE_CHOICE":
            case "SELECCIÓN MÚLTIPLE":
                return QuestionType.MULTIPLE_CHOICE;
            default:
                return null;
        }
    }
}
