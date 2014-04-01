package org.tassoni.quizexample.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tassoni.quizexample.model.*;
import org.tassoni.quizexample.repository.BasicRepository;
import org.tassoni.quizexample.repository.UserRepository;

//TODO  Aren't some of these transactions too fine grained, leaving a lot of transaction overhead for every little
//thing we want  from the database?
@Service
public class QuizServiceImpl implements QuizService{
	
	public static final Long INITIAL_QUESTION = 1l;
	
	private UserRepository userRepository;
	private BasicRepository basicRepository;
	
	@Autowired
	public QuizServiceImpl(BasicRepository basicRepository, UserRepository userRepository) {
		this.basicRepository = basicRepository;
		this.userRepository = userRepository;
	}
	
	
	@Transactional(readOnly = true)
	public QuizContent findQuizContentByChoiceId(Long id) {
		return basicRepository.findQuizContentByChoiceId(id);
	}
	
	
	@Transactional(readOnly = true)
	public User findUserByUsernameAndPassword(String username, String password) {
		return userRepository.findByUsernameAndPassword(username, password);
	}
	
	
	/**
	 * Note: Helplful if you have an Id and need to pass in a domain object, as below.
	 * .setChoice(
	 *					quizService.stubReferenceForId(Choice.class, choiceId))
	 * Will not open its own transaction, so no preformance hit.
	 */
	public <T extends BaseEntity> T stubReferenceForId(Class<T> clazz, Long id) {
		return basicRepository.stubReferenceForId(clazz, id);
	}
	
	@Transactional(readOnly = true)
	public <T extends BaseEntity> T findOne(Class<T> clazz, Long id) {
		return basicRepository.findById(clazz, id);
	}
	
	@Transactional(readOnly = true)
	public <T extends BaseEntity> List<T> findAll(Class<T> clazz) {
		return basicRepository.findAll(clazz);
	}
	
	@Transactional
	public Answer saveAnswer(Answer answer) {
		return basicRepository.save(answer);
	}
	

	@Transactional(readOnly = true) 
	public Question findNextQuestion(User user) {
		Answer latestAnswer = basicRepository.latestAnswerForUser(user);
		if(latestAnswer == null)
			return basicRepository.findQuestionAndChoices(INITIAL_QUESTION);
		else 
			return basicRepository.findNextQuestionAndChoices(latestAnswer);
		
	}
}
