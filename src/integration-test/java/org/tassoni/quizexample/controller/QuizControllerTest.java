package org.tassoni.quizexample.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;



//import net.petrikainulainen.spring.testmvc.IntegrationTestUtil;

//import org.hamcrest.generator.QuickReferenceWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.tassoni.quizexample.model.Answer;
import org.tassoni.quizexample.model.User;
import org.tassoni.quizexample.repository.BasicRepository;
import org.tassoni.quizexample.service.QuizService;

import com.jayway.jsonpath.JsonPath;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;
import static org.tassoni.quizexample.context.SecurityRequestPostProcessors.userDetailsService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:exampleApplicationContext.xml"})
@WebAppConfiguration
public class QuizControllerTest {
	
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private FilterChainProxy filterChainProxy;

    @Autowired
    private QuizService quizService;
    
    @Autowired 
    private PlatformTransactionManager platformTransactionManager;
    
    private TransactionTemplate transactionTemplate;
    
    @Autowired 
    private BasicRepository basicRepository;
     
    private MockMvc mockMvc;
    
    private final String userName = "Chuck";
    private final String correctPassword = "thePassword";
    private static final String QUOTE = "\"";
    
    @Before
    public void setUp() {
        mockMvc =  MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilter(filterChainProxy).build();
        //user = quizService.findUserByUsernameAndPassword(userName, correctPassword);
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }
    
   
    @Test
    public void nextQuestionWhenNotAuthenticated() throws Exception {
    	mockMvc.perform(get("/api/quiz/next")).andExpect(status().isUnauthorized());  	
    }
    
    @Test
    public void nextQuestion_WhenNoQuestionHasBeenAnsweredYet() throws Exception{
    	String json = mockMvc.perform(get("/api/quiz/next")
    			.with(userDetailsService("Chuck"))
    			//.sessionAttr(QuizController.USER_KEY, user))
    			).andExpect(status().isOk()).
    			andReturn().getResponse().getContentAsString();
    	
    	assertEquals("question.Id is not as expected", new Integer(1), JsonPath.read(json, "$.id"));
    	assertEquals("question.text is not as expected", "Do you have a LinkedIn account?", JsonPath.read(json, "$.text"));
    	assertTrue("question.edge should be null", null == JsonPath.read(json, "$.edge"));
    	assertTrue("question.whyImportant should be null", null == JsonPath.read(json, "$.whyImpportant"));
    	
    	assertEquals("choice texts are not as expected", new ArrayList<String>() {
    		{
    		  add("No");
    		  add("Yes");
    		}
    	}, JsonPath.read(json, "$.choices[*].text"));
    	assertEquals("choice ids are not as expected", new ArrayList<Integer>() {
    		{
    		  add(1);
    		  add(2);
    		}
    	}, JsonPath.read(json, "$.choices[*].id"));
    	
    	System.out.println("Json is " + json);
    }
    
    @Test
    public void answerQuestion() throws Exception {
    	//TODO  Check that the answer hasn't been saved before we act but is saved afterwards
    	//quizService.
    	User user = quizService.findUserByUsernameAndPassword(userName, correctPassword);
    	Answer latestAnswerBeforeAnsweringThisQuestion = findLatestAnswer(user);
    	
    	Long questionId = 1l; Long choiceId = 2l;
    	String json = answerQuestion(questionId, choiceId);
    	String mainContent = JsonPath.read(json, "$.main");
    	assertEquals(quizService.findQuizContentByChoiceId(choiceId).getMain(), mainContent);
    	
    	Answer latestAnswerAfterAnsweringThisQuestion = findLatestAnswer(user);
    	assertEquals(questionId, latestAnswerAfterAnsweringThisQuestion.getQuestion().getId());
    	assertEquals(choiceId, latestAnswerAfterAnsweringThisQuestion.getChoice().getId());
    	assertEquals(user.getId(), latestAnswerAfterAnsweringThisQuestion.getUser().getId());

    	assertTrue("Answer existed before we made a choice for our question ", 
    			latestAnswerBeforeAnsweringThisQuestion == null ||  ! latestAnswerAfterAnsweringThisQuestion.getId().equals(latestAnswerBeforeAnsweringThisQuestion.getId()));
    	
    	//I think we probably want to be able to answer the same question with more than one choice. 
    	//I'll try that out right here.
    	choiceId = 2l;
    	String jsonWhenAnsweringWithASecondChoice = answerQuestion(questionId, choiceId);
    	String mainContent2 = JsonPath.read(jsonWhenAnsweringWithASecondChoice, "$.main");
    	assertEquals(quizService.findQuizContentByChoiceId(choiceId).getMain(), mainContent2);
    	
    	Answer thisIsTheLastTimeWeAreAnsweringThisDarnQuestion = findLatestAnswer(user);
    	assertEquals(questionId, thisIsTheLastTimeWeAreAnsweringThisDarnQuestion.getQuestion().getId());
    	assertEquals(choiceId, thisIsTheLastTimeWeAreAnsweringThisDarnQuestion.getChoice().getId());
    	assertEquals(user.getId(), thisIsTheLastTimeWeAreAnsweringThisDarnQuestion.getUser().getId());
    	
    }
    
    private String answerQuestion(Long questionId, Long choiceId) throws Exception{
    	String path = String.format("/api/quiz/question/%d/choice/%d", questionId, choiceId);
     	return mockMvc.perform(put(path)
     			.with(userDetailsService("Chuck"))
     			//.sessionAttr(QuizController.USER_KEY, user))
     			).andExpect(status().isOk())
     			.andReturn().getResponse().getContentAsString();
    }
    
    @Test
    public void answerQuestionWhenNotAuthenticated() throws Exception{
    	String path = String.format("/api/quiz/question/%d/choice/%d", 1L, 1L);
     	mockMvc.perform(put(path)
     			).andExpect(status().isUnauthorized());   	
    }
    
    @Test
    public void nextQuestion_WhenOneQuestionHasAlreadyBeenAnswered() throws Exception{
    	answerQuestion(1l, 2l);
    	String json = mockMvc.perform(get("/api/quiz/next")
    			.with(userDetailsService("Chuck"))
    			//.sessionAttr(QuizController.USER_KEY, user))
    			).andExpect(status().isOk()).
    	andReturn().getResponse().getContentAsString();
    	
    	assertEquals("question.Id is not as expected", new Integer(2), JsonPath.read(json, "$.id"));
    	assertEquals("question.text is not as expected", "Do you have a resume? ", JsonPath.read(json, "$.text"));
    	assertEquals("question.edge is not as expected", "Want an edge? Create a business card or leave behind", JsonPath.read(json, "$.edge"));
    	assertTrue("question.whyImportant should be null", null == JsonPath.read(json, "$.whyImpportant"));
    	
    	assertEquals("choice texts are not as expected", new ArrayList<String>() {
    		{
    		  add("No");
    		  add("Yes");
    		}
    	}, JsonPath.read(json, "$.choices[*].text"));
    	assertEquals("choice ids are not as expected", new ArrayList<Integer>() {
    		{
    		  add(3);
    		  add(4);
    		}
    	}, JsonPath.read(json, "$.choices[*].id"));
    	
    	System.out.println("Json is " + json);
    }    
    
    
    private byte[] getBytes(String s) throws UnsupportedEncodingException {
        return s.getBytes("UTF8");
    }
    
    private Answer findLatestAnswer(final User user) {
    	return transactionTemplate.execute(new TransactionCallback<Answer>() {
			@Override
			public Answer doInTransaction(TransactionStatus status) {
				return basicRepository.latestAnswerForUser(user);
			}
		});
    };
    
}
