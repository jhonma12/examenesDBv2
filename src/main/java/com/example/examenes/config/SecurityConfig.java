package com.example.examenes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login","/logout", "/register", "/css/**", "/js/**", "/images/**", "/loginUsuario", "/registrar","/home", "/quiz", "/questions/**")
                .permitAll() // Estas rutas no requieren autenticación
//                .requestMatchers("/home", "/quiz", "/questions/**")
//                .authenticated() // Solo los usuarios autenticados pueden acceder a estas rutas
            )
            .formLogin(login -> login
                .loginPage("/login") // Página personalizada de login
                .defaultSuccessUrl("/home", true) // Asegura la redirección a /home
                .permitAll()
            )
            .logout(logout -> logout
            		.logoutSuccessUrl("/login")
            		.permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Deshabilitar CSRF si es necesario
            

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Usar BCrypt para encriptar las contraseñas
    }
}
