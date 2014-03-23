package org.tassoni.quizexample.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.constraints.AssertTrue;











import net.sf.cglib.transform.impl.AddDelegateTransformer;


//import org.hamcrest.generator.QuickReferenceWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.tassoni.quizexample.controller.QuizController;
import org.tassoni.quizexample.model.User;
import org.tassoni.quizexample.service.QuizService;

import com.jayway.jsonpath.JsonPath;













//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:exampleApplicationContext.xml"})
@WebAppConfiguration
public class QuizControllerTest {
	
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private QuizService quizService;
    
     
    private MockMvc mockMvc;
    
    private final String userName = "Chuck";
    private final String correctPassword = "thePassword";
    private static final String QUOTE = "\"";
    private User user;
    
    @Before
    public void setUp() {
        mockMvc =  MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        user = quizService.findUserByUsernameAndPassword(userName, correctPassword);
    }
    
    @Test
    public void login_badPassword() throws Exception {
    	String incorrectPassword = correctPassword + "x"; 
    	String json = mockMvc.perform(post("/login/Chuck").contentType(MediaType.APPLICATION_JSON).content(getBytes("{ \"password\": " + QUOTE + incorrectPassword + QUOTE + "}")) ).
    	andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
	
    }
    
    @Test
    public void login_withCorrectCredentials() throws Exception{
    	String json = mockMvc.perform(post("/login/Chuck").contentType(MediaType.APPLICATION_JSON).content(getBytes("{ \"password\": " + QUOTE + correctPassword + QUOTE + "}")) ).
    	andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    	System.out.println("JSON was " + json);
    }
    
    @Test
    public void nextQuestion_WhenNoQuestionHasBeenAnsweredYet() throws Exception{
    	String json = mockMvc.perform(get("/api/quiz/next").sessionAttr(QuizController.USER_KEY, user)).andExpect(status().isOk()).
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
    	Long questionId = 1l; Long choiceId = 2l;
    	String json = answerQuestion(questionId, choiceId);
    	String mainContent = JsonPath.read(json, "$.main");
    	assertEquals(quizService.findQuizContentByChoiceId(choiceId).getMain(), mainContent);
    }
    
    private String answerQuestion(Long questionId, Long choiceId) throws Exception{
    	String path = String.format("/api/quiz/question/%d/choice/%d", questionId, choiceId);
     	return mockMvc.perform(put(path).sessionAttr(QuizController.USER_KEY, user)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }
    
    @Test
    public void nextQuestion_WhenOneQuestionHasAlreadyBeenAnswered() throws Exception{
    	answerQuestion(1l, 2l);
    	String json = mockMvc.perform(get("/api/quiz/next").sessionAttr(QuizController.USER_KEY, user)).andExpect(status().isOk()).
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
}
