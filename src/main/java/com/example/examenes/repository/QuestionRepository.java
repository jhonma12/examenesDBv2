// src/main/java/com/example/examenes/repository/QuestionRepository.java
package com.example.examenes.repository;

import com.example.examenes.model.Question;
import com.example.examenes.model.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :id")
    Optional<Question> findByIdWithOptions(@Param("id") Long id);

    Page<Question> findByCategory(String category, Pageable pageable);

    Page<Question> findByType(QuestionType type, Pageable pageable);

    Page<Question> findByCategoryAndType(String category, QuestionType type, Pageable pageable);
}
