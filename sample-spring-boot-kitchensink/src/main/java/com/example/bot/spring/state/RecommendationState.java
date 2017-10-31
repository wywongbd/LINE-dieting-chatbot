package com.example.bot.spring;

//import java.io.IOException;

public class RecommendationState extends State {
    /**
     * Default constructor for RecommendationState
     */
	public RecommendationState() {
		
	}

    /**
     * Check if input message will trigger this state
     * Inherited from abstract base class
     * @param text A String data type
     * @return A bool data type
     */
	public boolean checkTrigger(String text) {
		// Just for testing
		if(text.equals("RecommendationState")) {
			return true;
		}
		else {
			return false;
		}
	}

    /**
     * Reply a message for input text
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String text) {
		return "Your text has been well received!"; 
	}
}