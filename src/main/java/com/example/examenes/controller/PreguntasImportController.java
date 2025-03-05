// src/main/java/com/example/examenes/controller/ImportController.java
package com.example.examenes.controller;

import com.example.examenes.service.QuestionImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PreguntasImportController {

    @Autowired
    private QuestionImportService questionImportService;

    @GetMapping("/questions/import")
    public String showImportForm() {
        return "import_questions"; // Crea una plantilla Thymeleaf llamada import_questions.html
    }

    @PostMapping("/questions/import")
    public String handleImport(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, selecciona un archivo para importar.");
            return "redirect:/questions/import";
        }

        try {
            List<String> errors = questionImportService.importQuestions(file.getInputStream());
            if (errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("successMessage", "Preguntas importadas exitosamente.");
            } else {
                // Concatenar todos los errores en un solo mensaje
                StringBuilder errorMsg = new StringBuilder();
                for (String error : errors) {
                    errorMsg.append(error).append("\n");
                }
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg.toString());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al importar las preguntas: " + e.getMessage());
        }

        return "redirect:/questions";
    }
}
