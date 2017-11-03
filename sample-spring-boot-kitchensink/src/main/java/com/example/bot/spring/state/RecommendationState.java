package com.example.bot.spring;

//import java.io.IOException;
import com.rivescript.RiveScript;
import com.example.bot.spring.SQLDatabaseEngine;
import java.util.ArrayList;

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
	public String recommendation(ArrayList<String> foodList, int userID) {
		SQLDatabaseEngine db = new SQLDatabaseEngine();
		
		
		
		return "Food";
	}
}