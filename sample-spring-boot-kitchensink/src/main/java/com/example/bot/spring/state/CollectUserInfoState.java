package com.example.bot.spring;

//import java.io.IOException;
import java.util.*;
import com.rivescript.RiveScript;

public class CollectUserInfoState extends State {
    /**
     * Default constructor for CollectUserInfoState
     */
	public CollectUserInfoState() {
		
	}

    /**
     * Synchronize between SQL and Rivescript
     * @param userId String data type 
     * @param bot RiveScript data type 
     */
    public void syncSQLWithRiveScript(String userId, RiveScript bot){
    	return;
    }

    /**
     * Reply a message for input text in this state
     * @param userId String data type
     * @param text String data type
     * @param bot RiveScript data type 
     * @return String data type as the reply
     */
	public String reply(String userId, String text, RiveScript bot) {
		System.out.println("Start: CollectUserInfoState");

		String currentState = bot.getUservar(userId, "state"); 
		String output = bot.reply(userId, text);
		String afterState = bot.getUservar(userId, "state");
				
		if (!currentState.equals(afterState)) {
			// write to DB
			int age = Integer.parseInt(bot.getUservar(userId, "age"));
			Double weight = Double.parseDouble(bot.getUservar(userId, "weight"));
			Double height = Double.parseDouble(bot.getUservar(userId, "height"));
			String gender = bot.getUservar(userId, "gender");
			String[] allergyFood = {"milk", "egg", "nut", "seafood"};
			ArrayList<String> allergies = new ArrayList<String>();
			String state = bot.getUservar(userId, "state");
			String topic = bot.getUservar(userId, "topic");
			String diet = bot.getUservar(userId, "diet");
			
			
			for (String food: allergyFood) {
				if (bot.getUservar(userId, food + "_allergy").equals("true")) {
					allergies.add(food);				
				}
			}
			
			sql.writeUserInfo(userId, age, gender, height, weight, allergies, diet, topic, state);
		}
               
		syncSQLWithRiveScript(userId, bot);
		return output;
	}


}