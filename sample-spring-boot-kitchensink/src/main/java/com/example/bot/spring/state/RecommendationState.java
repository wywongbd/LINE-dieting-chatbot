package com.example.bot.spring;

//import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import com.rivescript.RiveScript;
import com.example.bot.spring.SQLDatabaseEngine;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class RecommendationState extends State {
    /**
     * Default constructor for RecommendationState
     */
	public RecommendationState() {
		
	}

    /**
     * Reply a message for input text
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		int currentState = decodeState(bot.getUservar(userId, "state")); 
		String output = bot.reply(userId, text);
		int afterState = decodeState(bot.getUservar(userId, "state"));
		
		
		return output;
	}
	
	/**
     * Recommended a food after inputting a list of food
     * @param FoodList A ArrayList<String> data type
     * @param userID A int data type
     * @return A String data type
     */
	public String recommendation(String userId, ArrayList<String> foodList) {
		SQLDatabaseEngine sql = new SQLDatabaseEngine();
		HashMap<String, Double> foodWeightage = null;
		
		try {
			sql.addMenu(userId, foodList);
			sql.addRecommendations(userId);
			sql.processRecommendationsByAllergies(userId);
			sql.processRecommendationsByIntake(userId);
			foodWeightage = sql.getRecommendations(userId);
		} catch (Exception e) {
			log.info("Exception while removing recommendations from recommendations table: {}", e.toString());
			return "I am not sure what to recommend you";
		}

		
		return "Food";
	}
}