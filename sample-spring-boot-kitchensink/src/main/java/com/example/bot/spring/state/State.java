/**
* StateManager.java - A abstract class for implementing different states
* To do: Add implementation for singleton
*/

package com.example.bot.spring;
import com.rivescript.RiveScript;

//import java.io.IOException;

abstract class State {
    /**
     * Reply a message for input text
     * @param text A String data type
     * @return A String data type
     */
    public void updateDatabase(String userId, RiveScript bot){
    	try {
    		SQLDatabaseEngine sql = new SQLDatabaseEngine();
    		sql.setUserInfo(userId, "state", bot.getUservar(userId, "state"));
    		sql.setUserInfo(userId, "topic", bot.getUservar(userId, "topic"));
    	}
    	catch (Exception e) {
    		System.out.println("Database error");
    	}
    }

    public abstract String reply(String userId, String text, RiveScript bot);
	
	public int decodeState(String text) {
        switch(text) {
            case "standby":
                return 0;
            case "collect_user_info":
                return 1;
            case "input_menu":
                return 3;
            case "post_eating":
                return 5;
            case "provide_info":
                return 2;
            default:
                return 4;
        }
    }
}