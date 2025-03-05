// src/main/java/com/example/examenes/repository/OptionRepository.java
package com.example.examenes.repository;

import com.example.examenes.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    // Métodos de consulta personalizados pueden añadirse aquí si es necesario
}
