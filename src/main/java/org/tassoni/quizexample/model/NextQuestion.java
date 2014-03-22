package org.tassoni.quizexample.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "nextquestions")
public class NextQuestion extends BaseEntity{
	//TODO  questionid and choiceid are composite foreign keys that point to the next question.  Is what I have so far an adequate representation of that?
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "questionid")
	private Question question;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "next_questionid")	
	private Question nextQuestion;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "choiceid")
	private Choice choice;

}
