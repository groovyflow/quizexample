package org.tassoni.quizexample.repository;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tassoni.quizexample.controller.HomeController;
import org.tassoni.quizexample.model.Answer;
import org.tassoni.quizexample.model.BaseEntity;
import org.tassoni.quizexample.model.Choice;
import org.tassoni.quizexample.model.Question;
import org.tassoni.quizexample.model.QuizContent;
import org.tassoni.quizexample.model.User;

//Chuck!! IMPORTANT!! We could use this instead of the silly classes that extend JpaRepository<Entity, PrimaryKey>.
//Those classes would make sense if you really could concentrate on one entity at a time, but databases
//are made up of relationships.  

//By the way, those classes that extends JpaRepository are picked up by a spring-data jpa scan only because they extend that 
//interface. And our BasicRepository would not get picked up by that scan, even though we mark ourselves as a repository,
//because we don't extend JpaRepository.  So our exampleApplicationContext-persistence.xml needed to put in a component-scan
//to scan for @Resource.  The component scan has an include-filter that includes this class, and the jpa scan excludes this class.

//So far I'm thinking that spring-data plus JpaRepository<OneFrickinTypeOfEntity, AndItsPrimaryKey> is pretty silly, especially 
//combined with the fact that it's component scan misses classes that annotate @Repository.

class BasicRepositoryImpl implements BasicRepository{
	//Chuck!! You can see whether the JPA second level cache has your entity like so:
	//eM.getEntityManagerFactory().getCache().contains(Class cls, Object  primaryKey)
	//This is the cache that outlives a transaction, and it's mode is set as shared-cache-mode in the 
	//JPA persistence unit. Spring doesn't always require you to have a persistence.xml, and as
	//of this writing this project doesn't have one, so I'm not yet sure how to set it.
	//We must therefore be using the default shared-cache-mode.
	//This site has some info about the available shared-cache-modes, and also showed me how
	//to programmatically access the cache: http://docs.oracle.com/javaee/6/tutorial/doc/gkjjj.html
	
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRepositoryImpl.class);
	
	@PersistenceContext
	protected EntityManager eM;
	
	public <T extends BaseEntity> T save(T entity) {
		
		if(entity.isNew())
			eM.persist(entity);
		else {
			eM.merge(entity);
		}
		return entity;
	}
	
	//Chuck!! Neat trick! See http://stackoverflow.com/questions/16043761/java-hibernate-entity-allow-to-set-related-object-both-by-id-and-object-itself
	//For Hibernate the trick is session.load(SomeEntity.class, id);
	public <T extends BaseEntity> T stubReferenceForId(Class<T> clazz, Long id) {
		return eM.getReference(clazz, id);
	}
	
	public <T extends BaseEntity> T findById(Class<T> clazz, Long id) {
		return eM.find(clazz, id);
	}
	
	/**
	 * 
	 * Please don't call this in a loop!
	 * If you already have the entity that you want to remove, and if you are in a transaction,
	 * then you wouldn't use this. (Instead use remove(BaseEntity.)  You use this when you're outside a transaction, because
	 * JPA cannot delete a detached entity.
	 */
	public <T extends BaseEntity> void remove(Class<T> clazz, Long id) {
	  remove(findById(clazz, id));
	}
	
	public void remove(BaseEntity entity) {
		eM.remove(entity);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends BaseEntity> List<T> findAll(Class<T> clazz) {
		Query query = eM.createQuery("select x from " + clazz.getSimpleName() + " x " );
		return query.getResultList();
	}
	
	
	public QuizContent findQuizContentByChoiceId(Long choiceId) {
		 String queryString = "select quizContent from QuizContent quizContent, Choice choice where choice.id = :id and choice.quizContent = quizContent";
		 TypedQuery<QuizContent> query = eM.createQuery(queryString, QuizContent.class);
		 query.setParameter("id", choiceId);
		 return query.getSingleResult();
	}
	
	public Answer latestAnswerForUser(User user) {
		if(user == null)
			throw new IllegalArgumentException("Null user");
		TypedQuery<Answer> query = eM.createQuery("select answer from Answer answer, User user where user.id = :userid and answer.user = :user" + 
	         " order by answer.creationTime desc" , Answer.class);
		//The join syntax below hasn't worked for me here yet.
	/*	TypedQuery<Answer> query = eM.createQuery("select answer from Answer answer inner join User user  on (answer.user = :user)  where user.id = :userid "
				+ "order by answer.creationTime desc" , Answer.class);	*/	
		query.setMaxResults(1);
		query.setParameter("userid", user.getId());
		query.setParameter("user", user);
		query.setMaxResults(1);
		LOGGER.debug("Finding next answer for user with id =  " + user.getId());
		List<Answer> answers = query.getResultList();
		return answers.isEmpty() ? null : answers.iterator().next();
		//Could have done getSingleResult, but didn't because we don't want "no entity found for query" exception here: return query.getSingleResult();
	}
	public Question findNextQuestionAndChoices( Answer answer){	
		TypedQuery<Question> query = eM.createQuery("select distinct question2 from  Question question2 inner join fetch question2.choices choices,  NextQuestion nextQuestion, Question question where"
    		+ "  nextQuestion.question = :question and nextQuestion.choice = :choice and"
			+ " nextQuestion.nextQuestion = question2 order by choices.id", Question.class);
		LOGGER.debug("Finding next question for questionId " + answer.getQuestion().getId() + " and choice " + answer.getChoice().getId());
		query.setParameter("question", answer.getQuestion());
		query.setParameter("choice", answer.getChoice());
		return query.getSingleResult();
	}
	
	public Question findQuestionAndChoices(Long questionId) {
		TypedQuery<Question> query = eM.createQuery("select question from Question question inner join fetch question.choices choices where question.id = :id order by choices.id", 
				Question.class);
		query.setParameter("id", questionId);
	    return query.getSingleResult();
	}
	
	public <T extends BaseEntity> T genericSingle(final String queryString, final Map<String, Object> params, final Class<T> clazz) {
		TypedQuery<T> query = eM.createQuery(queryString, clazz);
		for(Map.Entry<String, Object> entry  : params.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		return  query.getSingleResult();		
	}

}
