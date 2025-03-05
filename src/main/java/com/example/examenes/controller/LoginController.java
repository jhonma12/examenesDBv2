package com.example.examenes.controller;

import com.example.examenes.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.example.examenes.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    // Página de login
    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("usuario") != null) {
            return "redirect:/home"; // Si ya está autenticado, redirigir al home
        }
        return "login"; // Mostrar el login si no está autenticado
    }

    @PostMapping("/loginUsuario")
    public String loginUsuario(@RequestParam String nombre,
                               @RequestParam String password,
                               Model model, HttpSession session, HttpServletRequest request) {

        // Usar UsuarioService para autenticar al usuario
        Optional<Usuario> usuarioOpt = usuarioService.autenticarUsuario(nombre, password);
        if (!usuarioOpt.isPresent()) {
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            return "login";  // Retorna la vista de login con error
        }

        // Si las credenciales son correctas, guardar la sesión
        Usuario usuario = usuarioOpt.get();
        session.setAttribute("usuario", usuario);  // Establecer la sesión aquí

        // Verificación detallada de la sesión
        System.out.println("Usuario autenticado: " + usuario.getNombre());  // Depuración
        System.out.println("ID de sesión: " + request.getSession().getId());  // Depuración

        return "redirect:/home"; // Redirigir a /home después de un login exitoso
    }

    @GetMapping("/register")
    public String register() {
        return "register";  // Vista para registro
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@RequestParam String nombre,
            @RequestParam String password,
            Model model) {
        // Usar UsuarioService para registrar al nuevo usuario
        usuarioService.registrarUsuario(nombre, password);
        return "redirect:/login";  // Redirigir al login después de registrar
    }
}
