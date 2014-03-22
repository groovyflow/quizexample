package org.tassoni.quizexample.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tassoni.quizexample.model.*;
import org.tassoni.quizexample.repository.AnswerRepository;
import org.tassoni.quizexample.repository.BasicRepository;
import org.tassoni.quizexample.repository.QuestionRepository;
import org.tassoni.quizexample.repository.QuizContentRepository;
import org.tassoni.quizexample.repository.UserRepository;

//TODO  Aren't these transactions too fine grained, leaveing a lot of transaction overhead for every little
//thing we want  from the database?
@Service
public class QuizServiceImpl implements QuizService{
	
	public static final Long INITIAL_QUESTION = 1l;
	
	@Autowired
	private QuizContentRepository quizContentRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BasicRepository basicRepository;
	
	@Autowired
	private AnswerRepository answerRepository;
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Transactional(readOnly = true)
	public QuizContent findQuizContentByChoiceId(Long id) {
		return basicRepository.findQuizContentByChoiceId(id);
	}
	
	@Transactional(readOnly = true)
	public QuizContent saveQuizContent(QuizContent quizContent) {
		return quizContentRepository.save(quizContent);
	}
	
	@Transactional(readOnly = true)
	public User findUserByUsernameAndPassword(String username, String password) {
		return userRepository.findByUsernameAndPassword(username, password);
	}
	
	
	//TODO  If this is actually transactional, code that uses it is wasting time.  
	//Instead that code should just pass a bunch of ids to the service, so that we don't have too many
	//transactions
	//TODO Decide if this can be used outside of transaction, and if it can't be, make it private!!
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
		return answerRepository.save(answer);
	}
	
	@Transactional
	public Answer saveAnswer(User user, Long questionId, Long choiceId) {
		System.out.println("Is the database quiet here?");
		Answer answer = new Answer().setChoice(stubReferenceForId(Choice.class, choiceId)).
				setQuestion(stubReferenceForId(Question.class, questionId)).
				setUser(user);
		System.out.println("Well, was it?");
		return answerRepository.save(answer);
	}
	

	@Transactional(readOnly = true) 
	public Question findNextQuestion(User user) {
		Answer latestAnswer = basicRepository.latestAnswerForUser(user);
		if(latestAnswer == null)
			return basicRepository.findQuestionAndChoices(INITIAL_QUESTION);
		else 
			return basicRepository.findNextQuestion(latestAnswer);
		
	}
}
