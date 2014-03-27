package org.tassoni.quizexample.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
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
import org.tassoni.quizexample.security.CurrentUser;
import org.tassoni.quizexample.service.QuizService;

//See http://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-three-custom-queries-with-query-methods/
//to learn how to use Spring Data jpa in some way that isn't silly.
@Controller
public class QuizController {
	public static final String USER_KEY = "user";
	private static final String LOGIN_URL = "/login/{username}";
	
	@Autowired
	private QuizService quizService;
	
    @RequestMapping(value = "/api/quiz", method = RequestMethod.POST)
    @ResponseBody
    public QuizContent add(@Valid  @RequestBody QuizContent quizContent) {
    	return quizService.saveQuizContent(quizContent);
    }
    
    
    //If the user has logged in and is resuming the quiz after being away long enough that the gui doen't know
    //the user's next question, go here.
    @RequestMapping(value = "/api/quiz/next", method = RequestMethod.GET)
    @ResponseBody
    public Question nextQuestionBasedOnlyOnUser() {
    	AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	User user = userDetails.getUser();
    	//TODO  Error if not logged in
    	//User user = (User) session.getAttribute(USER_KEY);
    	Question question =  quizService.findNextQuestion(user);
    	return question;
    }
    
    //Supposedly could also do, in RequestMapping, produces = MediatType.APLLICATION_JSON
    //and then not need @ResponseBody  Should also return the locaton of the next question.
    //See https://www.youtube.com/watch?v=wylViAqNiRA 43 minutes in
    @RequestMapping(value = "/api/quiz/question/{questionId}/choice/{choiceId}", method = RequestMethod.PUT)
    public ResponseEntity<QuizContent> answerQuestion(@PathVariable Long questionId, @PathVariable Long choiceId) {
    	AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	User user = userDetails.getUser();
    	//TODO  Redirect user to login url.  Best to do that with Spring Security.
    	//User user = (User) session.getAttribute(USER_KEY);
/*    	System.out.println("Here are the keys of the session " + sessionNames(session));
    	if(user == null) {
    		//Should really do a link-rel, I guess.
    		return new ResponseEntity<QuizContent>(new HttpHeaders() {
    			{
    				put("login url", Collections.singletonList(LOGIN_URL));
    			}
    		},HttpStatus.UNAUTHORIZED);
    	}
    	else {*/
    		quizService.saveAnswer(new Answer().setUser(user).setChoice(quizService.stubReferenceForId(Choice.class, choiceId)).
    				setQuestion(quizService.stubReferenceForId(Question.class, questionId)));
    		
    		//TODO  If no quizContent found, we have internal server error.
    		//And looks like we need to URL encode the data!  I see apostrophes getting mangled.
    		return new ResponseEntity<QuizContent>(quizService.findQuizContentByChoiceId(choiceId), HttpStatus.OK);
    	//}
    }
    
    private List<String> sessionNames(HttpSession session) {
        List<String> list = new ArrayList<String>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            list.add(names.nextElement());
        }
        return list;
    }
    
    //TODO Use Spring Security!!
    //Could add to this @RequestMapping consumes = MediaType.APPLICATION_JSON, according to minute 47:57 of https://www.youtube.com/watch?v=wylViAqNiRA
    //CHUCK!!! ---> That same video shows throwing specific Exceptions that can handle the possibility of no id found for that entity, etc!!
    //That's starting after minute 59 in that video
   // @RequestMapping(value = LOGIN_URL, method = RequestMethod.POST)
    //TODO  Tried to get password by itself from @RequestBody, but it was mangled.  Had to get the whole user.  It had \n and some other extra characters.
    //It's important to either find out how to get parameter values with @RequestBody, or to realize we always need to have @RequestBody <some dto or entity object>
    //TODO THe body we return here is pretty superfluous 
    //??Should it be @ResponseBody User rather than @RequestBody User, as in https://www.youtube.com/watch?v=wylViAqNiRA minute 47
/*	public ResponseEntity<Map<String, Object>> login(@PathVariable String username, @RequestBody User user, HttpSession session) {
		User retrieved = quizService.findUserByUsernameAndPassword(username, user.getPassword());
		if (retrieved == null) {
			return new ResponseEntity<Map<String, Object>>(
			new HashMap<String, Object>() {
				{
					put("Logged in", false);
				}
			}, HttpStatus.BAD_REQUEST);
		} else {
			session.setAttribute(USER_KEY, retrieved);
			return new ResponseEntity<Map<String, Object>>(new HashMap<String, Object>() {
				{
					put("Logged in", true);
				}
			}, HttpStatus.OK);
		}
	}
    */
    

}
