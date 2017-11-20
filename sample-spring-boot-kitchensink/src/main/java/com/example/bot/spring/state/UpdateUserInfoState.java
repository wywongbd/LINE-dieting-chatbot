package com.example.bot.spring;

//import java.io.IOException;
import java.util.*;
import com.rivescript.RiveScript;

public class UpdateUserInfoState extends State {
    /**
     * Default constructor for UpdateUserInfoState
     */
	public UpdateUserInfoState(){

	}

    /**
     * Reply a message for input text in this state
     * @param userId String data type
     * @param text String data type
     * @param bot RiveScript data type 
     * @return String data type as the reply
     */
	public String reply(String userId, String text, RiveScript bot) {
		String output = bot.reply(userId, text);
		syncSQLWithRiveScript(userId, bot);
		return output;
	}
}