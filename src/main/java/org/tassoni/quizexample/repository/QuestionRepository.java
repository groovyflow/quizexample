package org.tassoni.quizexample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tassoni.quizexample.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

	//Using Spring Data JPA  Fancy, huh?  
    @Query("select question2 from  Question question2 inner join fetch question2.choices,  NextQuestion nextQuestion, Question question where"
    		+ " question.id = :id and nextQuestion.question = question and"
			+ " nextQuestion.nextQuestion = question2 ")
	public Question findNextQuestion(@Param("id")  Long questionId);
}
