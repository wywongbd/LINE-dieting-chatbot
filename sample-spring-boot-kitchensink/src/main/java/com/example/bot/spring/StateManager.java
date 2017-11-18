/**
* StateManager.java - A class for managing different states and transitions
*/

package com.example.bot.spring;

import com.rivescript.RiveScript;
import java.io.File;
import java.util.*;

import com.example.bot.spring.DietbotController.DownloadedContent;

public class StateManager {
    // Constant values
    private final int STANDBY_STATE = 0;
    private final int INPUT_MENU_STATE = 3;
    private final int RECOMMEND_STATE = 4;
    // Must first go through InputMenuState before going to RecommendationState,
    // so 4 is not included
//    private final int[] FROM_STANDBY_STATE = {1, 2, 3, 5};

    // Value to keep track current state
    // private static Map<String, Integer> currentState; 
    
    private static final Map<String, State> states; 
    private static RiveScript bot;    

    static
    {
        bot = new RiveScript();
        
        states = new HashMap<String, State>();
        states.put("standby", new StandbyState());
        states.put("collect_user_info", new CollectUserInfoState());    
        states.put("recommend", new RecommendationState());
        states.put("input_menu", new InputMenuState());    
        states.put("provide_info", new ProvideInfoState());  
        states.put("post_eating", new PostEatingState());
        states.put("update_user_info", new UpdateUserInfoState());
    };
    
    /**
     * Default constructor for StateManager
     */
    public StateManager(String path) {
        // Load rive files for Rivescript object
        File resourcesDirectory = new File(path);
        bot.loadDirectory(resourcesDirectory.getAbsolutePath());
        bot.sortReplies();
    }

    public void updateBot(String userId){
        SQLDatabaseEngine sql = new SQLDatabaseEngine();

        System.out.println("updating bot, current state is " + sql.getUserInfo(userId, "state"));
        System.out.println("updating bot, current topic is " + sql.getUserInfo(userId, "topic"));
        bot.setUservar(userId, "topic", sql.getUserInfo(userId, "topic"));
        bot.setUservar(userId, "state", sql.getUserInfo(userId, "state"));
    }

    /**
     * Get output message after inputting text
     * @param text A String data type
     * @return A String data type
     */
    public Vector<String> chat(String userId, String text, boolean debug) throws Exception {
    	Vector<String> replyText = new Vector<String>(0);
        SQLDatabaseEngine sql = new SQLDatabaseEngine();
        String currentState = null;        
        String currentTopic = null;
        boolean isRegisteredUser = true;
        isRegisteredUser = sql.searchUser(userId);

        System.out.println("point 1");
        System.out.println(isRegisteredUser == false);

        if (!isRegisteredUser) {
            currentState = "collect_user_info";
            bot.setUservar(userId, "state", "collect_user_info");
        }
        else{
            // update bot status
            updateBot(userId);
            currentState = bot.getUservar(userId, "topic");
            currentTopic = bot.getUservar(userId, "state");
        }

        System.out.println("point 2");
        
    	replyText.add(states.get(currentState).reply(userId, text, bot));

        System.out.println("point 2.5");

        currentState = bot.getUservar(userId, "state");
        
        System.out.println("point 3");

        if(currentState == "recommend") {            	
        	String[] splitString = (replyText.lastElement()).split("AAAAAAAAAA");       	            	          	
        	replyText.add(0, splitString[0]);         	          	
        	replyText.remove(replyText.size() - 1);
        
        	String temp = states.get(currentState).reply(userId, splitString[1], bot);           	
        	replyText.add(temp);
        }
        
        System.out.println("point 4");
        
        if(replyText.size() > 0) {
            // Just for testing
        	if(debug == true) {
        		replyText.add("Current state is " + bot.getUservar(userId, "state"));
                replyText.add("Current topic is " + bot.getUservar(userId, "topic"));
        	}
        	return replyText;
        }
        throw new Exception("NOT FOUND");
    }

    /**
     * Get output message after inputting image
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
    public Vector<String> chat(String userId, DownloadedContent jpg, boolean debug) throws Exception {
    	Vector<String> replyText = new Vector<String>(0);
        SQLDatabaseEngine sql = new SQLDatabaseEngine();
    	String currentState = null;        
        String currentTopic = null;
        boolean isRegisteredUser = true;
        isRegisteredUser = sql.searchUser(userId);

        System.out.println("Image chat point 1");

        if (!isRegisteredUser) {
            replyText.add("Please finish giving us your personal information before sharing photos!");
            return replyText;
        }
        else{
        	updateBot(userId);
            currentState = bot.getUservar(userId, "topic");
            currentTopic = bot.getUservar(userId, "state");
            
            if (currentState == "update_user_info"){
                replyText.add("Please finish updating your personal information before sharing photos!");
                return replyText;
            }
        }

        System.out.println("Image chat point 2");

        // Pass the image into InputMenuState to check if the image is recognized as menu
        replyText.add(((InputMenuState) states.get(currentState)).replyImage(userId, jpg, bot));
        currentState = bot.getUservar(userId, "state");

        System.out.println("Image chat point 3");
        
        if(currentState == "recommend") {               
            String[] splitString = (replyText.lastElement()).split("AAAAAAAAAA");                                       
            replyText.add(0, splitString[0]);                       
            replyText.remove(replyText.size() - 1);
        
            String temp = states.get(currentState).reply(userId, splitString[1], bot);              
            replyText.add(temp);
        }

        System.out.println("Image chat point 4");

        if(replyText.size() > 0) {
            // Just for testing
        	if(debug == true) {
        		replyText.add("Current state is " +  bot.getUservar(userId, "state"));
                replyText.add("Current topic is " +  bot.getUservar(userId, "topic"));
        	}
        	return replyText;
        }
        throw new Exception("NOT FOUND");
    }
    
    /**
     * Get the next state after inputting text
     * @param text A String data type
     * @return A int data type
     */
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