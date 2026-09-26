package com.eduassess.repository;

import com.eduassess.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findByTestIdOrderByQuestionOrderAsc(String testId);
    long countByTestId(String testId);
}
