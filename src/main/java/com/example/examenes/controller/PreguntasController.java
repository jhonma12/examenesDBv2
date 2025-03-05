package com.example.examenes.controller;

import com.example.examenes.model.Option;
import com.example.examenes.model.Question;
import com.example.examenes.model.QuestionType;
import com.example.examenes.model.Usuario;
import com.example.examenes.service.QuestionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/questions")
public class PreguntasController {

	private static final Logger logger = LoggerFactory.getLogger(PreguntasController.class);

	@Autowired
	private QuestionService questionService;

	@Autowired
	private ObjectMapper objectMapper;

	private static final int PAGE_SIZE = 4; // Número de preguntas por página

	// Listar todas las preguntas con paginación y filtros
	@GetMapping
	public String listQuestions(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "type", required = false) String type, HttpSession session) {
		if (!isAdmin(session)) {
			return "redirect:/home"; // Redirigir a home si no es admin
		}

		if (page < 1)
			page = 1;

		Object usuario = session.getAttribute("usuario");
		if (usuario != null) {
			model.addAttribute("usuario", usuario);
		}

		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		Page<Question> questionPage;

		if (category != null && !category.isEmpty() && type != null && !type.isEmpty()) {
			try {
				QuestionType questionType = QuestionType.valueOf(type.toUpperCase());
				questionPage = questionService.getQuestionsByCategoryAndType(category, questionType, pageable);
			} catch (IllegalArgumentException e) {
				logger.error("Tipo de pregunta inválido: {}", type);
				questionPage = questionService.getQuestions(pageable);
				category = null;
				type = null;
			}
		} else if (category != null && !category.isEmpty()) {
			questionPage = questionService.getQuestionsByCategory(category, pageable);
		} else if (type != null && !type.isEmpty()) {
			try {
				QuestionType questionType = QuestionType.valueOf(type.toUpperCase());
				questionPage = questionService.getQuestionsByType(questionType, pageable);
			} catch (IllegalArgumentException e) {
				logger.error("Tipo de pregunta inválido: {}", type);
				questionPage = questionService.getQuestions(pageable);
				type = null;
			}
		} else {
			questionPage = questionService.getQuestions(pageable);
		}

		model.addAttribute("questionPage", questionPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", questionPage.getTotalPages());
		model.addAttribute("currentCategory", category);
		model.addAttribute("currentType", type);

		// Agregar lista de categorías y tipos para los filtros
		model.addAttribute("categories", questionService.getAllCategories());
		model.addAttribute("questionTypes", QuestionType.values());

		return "questions"; // questions.html
	}

	// Mostrar el formulario para crear una nueva pregunta
	@GetMapping("/new")
	public String showCreateForm(Model model, HttpSession session) {
		if (!isAdmin(session)) {
			return "redirect:/home"; // Redirigir a home si no es admin
		}

		Question question = new Question();
		// Añadir una opción vacía para empezar
		Option option = new Option();
		option.setQuestion(question);
		List<Option> options = new ArrayList<>();
		options.add(option);
		question.setOptions(options);
		model.addAttribute("question", question);
		model.addAttribute("questionTypes", QuestionType.values());
		return "create_question"; // create_question.html
	}

	// Procesar la creación de una nueva pregunta
	@PostMapping("/save")
	public String saveQuestion(@Valid @ModelAttribute("question") Question question, BindingResult bindingResult,
			Model model, RedirectAttributes redirectAttributes, HttpSession session) {
		if (!isAdmin(session)) {
			return "redirect:/home"; // Redirigir a home si no es admin
		}

		logger.info("Procesando la creación de una nueva pregunta: {}", question.getText());

		// Validar que haya al menos dos opciones
		if (question.getOptions() == null || question.getOptions().size() < 2) {
			bindingResult.rejectValue("options", "error.question", "La pregunta debe tener al menos dos opciones.");
			logger.warn("La pregunta '{}' tiene menos de dos opciones.", question.getText());
		}

		// Validar que al menos una opción sea correcta
		boolean hasCorrectOption = question.getOptions().stream()
				.anyMatch(option -> Boolean.TRUE.equals(option.getIsCorrect()));
		if (!hasCorrectOption) {
			bindingResult.rejectValue("options", "error.question", "Debe haber al menos una opción correcta.");
			logger.warn("La pregunta '{}' no tiene ninguna opción correcta.", question.getText());
		}

		// Validar que para SINGLE_CHOICE y TRUE_FALSE solo una opción sea correcta
		if (question.getType() == QuestionType.SINGLE_CHOICE || question.getType() == QuestionType.TRUE_FALSE) {
			long correctCount = question.getOptions().stream()
					.filter(option -> Boolean.TRUE.equals(option.getIsCorrect())).count();
			if (correctCount != 1) {
				bindingResult.rejectValue("options", "error.question",
						"Debe haber exactamente una opción correcta para este tipo de pregunta.");
				logger.warn("La pregunta '{}' tiene {} opciones correctas, se requiere exactamente una.",
						question.getText(), correctCount);
			}
		}

		if (bindingResult.hasErrors()) {
			logger.error("Errores de validación en la creación de la pregunta '{}'.", question.getText());
			model.addAttribute("questionTypes", QuestionType.values());
			model.addAttribute("categories", questionService.getAllCategories());
			return "create_question";
		}

		// Asociar cada opción con la pregunta y asegurar que isCorrect no sea nulo
		for (Option option : question.getOptions()) {
			option.setQuestion(question);
			if (option.getIsCorrect() == null) {
				option.setIsCorrect(false);
			}
			logger.debug("Opción: '{}', isCorrect: {}", option.getText(), option.getIsCorrect());
		}

		questionService.saveQuestion(question);
		logger.info("Pregunta '{}' creada exitosamente.", question.getText());
		redirectAttributes.addFlashAttribute("successMessage", "Pregunta creada exitosamente.");
		return "redirect:/questions";
	}

	// Mostrar el formulario para editar una pregunta existente
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {
		if (!isAdmin(session)) {
			return "redirect:/home"; // Redirigir a home si no es admin
		}

		Optional<Question> optionalQuestion = questionService.getQuestionById(id);
		if (optionalQuestion.isPresent()) {
			Question question = optionalQuestion.get();

			// Asegurar que la lista de opciones no sea nula
			if (question.getOptions() == null) {
				question.setOptions(new ArrayList<>());
			}

			// Si no hay opciones, añadir una opción vacía
			if (question.getOptions().isEmpty()) {
				Option option = new Option();
				option.setQuestion(question);
				question.getOptions().add(option);
			}

			model.addAttribute("question", question);
			model.addAttribute("questionTypes", QuestionType.values());
			model.addAttribute("categories", questionService.getAllCategories());

			try {
				String optionsJson = objectMapper.writeValueAsString(question.getOptions());
				model.addAttribute("optionsJson", optionsJson);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				model.addAttribute("optionsJson", "[]");
			}

			return "edit_question"; // edit_question.html
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Pregunta no encontrada.");
			return "redirect:/questions";
		}
	}
	@PostMapping("/update/{id}")
	public String updateQuestion(@PathVariable("id") Long id,
	                             @Valid @ModelAttribute("question") Question question,
	                             BindingResult bindingResult,
	                             Model model,
	                             RedirectAttributes redirectAttributes,
	                             HttpSession session) {
	    // Verificar si el usuario no es admin y redirigir a home si es el caso
	    if (!isAdmin(session)) {
	        return "redirect:/home";  // Redirigir a home si no es admin
	    }

	    logger.info("Procesando la actualización de la pregunta ID '{}'.", id);

	    // Validar que haya al menos dos opciones
	    if (question.getOptions() == null || question.getOptions().size() < 2) {
	        bindingResult.rejectValue("options", "error.question", "La pregunta debe tener al menos dos opciones.");
	        logger.warn("La pregunta ID '{}' tiene menos de dos opciones.", id);
	    }

	    // Validar que al menos una opción sea correcta
	    boolean hasCorrectOption = question.getOptions().stream()
	            .anyMatch(option -> Boolean.TRUE.equals(option.getIsCorrect()));
	    if (!hasCorrectOption) {
	        bindingResult.rejectValue("options", "error.question", "Debe haber al menos una opción correcta.");
	        logger.warn("La pregunta ID '{}' no tiene ninguna opción correcta.", id);
	    }

	    // Validar que para SINGLE_CHOICE y TRUE_FALSE solo una opción sea correcta
	    if (question.getType() == QuestionType.SINGLE_CHOICE || question.getType() == QuestionType.TRUE_FALSE) {
	        long correctCount = question.getOptions().stream()
	                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
	                .count();
	        if (correctCount != 1) {
	            bindingResult.rejectValue("options", "error.question", "Debe haber exactamente una opción correcta para este tipo de pregunta.");
	            logger.warn("La pregunta ID '{}' tiene {} opciones correctas, se requiere exactamente una.", id, correctCount);
	        }
	    }

	    if (bindingResult.hasErrors()) {
	        logger.error("Errores de validación en la actualización de la pregunta ID '{}'.", id);
	        model.addAttribute("questionTypes", QuestionType.values());
	        model.addAttribute("categories", questionService.getAllCategories());
	        return "edit_question";
	    }

	    // Asociar cada opción con la pregunta y asegurar que isCorrect no sea nulo
	    for (Option option : question.getOptions()) {
	        option.setQuestion(question);
	        if (option.getIsCorrect() == null) {
	            option.setIsCorrect(false);
	        }
	        logger.debug("Opción: '{}', isCorrect: {}", option.getText(), option.getIsCorrect());
	    }

	    question.setId(id); // Asegurar que el ID es correcto
	    questionService.saveQuestion(question);
	    logger.info("Pregunta ID '{}' actualizada exitosamente.", id);
	    redirectAttributes.addFlashAttribute("successMessage", "Pregunta actualizada exitosamente.");
	    return "redirect:/questions";
	}
	

	// Eliminar una pregunta
	@GetMapping("/delete/{id}")
	public String deleteQuestion(@PathVariable("id") Long id, RedirectAttributes redirectAttributes,
			HttpSession session) {
		// No redirigir a home si el usuario es admin
		if (isAdmin(session)) {
			Optional<Question> optionalQuestion = questionService.getQuestionById(id);
			if (optionalQuestion.isPresent()) {
				questionService.deleteQuestion(id);
				redirectAttributes.addFlashAttribute("successMessage", "Pregunta eliminada exitosamente.");
				logger.info("Pregunta ID '{}' eliminada exitosamente.", id);
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Pregunta no encontrada.");
				logger.error("No se encontró la pregunta con ID '{}'.", id);
			}
			return "redirect:/questions";
		} else {
			return "redirect:/home"; // Redirigir a home si no es admin
		}
	}

	private boolean isAdmin(HttpSession session) {
		Object usuario = session.getAttribute("usuario");
		return "admin".equals(((Usuario) usuario).getNombre());
	}
}
