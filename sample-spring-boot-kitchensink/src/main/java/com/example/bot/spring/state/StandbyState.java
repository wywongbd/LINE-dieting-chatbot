package com.example.bot.spring;

import java.util.*;

//import java.io.IOException;
import com.rivescript.RiveScript;

public class StandbyState extends State {
    /**
     * Default constructor for StandbyState
     */
	public StandbyState() {
		
	}

    /**
     * Reply a message for input text in this state
     * @param userId String data type
     * @param text String data type
     * @param bot RiveScript data type 
     * @return String data type as the reply
     */
	public String reply(String userId, String text, RiveScript bot) {

		String output = null;
		
		if(matchCheatDayTrigger(userId, text)){
			if(sql.canClaimCheatDay(userId)){
				sql.addUserEatingHistory(userId, "cheat day");
				output = "Congratulations! You have done well to stick to your diet plan for more than a week, you can now enjoy your cheat day! Feel free to eat whatever you wish for the rest of the day.";
			}
			else{
				output = "Sorry, you can only claim you cheat day once a week. Hang on for a little longer!";
			}
		}
		else{
			output = bot.reply(userId, text);
		}

		bot.setUservar(userId, "img_received", "false");
		syncSQLWithRiveScript(userId, bot);
		return output;
	}

    /**
     * Match trigger for cheat day
     * @param userId String data type
     * @param text String data type
     * @return boolean data type
     */
	private boolean matchCheatDayTrigger(String userId, String text){
		text = text.toLowerCase();
		if(text.contains("cheat day") || text.contains("cheatday")){
			return true;
		}
		else{
			return false;
		}
	}
}