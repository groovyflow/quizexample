package org.tassoni.quizexample.service;

import java.util.List;

import org.tassoni.quizexample.model.Answer;
import org.tassoni.quizexample.model.BaseEntity;
import org.tassoni.quizexample.model.Question;
import org.tassoni.quizexample.model.QuizContent;
import org.tassoni.quizexample.model.User;

public interface QuizService {
	<T extends BaseEntity> T stubReferenceForId(Class<T> clazz, Long id);
	QuizContent findQuizContentByChoiceId(Long id) ;
	QuizContent saveQuizContent(QuizContent quizContent);
	User findUserByUsernameAndPassword(String username, String password);
	<T extends BaseEntity> List<T> findAll(Class<T> clazz);
	<T extends BaseEntity> T findOne(Class<T> clazz, Long id);
	Answer saveAnswer(Answer answer) ;
	Answer saveAnswer(User user, Long questionId, Long choiceId);
	Question findNextQuestion(User user);
}
