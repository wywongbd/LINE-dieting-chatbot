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


@RunWith(SpringRunner.class)
//@SpringBootTest(classes = { DietbotTester.class, DatabaseEngine.class })
@SpringBootTest(classes = { DietbotTester.class, SQLDatabaseEngine.class })
public class DietbotTester {
	@Autowired
	private SQLDatabaseEngine databaseEngine;
	private RiveScript bot;
	

	@Test
	public void writeUserInfoNonExisting() throws Exception {
		boolean thrown = false;
		String[] allergies = {"peanuts", "shrimp"};
		try {
			this.databaseEngine.writeUserInfo("testUser", 20, "male", 1.75, 60, allergies);
		} catch (Exception e) {
			thrown = true;
		} finally {
			this.databaseEngine.deleteUserInfo("testUser");
		}
		assertThat(thrown).isEqualTo(false);
	}
	
	@Test
	public void writeUserInfoExisting() throws Exception {
		boolean thrown = false;
		String[] allergies = {"water"};
		try {
			this.databaseEngine.writeUserInfo("testUserExisting", 21, "female", 1.64, 55, allergies);
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
	}
	
	@Test
	public void testAdd() throws Exception {
		boolean thrown = false;
		String resultMenu = null;
		String resultRecommendations = null;
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("frozen water");
		menu.add("molten ice");
		try {
			this.databaseEngine.addMenu("testUser", menu);
			this.databaseEngine.addRecommendations("testUser");
			resultMenu = this.databaseEngine.searchMenu("frozen");
			resultRecommendations = this.databaseEngine.searchRecommendations("frozen");
			this.databaseEngine.resetMenu("testUser");
			this.databaseEngine.resetRecommendations("testUser");
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		assertThat(resultMenu.contains("frozen"));
		assertThat(resultRecommendations.contains("frozen"));
	}
	
	@Test
	public void searchMenuNotFound() throws Exception {
		boolean thrown = false;
		String result = null;
		try {
			result = this.databaseEngine.searchMenu("asdf");
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(true);
	}

	@Test
	public void searchRecommendationsNotFound() throws Exception {
		boolean thrown = false;
		String result = "";
		try {
			result = this.databaseEngine.searchRecommendations("asdf");
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(true);
	}
	
	@Test
	public void testRemoveAllergies() throws Exception {
		boolean thrown = false;
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("grilled salmon");
		try {
			this.databaseEngine.addMenu("testUser", menu);
			this.databaseEngine.addRecommendations("testUser");
			this.databaseEngine.processRecommendationsByAllergies("testUser");
			this.databaseEngine.searchRecommendations("salmon");
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(true);
	}
	
	@Test
	public void testUpdateRecommendationsByIntake() throws Exception {
		boolean thrown = false;
		try {
			this.databaseEngine.processRecommendationsByIntake("testUser");
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
	}
	
	@Test
	public void getRecommendations() throws Exception {
		HashMap<String, Double> result = new HashMap<String, Double>();
		ArrayList<String> menu = new ArrayList<String>();
		menu.add("frozen water");
		menu.add("molten ice");
		boolean thrown = false;
		try {
			this.databaseEngine.addMenu("testUser", menu);
			this.databaseEngine.addRecommendations("testUser");
			result = this.databaseEngine.getRecommendations("testUser");
			this.databaseEngine.resetMenu("testUser");
			this.databaseEngine.resetRecommendations("testUser");
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
		assertThat(result.get("frozen water")).isEqualTo(1.0);
	}
	
	@Test
	public void recommend() throws Exception {
		boolean thrown = false;
		String result = null;
		String resultString = "grilled salmon vege fish chip mayo rice fried chicken fish";
		RecommendationState recommend = new RecommendationState();
		ArrayList<String> foodList = new ArrayList<String>();
		foodList.add("grilled pork vege");
		foodList.add("fish chip mayo");
		foodList.add("rice fried chicken");

		try {
			result = recommend.recommendFood("testUser", foodList);
		} catch (Exception e) {
			thrown = true;
		}
		assertThat(thrown).isEqualTo(false);
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
	public void testURLtoJSON() throws Exception{
		
		boolean thrown = false;
		String output = null;
		final String realOutput = "{\"Network\":[{\"2G bands\":\"GSM 900 / 1800 - SIM 1 & SIM 2\",\"Technology\":\"GSM\",\"GPRS\":\"Class 12\",\"EDGE\":\"Yes\"}]}";
		try{
			final String HTML = "<table cellspacing=\"0\" style=\"height: 24px;\">\r\n<tr class=\"tr-hover\">\r\n<th rowspan=\"15\" scope=\"row\">Network</th>\r\n<td class=\"ttl\"><a href=\"network-bands.php3\">Technology</a></td>\r\n<td class=\"nfo\"><a href=\"#\" class=\"link-network-detail collapse\">GSM</a></td>\r\n</tr>\r\n<tr class=\"tr-toggle\">\r\n<td class=\"ttl\"><a href=\"network-bands.php3\">2G bands</a></td>\r\n<td class=\"nfo\">GSM 900 / 1800 - SIM 1 & SIM 2</td>\r\n</tr>   \r\n<tr class=\"tr-toggle\">\r\n<td class=\"ttl\"><a href=\"glossary.php3?term=gprs\">GPRS</a></td>\r\n<td class=\"nfo\">Class 12</td>\r\n</tr>   \r\n<tr class=\"tr-toggle\">\r\n<td class=\"ttl\"><a href=\"glossary.php3?term=edge\">EDGE</a></td>\r\n<td class=\"nfo\">Yes</td>\r\n</tr>\r\n</table>";
			Document document = Jsoup.parse(HTML);
			Element table = document.select("table").first();
			String arrayName = table.select("th").first().text();
			JSONObject jsonObj = new JSONObject();
			JSONArray jsonArr = new JSONArray();
			Elements ttls = table.getElementsByClass("ttl");
			Elements nfos = table.getElementsByClass("nfo");
			JSONObject jo = new JSONObject();
			for (int i = 0, l = ttls.size(); i < l; i++) {
			    String key = ttls.get(i).text();
			    String value = nfos.get(i).text();
			    jo.put(key, value);
			}
			jsonArr.put(jo);
			jsonObj.put(arrayName, jsonArr);
			output = jsonObj.toString();
			
    
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
}
 	
