package com.example.bot.spring;

//import java.io.IOException;
import com.rivescript.RiveScript;

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
}