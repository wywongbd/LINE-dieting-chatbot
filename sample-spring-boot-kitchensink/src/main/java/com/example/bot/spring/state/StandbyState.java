package com.example.bot.spring;

//import java.io.IOException;
import com.rivescript.RiveScript;

public class StandbyState extends State {
    /**
     * Default constructor for StandbyState
     */
	public StandbyState() {
		
	}

    /**
     * Reply a message for input text
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		int currentState = bot.getUservar(userId, "state"); 
		String output = bot.reply(userId, text);
		int afterState = bot.getUservar(userId, "state");
		
		updateDatabase(userId, bot);
		return output;
	}
}