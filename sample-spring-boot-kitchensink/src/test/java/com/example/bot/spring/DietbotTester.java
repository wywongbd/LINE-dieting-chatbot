package com.example.bot.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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
import java.time.Instant;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.LineBotMessages;
import com.example.bot.spring.DietbotController.DownloadedContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.event.postback.PostbackContent;
import com.linecorp.bot.model.event.PostbackEvent;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import com.example.bot.spring.RecommendationState;
import com.example.bot.spring.InputMenuState;
import com.example.bot.spring.OCRStringPreprocessing;
import com.example.bot.spring.StateManager;

import com.rivescript.Config;
import com.rivescript.RiveScript;
import com.rivescript.macro.Subroutine;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;


@RunWith(SpringRunner.class)
//@SpringBootTest(classes = { DietbotTester.class, DatabaseEngine.class })
@SpringBootTest(classes = { DietbotTester.class, SQLDatabaseEngine.class })
public class DietbotTester {
	@Autowired
	private static SQLDatabaseEngine databaseEngine;
	private RiveScript bot;
	private StateManager stateManager;

	static {
		databaseEngine = new SQLDatabaseEngine();
	}
	
	public DietbotTester() {
		bot = new RiveScript();
		stateManager = new StateManager("src/main/resources/rivescript");
	}

	@BeforeClass
	public static void addTestUser() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("chicken potato soup");
		ArrayList<String> allergies = new ArrayList<String>();
		allergies.add("seafood");

		 databaseEngine.writeUserInfo("testUser", 20, "male", 1.75, 60, allergies, "normal", "testTopic", "testState");
		 databaseEngine.writeUserInfo("testUserIntake", 19, "male", 2.15, 80, new ArrayList<String>(), "normal", "testTopic", "testState");
		 databaseEngine.writeUserInfo("testUserAllergy", 18, "female", 1.63, 55, allergies, "normal", "testTopic", "testState");
		 databaseEngine.writeUserInfo("testUserHistory", 21, "male", 1.73, 65, allergies, "normal", "testTopic", "testState");
		 databaseEngine.writeUserInfo("testUserGoalLittle", 22, "male", 1.69, 69, allergies, "little_diet", "testTopic", "testState");
		 databaseEngine.writeUserInfo("testUserGoalSerious", 23, "male", 1.71, 68, allergies, "serious_diet", "testTopic", "testState");
		 databaseEngine.writeUserInfo("testUserCalories", 24, "male", 1.83, 77, allergies, "normal", "testTopic", "testState");
		 databaseEngine.writeUserInfo("testUserInputImage", 22, "male", 1.70, 81, allergies, "normal", "standby", "standby");
		 databaseEngine.writeUserInfo("testUserChatImageInputMenu1", 15, "male", 1.72, 82, allergies, "normal", "input_menu", "input_menu");
		 databaseEngine.writeUserInfo("testUserChatImageInputMenu2", 15, "male", 1.72, 82, allergies, "normal", "input_menu", "input_menu");
		 databaseEngine.writeUserInfo("testUserChatImageUpdateUserInfo", 17, "male", 1.73, 83, allergies, "normal", "update_user_info", "update_user_info");
		 databaseEngine.writeUserInfo("testUserChatImagePostEating", 18, "male", 1.74, 84, allergies, "normal", "post_eating", "post_eating");
		 databaseEngine.addMenu("testUser", menu);
		 databaseEngine.addRecommendations("testUser");
	}

	@AfterClass
	public static void removeTestUser() {
		databaseEngine.reset("testUser", "userinfo");
		databaseEngine.reset("testUser", "menu");
		databaseEngine.reset("testUser", "recommendations");
		databaseEngine.reset("testUser", "userallergies");
		databaseEngine.reset("testUserIntake", "userinfo");
		databaseEngine.reset("testUserIntake", "userallergies");
		databaseEngine.reset("testUserAllergy", "userinfo");
		databaseEngine.reset("testUserAllergy", "userallergies");
		databaseEngine.reset("testUserHistory", "userinfo");
		databaseEngine.reset("testUserHistory", "userallergies");
		databaseEngine.reset("testUserGoalLittle", "userinfo");
		databaseEngine.reset("testUserGoalLittle", "userallergies");
		databaseEngine.reset("testUserGoalSerious", "userinfo");
		databaseEngine.reset("testUserGoalSerious", "userallergies");
		databaseEngine.reset("testUserCalories", "userinfo");
		databaseEngine.reset("testUserCalories", "userallergies");
		databaseEngine.reset("testUserInputImage", "userinfo");
	}

	@Test
	public void writeUserInfoExisting() {
		ArrayList<String> allergies = null;

		this.databaseEngine.writeUserInfo("testUser", 20, "male", 1.75, 60, allergies, "normal", "testTopic", "testState");
		assertThat(this.databaseEngine.searchUser("testUser", "userinfo")).isEqualTo(true);
	}
	
	
	@Test
	public void writeUserInfoNonExisting() {
		ArrayList<String> allergies = new ArrayList<String>();
		allergies.add("milk");

		this.databaseEngine.writeUserInfo("testUserNonExisting", 21, "female", 1.64, 55, allergies, "normal", "testTopic", "testState");
		assertThat(this.databaseEngine.searchUser("testUserNonExisting", "userinfo")).isEqualTo(true);
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
	public void setUserAllergies() {
		ArrayList<String> allergies = new ArrayList<String>();
		allergies.add("nut");

		this.databaseEngine.setUserAllergies("testUser", allergies);
		assertThat(this.databaseEngine.getUserAllergies("testUser")).isEqualTo(allergies);

		allergies.remove("nut");

		this.databaseEngine.setUserAllergies("testUser", allergies);
		assertThat(this.databaseEngine.getUserAllergies("testUser")).isEqualTo(allergies);
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
		menu.add("fish and chips");
		menu.add("sausages and chicken wings");

		this.databaseEngine.reset("testUserAddReset", "menu");
		this.databaseEngine.reset("testUserAddReset", "recommendations");
		this.databaseEngine.addMenu("testUserAddReset", menu);
		this.databaseEngine.addRecommendations("testUserAddReset");
		assertThat(this.databaseEngine.getMenu("testUserAddReset", "fish")).isEqualTo("fish and chips");
		assertThat(this.databaseEngine.getMenu("testUserAddReset", "sausage")).isEqualTo("sausages and chicken wings");
		assertThat(this.databaseEngine.getRecommendation("testUserAddReset", "fish")).isEqualTo("fish and chips");
		assertThat(this.databaseEngine.getRecommendation("testUserAddReset", "sausage")).isEqualTo("sausages and chicken wings");
		this.databaseEngine.reset("testUserAddReset", "menu");
		this.databaseEngine.reset("testUserAddReset", "recommendations");
	}


	@Test
	public void testReset() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("fish and chips");
		menu.add("sausages and chicken wings");

		this.databaseEngine.addMenu("testUserAddReset", menu);
		this.databaseEngine.addRecommendations("testUserAddReset");
		this.databaseEngine.reset("testUserAddReset", "menu");
		this.databaseEngine.reset("testUserAddReset", "recommendations");
		assertThat(this.databaseEngine.getMenu("testUserAddReset", "fish")).isEqualTo(null);
		assertThat(this.databaseEngine.getMenu("testUserAddReset", "sausage")).isEqualTo(null);
		assertThat(this.databaseEngine.getRecommendation("testUserAddReset", "fish")).isEqualTo(null);
		assertThat(this.databaseEngine.getRecommendation("testUserAddReset", "sausage")).isEqualTo(null);
	}


	@Test
	public void addUserEatingHistory() {
		String meals1 = "chicken soup, spaghetti bolognese";
		String meals2 = "apples, chocolate cake";
		this.databaseEngine.addUserEatingHistory("testUserEating", meals1);
		this.databaseEngine.addUserEatingHistory("testUserEating", meals2);
		assertThat(this.databaseEngine.searchUser("testUserEating", "eating_history")).isEqualTo(true);
		assertThat(this.databaseEngine.getUserEatingHistory("testUserEating", 1).get(0)).isEqualTo(meals1);
		assertThat(this.databaseEngine.getUserEatingHistory("testUserEating", 1).get(1)).isEqualTo(meals2);
		this.databaseEngine.reset("testUserEating", "eating_history");
	}
	

	@Test
	public void processRecommendationsByAllergies() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("chicken potato soup");
		menu.add("grilled salmon");

		this.databaseEngine.addMenu("testUserAllergy", menu);
		this.databaseEngine.addRecommendations("testUserAllergy");
		this.databaseEngine.processRecommendationsByAllergies("testUserAllergy");
		assertThat(this.databaseEngine.getRecommendation("testUserAllergy", "chicken")).isEqualTo("chicken potato soup");
		assertThat(this.databaseEngine.getRecommendation("testUserAllergy", "salmon")).isEqualTo(null);
		this.databaseEngine.reset("testUserAllergy", "menu");
		this.databaseEngine.reset("testUserAllergy", "recommendations");
	}
	

	@Test
	public void processRecommendationsByIntake() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("chicken potato soup");
		menu.add("caramel apples");

		this.databaseEngine.addMenu("testUserIntake", menu);
		this.databaseEngine.addRecommendations("testUserIntake");
		this.databaseEngine.processRecommendationsByIntake("testUserIntake");
		assertThat(this.databaseEngine.getWeightage("testUserIntake", "chicken")).isEqualTo(2.5);
		assertThat(this.databaseEngine.getWeightage("testUserIntake", "apples")).isEqualTo(2);
		this.databaseEngine.reset("testUserIntake", "menu");
		this.databaseEngine.reset("testUserIntake", "recommendations");
	}
	

	@Test
	public void processRecommendationsByEatingHistory() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("apple");
		menu.add("banana");
		menu.add("orange");
		String meals1 = "apples";
		String meals2 = "bananas";

		this.databaseEngine.addMenu("testUserHistory", menu);
		this.databaseEngine.addRecommendations("testUserHistory");
		this.databaseEngine.addUserEatingHistory("testUserHistory", meals1);
		this.databaseEngine.addUserEatingHistory("testUserHistory", meals1);
		this.databaseEngine.addUserEatingHistory("testUserHistory", meals2);
		this.databaseEngine.processRecommendationsByIntake("testUserHistory");
		this.databaseEngine.processRecommendationsByEatingHistory("testUserHistory");

		assertThat(this.databaseEngine.getWeightage("testUserHistory", "apple")).isEqualTo(0.5);
		assertThat(this.databaseEngine.getWeightage("testUserHistory", "banana")).isEqualTo(1);
		assertThat(this.databaseEngine.getWeightage("testUserHistory", "orange")).isEqualTo(2);

		this.databaseEngine.reset("testUserHistory", "menu");
		this.databaseEngine.reset("testUserHistory", "recommendations");
		this.databaseEngine.reset("testUserHistory", "eating_history");
	}


	@Test
	public void processRecommendationsByGoal() {
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("apple");
		menu.add("broccoli");
		menu.add("cereal");
		menu.add("chicken");

		this.databaseEngine.addMenu("testUserGoalLittle", menu);
		this.databaseEngine.addRecommendations("testUserGoalLittle");
		this.databaseEngine.addMenu("testUserGoalSerious", menu);
		this.databaseEngine.addRecommendations("testUserGoalSerious");
		this.databaseEngine.processRecommendationsByGoal("testUserGoalLittle");
		this.databaseEngine.processRecommendationsByGoal("testUserGoalSerious");

		assertThat(this.databaseEngine.getWeightage("testUserGoalLittle", "apple")).isEqualTo(1);
		assertThat(this.databaseEngine.getWeightage("testUserGoalLittle", "broccoli")).isEqualTo(1);
		assertThat(this.databaseEngine.getWeightage("testUserGoalLittle", "cereal")).isEqualTo(0.7);
		assertThat(this.databaseEngine.getWeightage("testUserGoalLittle", "chicken")).isEqualTo(0.8);
		assertThat(this.databaseEngine.getWeightage("testUserGoalSerious", "apple")).isEqualTo(1.2);
		assertThat(this.databaseEngine.getWeightage("testUserGoalSerious", "broccoli")).isEqualTo(1.2);
		assertThat(this.databaseEngine.getWeightage("testUserGoalSerious", "cereal")).isEqualTo(0.5);
		assertThat(this.databaseEngine.getWeightage("testUserGoalSerious", "chicken")).isEqualTo(0.6);

		this.databaseEngine.reset("testUserGoalLittle", "menu");
		this.databaseEngine.reset("testUserGoalLittle", "recommendations");
		this.databaseEngine.reset("testUserGoalSerious", "menu");
		this.databaseEngine.reset("testUserGoalSerious", "recommendations");
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
	public void addCampaignUser() {
		this.databaseEngine.addCampaignUser("testUser");
		assertThat(this.databaseEngine.searchUser("testUser", "campaign_user")).isEqualTo(true);
		this.databaseEngine.reset("testUser", "campaign_user");
		assertThat(this.databaseEngine.searchUser("testUser", "campaign_user")).isEqualTo(false);
	}


	@Test
	public void generateAndStoreCode() {
		ArrayList<String> result = new ArrayList<String>();

		this.databaseEngine.generateAndStoreCode("testUserCode");
		result = this.databaseEngine.getCodeInfo(100000);
		assertThat(result.get(0)).isEqualTo("testUserCode");
		assertThat(result.get(1)).isEqualTo(null);
		this.databaseEngine.resetCoupon("testUserCode");
		assertThat(this.databaseEngine.searchUser("testUserCode", "campaign_user")).isEqualTo(false);
	}


	@Test
	public void claimCode() {
		ArrayList<String> result = new ArrayList<String>();

		this.databaseEngine.addCampaignUser("testUserClaim");
		this.databaseEngine.generateAndStoreCode("testUserCode");
		this.databaseEngine.claimCode("testUserClaim", 100000);
		assertThat(this.databaseEngine.searchUser("testUserCode", "campaign_user")).isEqualTo(false);
		result = this.databaseEngine.getCodeInfo(100000);
		assertThat(result.get(0)).isEqualTo("testUserCode");
		assertThat(result.get(1)).isEqualTo("testUserClaim");
		this.databaseEngine.reset("testUserClaim", "campaign_user");
		this.databaseEngine.resetCoupon("testUserCode");
	}


	@Test
	public void couponExceeds5000() {
		this.databaseEngine.generateAndStoreCode("testUserCode");
		assertThat(this.databaseEngine.couponExceeds5000(4)).isEqualTo(false);
		this.databaseEngine.generateAndStoreCode("testUserCode");
		assertThat(this.databaseEngine.couponExceeds5000(4)).isEqualTo(false);
		this.databaseEngine.claimCode("testUserClaim", 100000);
		assertThat(this.databaseEngine.couponExceeds5000(4)).isEqualTo(false);
		this.databaseEngine.claimCode("testUserClaim2", 100001);
		assertThat(this.databaseEngine.couponExceeds5000(4)).isEqualTo(true);
		this.databaseEngine.resetCoupon("testUserCode");
	}


	@Test
	public void setCouponUrl() {
		String orgUrl = "https://dieting-chatbot.herokuapp.com/downloaded/2017-11-20T07:33:48.762-49f1625f-82f7-4c67-91cf-bb87586273b9.jpg";
		String url = "testCouponUrlButIPurposedlyMakeItLongerJustToTestIfItCanHandleLongLengths";

		this.databaseEngine.setCouponUrl(url);
		assertThat(this.databaseEngine.getCouponUrl()).isEqualTo(url);
		this.databaseEngine.setCouponUrl(orgUrl);
		assertThat(this.databaseEngine.getCouponUrl()).isEqualTo(orgUrl);
	}


	@Test
	public void setCampaign() {
		this.databaseEngine.setCampaign(1);
		assertThat(this.databaseEngine.isCampaignOpen()).isEqualTo(true);
		this.databaseEngine.setCampaign(0);
		assertThat(this.databaseEngine.isCampaignOpen()).isEqualTo(false);
	}


	@Test
	public void getAverageConsumptionInfo() {
		ArrayList<Double> result = new ArrayList<Double>();

		this.databaseEngine.addUserEatingHistory("testUserConsumptionInfo", "fried chicken, chocolate cake");
		result = this.databaseEngine.getAverageConsumptionInfo("testUserConsumptionInfo", 1);
		assertEquals(1050, result.get(0), 0.1);
		assertEquals(291.1, result.get(1), 0.1);
		assertEquals(4.3, result.get(2), 0.1);
		this.databaseEngine.addUserEatingHistory("testUserConsumptionInfo", "fried chicken, chocolate cake");
		result = this.databaseEngine.getAverageConsumptionInfo("testUserConsumptionInfo", 1);
		assertEquals(2100, result.get(0), 0.1);
		assertEquals(582.2, result.get(1), 0.1);
		assertEquals(8.6, result.get(2), 0.1);
		this.databaseEngine.reset("testUserConsumptionInfo", "eating_history");
	}


	@Test
	public void getNutritionInfo() {
		ArrayList<Double> result = new ArrayList<Double>();

		result = this.databaseEngine.getNutritionInfo("fried chicken");
		assertThat(result.get(0)).isEqualTo(71);
		assertThat(result.get(1)).isEqualTo(354);
		assertThat(result.get(2)).isEqualTo(0.79);
	}


	@Test
	public void claimCheatDay() {
		this.databaseEngine.addUserEatingHistory("testUserCheatDay", "cheat day");
		assertThat(this.databaseEngine.searchUser("testUserCheatDay", "eating_history")).isEqualTo(true);
		assertThat(this.databaseEngine.getUserEatingHistory("testUserCheatDay", 1).get(0)).isEqualTo("cheat day");
		this.databaseEngine.reset("testUserCheatDay", "eating_history");
	}


	@Test
	public void canClaimCheatDay() {
		assertThat(this.databaseEngine.canClaimCheatDay("testUserCanClaimCheatDay")).isEqualTo(true);
		this.databaseEngine.addUserEatingHistory("testUserCanClaimCheatDay", "cheat day");
		assertThat(this.databaseEngine.canClaimCheatDay("testUserCanClaimCheatDay")).isEqualTo(false);
		this.databaseEngine.reset("testUserCanClaimCheatDay", "eating_history");
	}


	@Test
	public void exceedDailyCalorieQuota() {
		this.databaseEngine.addUserEatingHistory("testUserCalories", "fried chicken, chocolate cake");
		assertThat(this.databaseEngine.exceedDailyCalorieQuota("testUserCalories")).isEqualTo(false);
		this.databaseEngine.addUserEatingHistory("testUserCalories", "fried chicken, chocolate cake");
		assertThat(this.databaseEngine.exceedDailyCalorieQuota("testUserCalories")).isEqualTo(false);
		this.databaseEngine.addUserEatingHistory("testUserCalories", "fried chicken, chocolate cake");
		assertThat(this.databaseEngine.exceedDailyCalorieQuota("testUserCalories")).isEqualTo(true);
		this.databaseEngine.reset("testUserCalories", "eating_history");
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
	public void testJSONUrl() throws Exception{
		boolean thrown = false;
		
		final String JSONUrl = "http://www.json-generator.com/api/json/get/cjTeRHAnfS?indent=2";
		Dish[] actualDishes = new Dish[3];
		String actualResponse = "";
		
		ArrayList<String> firstIngredients = new ArrayList<String>();
		firstIngredients.add("Pork");
		firstIngredients.add("Bean curd");
		firstIngredients.add("Rice");   
		
		actualDishes[0] = new Dish(35, "Spicy Bean curd with Minced Pork served with Rice", firstIngredients);
		 
		ArrayList<String> secondIngredients = new ArrayList<String>();
		firstIngredients.add("Pork"); 
		firstIngredients.add("Sweet and Sour Sauce");
		firstIngredients.add("Pork");
		
		actualDishes[1] = new Dish(36, "Sweet and Sour Pork served with Rice", secondIngredients);
	
		ArrayList<String> thirdIngredients = new ArrayList<String>();
		firstIngredients.add("Chili");
		firstIngredients.add("Chicken");
		firstIngredients.add("Rice"); 
		
		actualDishes[2] = new Dish(28, "Chili Chicken on Rice", thirdIngredients);
	
		actualResponse += Arrays.toString(JSONPreprocessing.getDishName(actualDishes));
		
		String rawJSONString = "";
		Dish[] dishObjects = null; 
		String observedResponse = "";
		
		try {
			rawJSONString += JSONPreprocessing.readJSONUrl(JSONUrl);
			dishObjects = JSONPreprocessing.getDishFromJSON(rawJSONString);
			observedResponse = Arrays.toString(JSONPreprocessing.getDishName(dishObjects));
			 
		}
		catch (Exception e){ 
			thrown = true;
		}
		
		assertThat(observedResponse).isEqualTo(actualResponse);
		
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

		// delete the testing userId first
		databaseEngine.reset("testCollectUserInformation", "userinfo");
		databaseEngine.reset("testCollectUserInformation", "userallergies");

		//example random userId from LINE
		String userId = "testCollectUserInformation";
		stateManager = new StateManager("src/test/resources/rivescriptChatbot");

		try{
			//random input at first when the user start chatting
			input = "fajsofifeojfeoijj";
    		expectedResponse = "Hi! I am your personal Dieting Chatbot!\n"
    						+ "First, I need to ask you a few questions about your physical information.\n"
    						+ "What is your name?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their name
    		input = "gord";
    		expectedResponse = "Is Gord your name?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{

    		//user entered a wrong name so they say no
    		input = "no";
    		expectedResponse = "Can you enter your name again? Pls~";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their name again
    		input = "gordon";
    		expectedResponse = "Is Gordon your name?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "yes";
    		expectedResponse = "Ok. Nice to meet you Gordon! Next question! What is your age?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		
		try{
    		//user enter their age
    		input = "i'm 8";
    		expectedResponse = "Are you 8 years old?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your age again? Pls~";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their age again
    		input = "ok it should be 20";
    		expectedResponse = "Are you 20 years old?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//confirm
    		input = "yeah";
    		expectedResponse = "Alright, what is your gender?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		
		try{
    		//user enter their gender
    		input = "M";
    		expectedResponse = "You are a m. Ok, so what is your weight (in kg)? Please input an integer.";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their weight
    		input = "60kg";
    		expectedResponse = "Is your weight 60kg?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your weight again? Please input an integer.";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their weight again
    		input = "62 kg";
    		expectedResponse = "Is your weight 62kg?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		
		try{
    		//confirm
    		input = "Yes";
    		expectedResponse = "Alright, then what is your height in cm?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their height
    		input = "175cm";
    		expectedResponse = "Is your height 175cm?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		input = "no";
    		expectedResponse = "Can you enter your height (in cm) again? Please input an integer.";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//user enter their height
    		input = "176 cm";
    		expectedResponse = "Is your height 176cm?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		
		try{
    		//confirm
    		input = "yes";
    		expectedResponse = "Great. Are you allergic to milk? (Yes/No)";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to eggs? (Yes/No)";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to nuts in general? (Yes/No)";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer allergy
    		input = "yes";
    		expectedResponse = "I see, I'll take note of that. Are you allergic to seafood? (Yes/No)";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		
		try{
    		//answer allergy
    		input = "yes";
    		expectedResponse ="I see, I'll take note of that. From a scale of 1 to 3, how urgent are you in cutting down your weight? Please input 1, 2 or 3." 
        						+ "\n- 1. I am ok with my current weight, but slimming down just a little would be perfect."
						        + "\n- 2. I am not satisfied with my current weight, I hope to lose weight significantly to be more healthy."
						        + "\n- 3. I am in an emergency and I need to lose weight immediately, the sooner the better!";
			chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//answer health goal
    		input = "1";
    		expectedResponse ="Great, thanks for cooperating. I have a better understanding of your physical condition now. "
    							+ "I'll try my best to help you reach your health goal!";
			chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//query user information to check the correctness
    		assertThat(databaseEngine.getUserInfo(userId, "age")).isEqualTo("20");
    		assertThat(databaseEngine.getUserInfo(userId, "gender")).isEqualTo("male");
    		assertThat(databaseEngine.getUserInfo(userId, "height")).isEqualTo("176.0");
    		assertThat(databaseEngine.getUserInfo(userId, "weight")).isEqualTo("62.0");

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		
		try{
    		//user want to update their personal info
    		input = "update";
    		expectedResponse = "Do you want to update your personal information?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);


    		//confirm
    		input = "yes";
    		expectedResponse = "OK. Which one do you want to update? Pick one.\n" 
    							+ "- name\n"
    							+ "- age\n"
    							+ "- gender\n"
    							+ "- weight\n"
    							+ "- height\n"
    							+ "- allergy\n"
    							+ "- health goal";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);


    		//user want to update their personal info
    		input = "age";
    		expectedResponse = "What is your age?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);


		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{
    		// repeat the questioning process

    		//user enter their age
    		input = "30";
    		expectedResponse = "Are you 30 years old?";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{

    		//confirm
    		input = "yes";
    		expectedResponse = "Alright!";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);
    		
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{

    		assertThat(databaseEngine.getUserInfo(userId, "age")).isEqualTo("30");

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{

    		//test inputMenuState
    		input = "input";
    		expectedResponse = "Please let me have a look at your menu first. You can take a photo, or share the menu url with me."
    							+ "\nIf you want to input menu using text, pls input food only and separate them by a comma.";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		
    		//confirm
    		input = "orange";
    		expectedResponse = "Alright!";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{
    		//test inputMenuState
    		input = "input";
    		expectedResponse = "Please let me have a look at your menu first. You can take a photo, or share the menu url with me."
    							+ "\nIf you want to input menu using text, pls input food only and separate them by a comma.";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		
    		//confirm
    		input = "http://fake_url";
    		expectedResponse = "Thanks, I'm looking at your url now! I'll try to give you some recommendations.";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);
    		
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{
    		//test inputMenuState
    		input = "friend:generate";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse.length()).isEqualTo("Thank you, your code is ".length() + 6);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{

    		//test provide info state
    		input = "check";
    		expectedResponse = "Which one do you want to choose? Nutrient history or food nutrient? Btw you can type \"leave\" to exit.";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		//test history
    		input = "history";
    		expectedResponse = "Please click to enter the starting date of your query.";
    		chatBotReponse = ((TextMessage)stateManager.chat(userId, input, false).get(0)).getText();
    		
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

	}

	@Test
	public void testInputImageLongString() throws Exception{
		// testUserInputImage
		ArrayList<String> ans = null;
		boolean thrown = false;
		String longString = "Image search is a traditional method to search for similar products based on"
				+ "input keywords. Search engines like Google and Bing enable keyword search to obtain"
				+ "similar products. Similarly, fashion search services like Shopstyle and Lyst focuses"
				+ "in searching for fashionable products";

		try{
			OCRStringPreprocessing obj = new OCRStringPreprocessing();
			ans = obj.processOcrRawString(longString);
			assertThat(ans.size()).isEqualTo(0);
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
	}

	@Test
	public void testChatImage() throws Exception{
		// testUserInputImage
		boolean thrown = false;

		final Path path1 = Paths.get("test-reply-image-1.jpg");
		final Path path2 = Paths.get("test-reply-image-2.jpg");
		DownloadedContent jpg1 = new DownloadedContent(path1, null);
		DownloadedContent jpg2 = new DownloadedContent(path2, null);

		Vector<String> response = null;
		String expectedResponse = null;

		final String reply1 = "There is no useful information in your image!";
		final String reply2a = "Thanks, I'm looking at your photo now! I'll try to give you some recommendations.";
		final String reply2b = "Sweet and Sour Park";

		try{
			expectedResponse = "Please finish giving us your personal information before sharing photos!";
			response = stateManager.chat("fakeUserId", jpg1, false);
			assertThat(response.get(0)).isEqualTo(expectedResponse);
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		
		try{
			expectedResponse = "Please finish updating your personal information before sharing me photos!";
			response = stateManager.chat("testUserChatImageUpdateUserInfo", jpg1, false);
			assertThat(response.get(0)).isEqualTo(expectedResponse);
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{
			expectedResponse = "Sorry, I am lost and I don't know how to respond. Please continue with your previous activity first.";
			response = stateManager.chat("testUserChatImagePostEating", jpg1, false);
			assertThat(response.get(0)).isEqualTo(expectedResponse);
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{
			expectedResponse = "There is no useful information in your image!";
			response = stateManager.chat("testUserChatImageInputMenu1", jpg1, false);
			assertThat(response.get(0)).isEqualTo(expectedResponse);
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);

		try{
			expectedResponse = "Thanks, I'm looking at your photo now! I'll try to give you some recommendations.";
			String expectedResponse2 = "Sweet and Sour Park";
			response = stateManager.chat("testUserChatImageInputMenu2", jpg2, false);
			// assertThat(response.get(0)).isEqualTo(expectedResponse);
			// assertThat(response.get(1)).isEqualTo(expectedResponse2);
			assertThat(expectedResponse.contains(response.get(0)));
			assertThat(expectedResponse2.contains(response.get(1)));
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
	}

	@Test
	public void testHandleFollowEvent() throws Exception {
		boolean thrown = false;
		try{
			String testUserId = "testHandleFollowEvent";
			String testReplyToken = "testReplyToken";
			Instant timestamp = Instant.now();
			Source testSource = new UserSource("testHandleFollowEvent");
			Event testFollowEvent = new FollowEvent(testReplyToken, testSource, timestamp);
			DietbotController.handleFollowEvent(((FollowEvent) testFollowEvent));
			assertThat(databaseEngine.searchUser(testSource.getUserId(), "campaign_user")).isEqualTo(true);

		} catch (Exception e){
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
	}


	@Test
	public void testCreateTempFile() throws Exception {
		boolean thrown = false;
		try{
			 DietbotController.DownloadedContent dl = DietbotController.createTempFile("");
		} catch (Exception e){
			thrown = true;
		}
		assertThat(thrown).isEqualTo(true);
	}

	@Test
	public void testSaveContent() throws Exception{
		boolean thrown = false;
		try{
			DietbotController.DownloadedContent dl = DietbotController.saveContent("jpg", null);
		} catch (Exception e){
			thrown = true;
		}
		assertThat(thrown).isEqualTo(true);

	}

	@Test
	public void testCreateUri() throws Exception{
		boolean thrown = false;
		try{
			String out = DietbotController.createUri("jpg");
		} catch (Exception e){
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
	}

	@Test
	public void testAdminUploadCoupon() throws Exception{

		boolean thrown = false;
		String input = null;
		String chatBotReponse = null;
		String expectedResponse = null;

		//example random userId from LINE
		String userId = "testCollectUserInformation";
		stateManager = new StateManager("src/test/resources/rivescriptChatbot");
		String adminUserId = "Udfd2991f287cc5c75f6c1d2c30c58a3a";
		ArrayList<String> allergies = new ArrayList<String>();
		allergies.add("seafood");
		databaseEngine.reset(adminUserId, "userinfo");
		databaseEngine.reset(adminUserId, "userallergies");

		databaseEngine.writeUserInfo(adminUserId, 20, "male", 1.75, 60, allergies, "normal", "standby", "standby");

		try{
    		//test admin upload image
    		input = "admin:upload_coupon";
    		expectedResponse = "Hi admin, please input a coupon image!";
    		chatBotReponse = ((TextMessage)stateManager.chat(adminUserId, input, true).get(0)).getText();
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

    		String url = "https://dieting-chatbot.herokuapp.com/downloaded/2017-11-20T07:33:48.762-49f1625f-82f7-4c67-91cf-bb87586273b9.jpg";
			DownloadedContent temp = new DownloadedContent(null, url);

    		expectedResponse = "Hi Admin, your image has been well received!";
    		chatBotReponse = stateManager.chat(adminUserId, temp, true).get(0);
    		assertThat(chatBotReponse).isEqualTo(expectedResponse);

		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		
	}
 }