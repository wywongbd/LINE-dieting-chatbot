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
     * Reply a message for input text
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		int currentState = decodeState(bot.getUservar(userId, "state")); 
		String output = bot.reply(userId, text);
		int afterState = decodeState(bot.getUservar(userId, "state"));
		
		if (currentState != afterState) {
			// write to DB
			System.out.println("Writing to DB....");
			SQLDatabaseEngine sql = new SQLDatabaseEngine();
			int age = Integer.parseInt(bot.getUservar(userId, "age"));
			Double weight = Double.parseDouble(bot.getUservar(userId, "weight"));
			Double height = Double.parseDouble(bot.getUservar(userId, "height"));
			String gender = bot.getUservar(userId, "gender");
			String[] allergyFood = {"milk", "eggs", "nut", "seafood"};
			Vector<String> allergies = new Vector<String>(0);
			
			System.out.println("Writing to DB....");
			
			for (String food: allergyFood) {
				if (bot.getUservar(userId, food + "_allergy").equals("true")) {
					allergies.add(food);
				}
			}
			
			try {
				sql.writeUserInfo(userId, age, gender, height, weight, allergies.toArray(new String[allergies.size()]));
			}
			catch(Exception e) {
				System.out.println("Exception while inserting user info into user database: " + e.toString());
			}
		}
		System.out.println("Writing to DB....");
		return output;
	}
}