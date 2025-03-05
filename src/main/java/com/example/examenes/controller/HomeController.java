package com.example.examenes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Mostrar la página de inicio
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        // Verificar si el usuario está en la sesión
        Object usuario = session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";  // Redirigir al login si no hay usuario en la sesión
        }

        // Agregar el usuario al modelo para que Thymleaf lo use
        model.addAttribute("usuario", usuario);
        return "home";  // Devolver la vista home
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // Invalidar la sesión
        return "redirect:/login";  // Redirigir al login
    }
}
