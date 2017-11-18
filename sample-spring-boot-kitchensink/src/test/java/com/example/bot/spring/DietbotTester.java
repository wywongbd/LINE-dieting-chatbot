package com.example.bot.spring;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.io.ByteStreams;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.LineBotMessages;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import com.example.bot.spring.DatabaseEngine;
import com.example.bot.spring.RecommendationState;
import com.example.bot.spring.InputMenuState;

import com.rivescript.Config;
import com.rivescript.RiveScript;
import com.rivescript.macro.Subroutine;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;


import java.util.Arrays;


@RunWith(SpringRunner.class)
//@SpringBootTest(classes = { DietbotTester.class, DatabaseEngine.class })
@SpringBootTest(classes = { DietbotTester.class, SQLDatabaseEngine.class })
public class DietbotTester {
	@Autowired
	private SQLDatabaseEngine databaseEngine;
	private RiveScript bot;
	private StateManager stateManager;
	

	@Before
	public void addTestUser() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("chicken potato soup");

		this.databaseEngine.addMenu("testUser", menu);
		this.databaseEngine.addRecommendations("testUser");
	}


	@After
	public void removeTestUser() {
		this.databaseEngine.resetMenu("testUser");
		this.databaseEngine.resetRecommendations("testUser");
	}

	
	@Test
	public void writeUserInfoExisting() {
		ArrayList<String> allergies = null;

		this.databaseEngine.writeUserInfo("testUser", 20, "male", 1.75, 60, allergies, 3, "testTopic", "testState");
		assertThat(this.databaseEngine.searchUser("testUser")).isEqualTo(true);
	}
	
	
	@Test
	public void writeUserInfoNonExisting() {
		ArrayList<String> allergies = new ArrayList<String>();
		allergies.add("milk");

		this.databaseEngine.writeUserInfo("testUserNonExisting", 21, "female", 1.64, 55, allergies, 3, "testTopic", "testState");
		assertThat(this.databaseEngine.searchUser("testUserNonExisting")).isEqualTo(true);
		this.databaseEngine.deleteUserInfo("testUserNonExisting");
	}
	

	@Test
	public void setUserInfoString() {
		this.databaseEngine.setUserInfo("testUser", "gender", "female");
		assertThat(this.databaseEngine.getUserInfo("testUser", "gender")).isEqualTo("female");
		this.databaseEngine.setUserInfo("testUser", "gender", "male");
		assertThat(this.databaseEngine.getUserInfo("testUser", "gender")).isEqualTo("male");
	}


	@Test
	public void setUserInfoInt() {
		this.databaseEngine.setUserInfo("testUser", "age", 55);
		assertThat(this.databaseEngine.getUserInfo("testUser", "age")).isEqualTo("55");
		this.databaseEngine.setUserInfo("testUser", "age", 20);
		assertThat(this.databaseEngine.getUserInfo("testUser", "age")).isEqualTo("20");
	}


	@Test
	public void setUserInfoDouble() {
		this.databaseEngine.setUserInfo("testUser", "height", 2.17);
		assertThat(this.databaseEngine.getUserInfo("testUser", "height")).isEqualTo("2.17");
		this.databaseEngine.setUserInfo("testUser", "height", 1.75);
		assertThat(this.databaseEngine.getUserInfo("testUser", "height")).isEqualTo("1.75");
	}


	@Test
	public void getMenuFound() {
		assertThat(this.databaseEngine.getMenu("testUser", "chicken")).isEqualTo("chicken potato soup");
	}


	@Test
	public void getMenuNotFound() {
		assertThat(this.databaseEngine.getMenu("testUser", "asdf")).isEqualTo(null);
	}


	@Test
	public void getRecommendationFound() {
		assertThat(this.databaseEngine.getRecommendation("testUser", "chicken")).isEqualTo("chicken potato soup");
	}


	@Test
	public void getRecommendationNotFound() {
		assertThat(this.databaseEngine.getRecommendation("testUser", "asdf")).isEqualTo(null);
	}


	@Test
	public void testAdd() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("frozen water");
		menu.add("molten ice");

		this.databaseEngine.resetMenu("testUserAddReset");
		this.databaseEngine.resetRecommendations("testUserAddReset");
		this.databaseEngine.addMenu("testUserAddReset", menu);
		this.databaseEngine.addRecommendations("testUserAddReset");
		assertThat(this.databaseEngine.getMenu("testUserAddReset", "frozen")).isEqualTo("frozen water");
		assertThat(this.databaseEngine.getMenu("testUserAddReset", "molten")).isEqualTo("molten ice");
		assertThat(this.databaseEngine.getRecommendation("testUserAddReset", "frozen")).isEqualTo("frozen water");
	}


	@Test
	public void testReset() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("frozen water");
		menu.add("molten ice");

		this.databaseEngine.addMenu("testUserAddReset", menu);
		this.databaseEngine.addRecommendations("testUserAddReset");
		this.databaseEngine.resetMenu("testUserAddReset");
		this.databaseEngine.resetRecommendations("testUserAddReset");
		assertThat(this.databaseEngine.getMenu("testUserAddReset", "frozen")).isEqualTo(null);
		assertThat(this.databaseEngine.getMenu("testUserAddReset", "molten")).isEqualTo(null);
		assertThat(this.databaseEngine.getRecommendation("testUserAddReset", "frozen")).isEqualTo(null);
	}
	

	@Test
	public void testRemoveAllergies() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("chicken potato soup");
		menu.add("grilled salmon");

		this.databaseEngine.addMenu("testUserAllergy", menu);
		this.databaseEngine.addRecommendations("testUserAllergy");
		this.databaseEngine.processRecommendationsByAllergies("testUserAllergy");
		assertThat(this.databaseEngine.getRecommendation("testUserAllergy", "chicken")).isEqualTo("chicken potato soup");
		assertThat(this.databaseEngine.getRecommendation("testUserAllergy", "salmon")).isEqualTo(null);
		this.databaseEngine.resetMenu("testUserAllergy");
		this.databaseEngine.resetRecommendations("testUserAllergy");
	}
	

	@Test
	public void testUpdateRecommendationsByIntake() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("chicken potato soup");
		menu.add("fruit salad");

		this.databaseEngine.addMenu("testUserIntake", menu);
		this.databaseEngine.addRecommendations("testUserIntake");
		this.databaseEngine.processRecommendationsByIntake("testUserIntake");
		assertThat(this.databaseEngine.getWeightage("testUserIntake", "chicken")).isEqualTo(2.5);
		assertThat(this.databaseEngine.getWeightage("testUserIntake", "fruit")).isEqualTo(6);
		this.databaseEngine.resetMenu("testUserIntake");
		this.databaseEngine.resetRecommendations("testUserIntake");
	}
	

	@Test
	public void getRecommendationList() {
		HashMap<String, Double> result = new HashMap<String, Double>();

		result = this.databaseEngine.getRecommendationList("testUser");
		assertThat(result.get("chicken potato soup")).isEqualTo(1.0);
	}
	

	@Test
	public void recommendFood() {
		String result = null;
		String resultString = "grilled salmon vege fish chip mayo rice fried chicken fish";
		RecommendationState recommend = new RecommendationState();
		ArrayList<String> foodList = new ArrayList<String>();
		foodList.add("grilled pork vege");
		foodList.add("fish chip mayo");
		foodList.add("rice fried chicken");
		foodList.add("fruit apple");

		result = recommend.recommendFood("testUser", foodList);
		assertThat(resultString.contains(result));
	}


	@Test
	public void testNewRivescript() throws Exception {
		bot = new RiveScript();
		File resourcesDirectory = new File("src/test/resources/rivescript");
		// assertThat(resourcesDirectory.getAbsolutePath()).isEqualTo("abc");
		bot.loadDirectory(resourcesDirectory.getAbsolutePath());

		// Sort the replies after loading them!
		bot.sortReplies();

		// Get a reply.
		String reply = bot.reply("user2", "can you tell me my id");
		assertThat(reply).isEqualTo("user2");
	}

	// to test how to use RiveScript with different users and get user variables
	// that have been set before or not.
	@Test
	public void testRivescriptToGetVariableFromDifferentUsers() throws Exception {
		bot = new RiveScript();
		File resourcesDirectory = new File("src/test/resources/rivescript");
		bot.loadDirectory(resourcesDirectory.getAbsolutePath());

		bot.sortReplies();

		String reply1 = bot.reply("user1", "can you tell me my id");
		assertThat(reply1).isEqualTo("user1");

		String reply2 = bot.reply("user2", "can you tell me my id");
		assertThat(reply2).isEqualTo("user2");

		// try to set user variable "name" for two different users
		String setName1 = bot.reply("user1", "my name is Gordon.");
		assertThat(setName1).contains("Gordon");
		String setName2 = bot.reply("user2", "my name is Tom.");
		assertThat(setName2).contains("Tom");

		// try to get them back and check equality
		String user1Name = bot.getUservar("user1", "name");
		assertThat(user1Name).isEqualTo("Gordon");
		String user2Name = bot.getUservar("user2", "name");
		assertThat(user2Name).isEqualTo("Tom");

		// try to get unset user variable
		String unsetVar = bot.getUservar("user1", "age");
		assertThat(unsetVar).isEqualTo(null);


		Map<String, String> varSetToValue = new HashMap<String, String>();
		varSetToValue.put("age", "100");
		varSetToValue.put("weight", "200");
		bot.setUservars("user1", varSetToValue);
		String age = bot.getUservar("user1", "age");
		assertThat(age).isEqualTo("100");

		String weight = bot.getUservar("user1", "weight");
		assertThat(weight).isEqualTo("200");
	}

	public class MyTestingSubroutine implements Subroutine {
		
 		public String call(RiveScript rs, String[] args) {
 			assertThat(args.length).isEqualTo(2);
 			assertThat(args[0]).isEqualTo("abc");
 			return "yes";
 		}
 	}
 
 	// to test how to use RiveScript Subroutine
 	@Test
 	public void testRivescriptSubroutine() throws Exception {
 		bot = new RiveScript();
 		File resourcesDirectory = new File("src/test/resources/rivescript");
 		bot.loadDirectory(resourcesDirectory.getAbsolutePath());
 
 		bot.sortReplies();
 
 		bot.setSubroutine("MyTestingSubroutine", new MyTestingSubroutine());
 
 		String reply1 = bot.reply("user1", "MyTestingSubroutine abc");
 		assertThat(reply1).isEqualTo("yes");
 	}
  	
	@Test
	public void convertHTMLTabletoJson() throws Exception{
		
		boolean thrown = false;
		String output = null;
		final String realOutput = "{\"Network\":[{\"2G bands\":\"GSM 900 / 1800 - SIM 1 & SIM 2\",\"Technology\":\"GSM\",\"GPRS\":\"Class 12\",\"EDGE\":\"Yes\"}]}";
		try{
			final String HTML = "<table cellspacing=\"0\" style=\"height: 24px;\">\r\n<tr class=\"tr-hover\">\r\n<th rowspan=\"15\" scope=\"row\">Network</th>\r\n<td class=\"ttl\"><a href=\"network-bands.php3\">Technology</a></td>\r\n<td class=\"nfo\"><a href=\"#\" class=\"link-network-detail collapse\">GSM</a></td>\r\n</tr>\r\n<tr class=\"tr-toggle\">\r\n<td class=\"ttl\"><a href=\"network-bands.php3\">2G bands</a></td>\r\n<td class=\"nfo\">GSM 900 / 1800 - SIM 1 & SIM 2</td>\r\n</tr>   \r\n<tr class=\"tr-toggle\">\r\n<td class=\"ttl\"><a href=\"glossary.php3?term=gprs\">GPRS</a></td>\r\n<td class=\"nfo\">Class 12</td>\r\n</tr>   \r\n<tr class=\"tr-toggle\">\r\n<td class=\"ttl\"><a href=\"glossary.php3?term=edge\">EDGE</a></td>\r\n<td class=\"nfo\">Yes</td>\r\n</tr>\r\n</table>";
			HTMLStringPreprocessing h = new HTMLStringPreprocessing();
			JSONObject jsonObj = h.parseHTMLTableToJson(HTML);
			output = jsonObj.toString();
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(output).isEqualTo(realOutput);
	}
	
	@Test
	public void testURLtoJSON() throws Exception{
		
		boolean thrown = false;
		String output = null;
		final String realOutput = "[apps snacks salads burgers sandwiches pairings desserts drinks, spinach queso dip, panseared pot stickers, chicken quesadilla, grilled salmon, flat iron steak, grilled salmon, flat iron steak, salads, soups, burger greenstyle, burgers sandwiches fries salad sweet potato fries instead, steaks ribs, pastas, chicken seafood, slushes, smoothies, freshly brewed teas, juices, handcrafted alcoholfree beverages made fruit pures natural flavors, refills freshly brewed teas slushes, fruit teas, slushes, drink options, casamigos strawberry rita, boba long island tea, sangria rita, peach sangria, crown apple cooler, tropical berry mojito shaker, tap drafts, happy tell what other local craft beers, bottles cans, red, white, bubbles, bottle selections, glutensensitive, tgi fridays franchisor llc drink responsibly locations see]";
		try{
			final String urlString = "https://tgifridays.com/menu/dine-in/";
			HTMLStringPreprocessing h = new HTMLStringPreprocessing();
			output =Arrays.toString(h.processURLRawContent((h.readFromUrl(urlString))).toArray());
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(output).isEqualTo(realOutput);
	}

	@Test
	public void testOCR() throws Exception{

		boolean thrown = false;
		String ans1 = null;
		String ans2 = null;
		String output1 = null;
		String output2 = null;
		final String path1 = "final-sample-menu-1.jpg";
		final String path2 = "final-sample-menu-2.jpg";
		final String realOutput1 = "splcy bean curd wllh mlnced pork served wllh rice\n" + 
				"sweet sour fork sewed thn rce\n" + 
				"chlh chlcken che\n" + 
				"fried instance needle luncheon meat";
		final String realOutput2 = "shortbread\n" + 
				"puddle cookie\n" + 
				"cookie\n" + 
				"macaroon\n" + 
				"biscotti\n" + 
				"ginger choc teabread\n" + 
				"organic bliss cookie\n" + 
				"loralyn bar\n" + 
				"muffin\n" + 
				"croissant\n" + 
				"organic bliss teabread\n" + 
				"glutenfree teabread\n" + 
				"cinnamon roll\n" + 
				"scone\n" + 
				"bear claw\n" + 
				"boulder cookiett\n" + 
				"brownie\n" + 
				"almond chocolate croissant\n" + 
				"ham cheese croissant";
		try{
			InputMenuState obj = new InputMenuState();
			ans1 = obj.ocrImagePath(path1);
			ans2 = obj.ocrImagePath(path2);
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(ans1.contains(realOutput1));
		assertThat(ans2.contains(realOutput2));
	}


	@Test
	public void testCollectUserInformation() throws Exception {
		
		boolean thrown = false;
		String input = null;
		String chatBotReponse = null;
		String expectedResponse = null;

		//example random userId from LINE
		String userId = "123";

		try{
			stateManager = new StateManager("src/test/resources/rivescriptChatbot");



			//random input at first when the user start chatting
			input = "fajsofifeojfeoijj";
    		expectedResponse = "Hi! I am your personal Dieting Chatbot!\n"
    						+ "First, I need to ask you a few questions about your physical information.\n"
    						+ "What is your name?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their name
    		input = "gord";
    		expectedResponse = "Is Gord your name?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user entered a wrong name so they say no
    		input = "no";
    		expectedResponse = "Can you enter your name again? Pls~";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their name again
    		input = "gordon";
    		expectedResponse = "Is Gordon your name?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "yes";
    		expectedResponse = "Ok. Nice to meet you Gordon! Next question! What is your age?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their age
    		input = "i'm 8";
    		expectedResponse = "Are you 8 years old?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your age again? Pls~";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their age again
    		input = "ok it should be 20";
    		expectedResponse = "Are you 20 years old?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "yeah";
    		expectedResponse = "Alright, what is your gender?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their gender
    		input = "M";
    		expectedResponse = "You are a m. Ok, so what is your weight (in kg)? Please input an integer.";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their weight
    		input = "60kg";
    		expectedResponse = "Is your weight 60kg?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your weight again? Please input an integer.";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their weight again
    		input = "62 kg";
    		expectedResponse = "Is your weight 62kg?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "Yes";
    		expectedResponse = "Alright, then what is your height in cm?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their height
    		input = "175cm";
    		expectedResponse = "Is your height 175cm?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your height (in cm) again? Please input an integer.";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their height
    		input = "176 cm";
    		expectedResponse = "Is your height 176cm?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "yes";
    		expectedResponse = "Great. Are you allergic to milk? (Yes/No)";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to eggs? (Yes/No)";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to nuts in general? (Yes/No)";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to seafood? (Yes/No)";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Thank you for your cooperation, "
    						+"I have a better understanding of your physical conditions now.";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//query user information to check the correctness
    		SQLDatabaseEngine db = new SQLDatabaseEngine();
    		assertThat(db.getUserInfo(userId, "age")).isEqualTo("20");
    		assertThat(db.getUserInfo(userId, "gender")).isEqualTo("male");
    		assertThat(db.getUserInfo(userId, "height")).isEqualTo("176.0");
    		assertThat(db.getUserInfo(userId, "weight")).isEqualTo("62.0");



    		//user want to update their personal info
    		input = "update";
    		expectedResponse = "Do you want to update your personal information?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);


    		//confirm
    		input = "yes";
    		expectedResponse = "OK. What is your name?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);


    		// repeat the questioning process

    		//user enter their name
    		input = "gord";
    		expectedResponse = "Is Gord your name?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user entered a wrong name so they say no
    		input = "no";
    		expectedResponse = "Can you enter your name again? Pls~";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their name again
    		input = "gordon";
    		expectedResponse = "Is Gordon your name?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "yes";
    		expectedResponse = "Ok. Nice to meet you Gordon! Next question! What is your age?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their age
    		input = "i'm 8";
    		expectedResponse = "Are you 8 years old?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your age again? Pls~";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their age again
    		input = "ok it should be 20";
    		expectedResponse = "Are you 20 years old?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "yeah";
    		expectedResponse = "Alright, what is your gender?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their gender
    		input = "M";
    		expectedResponse = "You are a m. Ok, so what is your weight (in kg)? Please input an integer.";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their weight
    		input = "60kg";
    		expectedResponse = "Is your weight 60kg?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your weight again? Please input an integer.";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their weight again
    		input = "80 kg";
    		expectedResponse = "Is your weight 80kg?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "Yes";
    		expectedResponse = "Alright, then what is your height in cm?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their height
    		input = "180cm";
    		expectedResponse = "Is your height 180cm?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your height (in cm) again? Please input an integer.";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their height
    		input = "181 cm";
    		expectedResponse = "Is your height 181cm?";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "yes";
    		expectedResponse = "Great. Are you allergic to milk? (Yes/No)";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to eggs? (Yes/No)";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to nuts in general? (Yes/No)";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to seafood? (Yes/No)";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Thank you for your cooperation, "
    						+"I have a better understanding of your physical conditions now.";
    		chatBotReponse = stateManager.chat(userId, input, false).firstElement();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);



    		assertThat(db.getUserInfo(userId, "age")).isEqualTo("20");
    		assertThat(db.getUserInfo(userId, "gender")).isEqualTo("male");
    		assertThat(db.getUserInfo(userId, "height")).isEqualTo("181.0");
    		assertThat(db.getUserInfo(userId, "weight")).isEqualTo("80.0");

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
	}

 }
 	
