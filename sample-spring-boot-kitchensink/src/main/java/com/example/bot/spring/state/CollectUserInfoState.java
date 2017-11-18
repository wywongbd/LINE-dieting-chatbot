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
    public void updateDatabase(String userId, RiveScript bot){
    	return;
    }

	public String reply(String userId, String text, RiveScript bot) {
		System.out.println("CollectUserInfoState point 1");

		String currentState = bot.getUservar(userId, "state"); 
		String output = bot.reply(userId, text);
		String afterState = bot.getUservar(userId, "state");

		System.out.println("CollectUserInfoState point 2");

		System.out.println(currentState);
		System.out.println(afterState);
		
				
		if (currentState.equals(afterState) == false) {
			System.out.println("Adding to DB");
			// write to DB
			SQLDatabaseEngine sql = new SQLDatabaseEngine();
			int age = Integer.parseInt(bot.getUservar(userId, "age"));
			Double weight = Double.parseDouble(bot.getUservar(userId, "weight"));
			Double height = Double.parseDouble(bot.getUservar(userId, "height"));
			String gender = bot.getUservar(userId, "gender");
			String[] allergyFood = {"milk", "egg", "nut", "seafood"};
<<<<<<< HEAD
			Vector<String> allergies = new Vector<String>(0);
			String state = bot.getUservar(userId, "state"); 
			String topic = bot.getUservar(userId, "topic");

			System.out.println("Adding to DB: state is " + state);
			System.out.println("Adding to DB: topic is " + topic);
=======
			ArrayList<String> allergies = new ArrayList<String>();
			
>>>>>>> a5f06f72e2cea05be5220870c2c48494206535fd
			
			for (String food: allergyFood) {
				if (bot.getUservar(userId, food + "_allergy").equals("true")) {
					allergies.add(food);				
				}
			}
			
			try {
<<<<<<< HEAD
				sql.writeUserInfo(userId, age, gender, height, weight, temp, 3, topic, state);
=======
				sql.writeUserInfo(userId, age, gender, height, weight, allergies, 3, "testTopic", "testState");
>>>>>>> a5f06f72e2cea05be5220870c2c48494206535fd
			}
			catch(Exception e) {
				System.out.println("Exception while inserting user info into user database: " + e.toString());
			}
		}
		System.out.println("CollectUserInfoState point 3");
		return output;
	}
}