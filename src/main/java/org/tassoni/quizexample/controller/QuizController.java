package org.tassoni.quizexample.controller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tassoni.quizexample.model.Answer;
import org.tassoni.quizexample.model.Choice;
import org.tassoni.quizexample.model.Question;
import org.tassoni.quizexample.model.QuizContent;
import org.tassoni.quizexample.model.User;
import org.tassoni.quizexample.security.AppUserDetails;
import org.tassoni.quizexample.security.SecurityUtil;
import org.tassoni.quizexample.security.UserNotAuthenticatedException;
import org.tassoni.quizexample.service.QuizService;


//See http://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-three-custom-queries-with-query-methods/
//to learn how to use Spring Data jpa in some way that isn't silly.
@Controller
public class QuizController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuizController.class);
	
	public static final String USER_KEY = "user";
	private static final String LOGIN_URL = "/login/{username}";
	
	private QuizService quizService;
	
	@Autowired
	public QuizController(QuizService quizService) {
		this.quizService = quizService;
	}
    
    @ExceptionHandler(UserNotAuthenticatedException.class)
    ResponseEntity<String> handleMissingUser(Exception e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    //If the user has logged in and is resuming the quiz after being away long enough that the gui doen't know
    //the user's next question, go here.
    @RequestMapping(value = "/api/quiz/next", method = RequestMethod.GET)
    @ResponseBody
    public Question nextQuestionBasedOnlyOnUser() {
    	Question question =  quizService.findNextQuestion(SecurityUtil.getUser());
    	return question;
    }
    
    //Supposedly could also do, in RequestMapping, produces = MediatType.APLLICATION_JSON
    //and then not need @ResponseBody  Should also return the location of the next question.
    //See https://www.youtube.com/watch?v=wylViAqNiRA 43 minutes in
    @RequestMapping(value = "/api/quiz/question/{questionId}/choice/{choiceId}", method = RequestMethod.PUT)
	public ResponseEntity<QuizContent> answerQuestion(
			@PathVariable Long questionId, @PathVariable Long choiceId) {
		User user =SecurityUtil.getUser();
		Answer answer = quizService.saveAnswer(new Answer()
				.setUser(user)
				.setChoice(
						quizService.stubReferenceForId(Choice.class, choiceId))
				.setQuestion(
						quizService.stubReferenceForId(Question.class,
								questionId)));
		LOGGER.debug("Saved answer has id " + answer.getId());
		//TODO Does the client care about the id of the answer just created?
		// TODO If no quizContent found, we have internal server error.
		// And looks like we need to URL encode the data! I see apostrophes
		// getting mangled.
		return new ResponseEntity<QuizContent>(
				quizService.findQuizContentByChoiceId(choiceId), HttpStatus.OK);

	}

    
    //Could add to this @RequestMapping consumes = MediaType.APPLICATION_JSON, according to minute 47:57 of https://www.youtube.com/watch?v=wylViAqNiRA
    //CHUCK!!! ---> That same video shows throwing specific Exceptions that can handle the possibility of no id found for that entity, etc!!
    //That's starting after minute 59 in that video
   // @RequestMapping(value = LOGIN_URL, method = RequestMethod.POST)
    //TODO  Tried to get password by itself from @RequestBody, but it was mangled.  Had to get the whole user.  It had \n and some other extra characters.
    //It's important to either find out how to get parameter values with @RequestBody, or to realize we always need to have @RequestBody <some dto or entity object>

    

}
