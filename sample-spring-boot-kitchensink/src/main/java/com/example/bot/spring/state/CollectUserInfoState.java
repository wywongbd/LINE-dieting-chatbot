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
			System.out.println("Writing to DB.... 1");
			
			SQLDatabaseEngine sql = null;
			int age = null;
			Double weight = null;
			Double height = null;
			String gender = null;
			String[] allergyFood = null;
			Vector<String> allergies = null;
			
			try {
				sql = new SQLDatabaseEngine();
				age = Integer.parseInt(bot.getUservar(userId, "age"));
				weight = Double.parseDouble(bot.getUservar(userId, "weight"));
			    height = Double.parseDouble(bot.getUservar(userId, "height"));
				gender = bot.getUservar(userId, "gender");
				allergyFood = {"milk", "eggs", "nut", "seafood"};
				allergies = new Vector<String>(0);
			}
			catch(Exception e) {
				System.out.println("failed");
			}
			
			System.out.println("Writing to DB.... 2");
			
			for (String food: allergyFood) {
				if (bot.getUservar(userId, food + "_allergy").equals("true")) {
					allergies.add(food);
					
				}
			}
			
			String[] temp = allergies.toArray(new String[allergies.size()]);
			
			for (String food: temp) {
				System.out.println(food);
			}
			
			try {
				sql.writeUserInfo(userId, age, gender, height, weight, temp);
			}
			catch(Exception e) {
				System.out.println("Exception while inserting user info into user database: " + e.toString());
			}
		}
		return output;
	}
}