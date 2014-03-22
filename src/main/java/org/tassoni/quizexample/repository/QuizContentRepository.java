package org.tassoni.quizexample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tassoni.quizexample.model.QuizContent;

public interface QuizContentRepository extends JpaRepository<QuizContent, Long> {

}
