package com.example.bot.spring;

//import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import com.rivescript.RiveScript;
import com.example.bot.spring.SQLDatabaseEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RecommendationState extends State {
	private static final String DEFAULT_RECOMMENDATION = "I am not sure what to recommend you";
	
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
	public String recommendFood(String userId, ArrayList<String> foodList) {
		SQLDatabaseEngine sql = new SQLDatabaseEngine();
		HashMap<String, Double> foodWeightage = null;
		String recommendation = null;

		try {
			sql.addMenu(userId, foodList);
			sql.addRecommendations(userId);
			sql.processRecommendationsByAllergies(userId);
			sql.processRecommendationsByIntake(userId);
			foodWeightage = sql.getRecommendations(userId);
			sql.resetMenu(userId);
			sql.resetRecommendations(userId);

			// Compute the total weight of all items together
			Double totalWeight = 0.0d;
			for (Double weight : foodWeightage.values()){
			    totalWeight += weight;
			}

			// Now choose a random item
			int randomIndex = -1;
			double random = Math.random() * totalWeight;
			for (Map.Entry<String, Double> entry : foodWeightage.entrySet()) {
			    String food = entry.getKey();
			    Double weightage = entry.getValue();
			    
			    random -= weightage;
			    if (random <= 0.0d){
			    		recommendation = food;
			        break;
			    }
			}

		} catch (Exception e) {
			log.info("Exception while removing recommendations from recommendations table: {}", e.toString());
			return DEFAULT_RECOMMENDATION;
		} 

		if(recommendation != null) {
			return recommendation;
		}
		else {
			return DEFAULT_RECOMMENDATION;
		}
	}
}