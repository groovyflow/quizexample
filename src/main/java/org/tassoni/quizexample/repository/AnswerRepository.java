package org.tassoni.quizexample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tassoni.quizexample.model.Answer;
import org.tassoni.quizexample.model.User;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
	

}
