package com.example.examenes.service;

import com.example.examenes.model.Usuario;
import com.example.examenes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Registrar un nuevo usuario
    public void registrarUsuario(String nombre, String password) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setPassword(passwordEncoder.encode(password));  // Encriptar la contraseña antes de guardar
        usuarioRepository.save(usuario);
    }

    // Validar credenciales de login
    public Optional<Usuario> autenticarUsuario(String nombre, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombre(nombre);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                return Optional.of(usuario);  // Devuelve el usuario si las credenciales son correctas
            }
        }
        return Optional.empty();  // Devuelve vacío si las credenciales no son correctas
    }
}
