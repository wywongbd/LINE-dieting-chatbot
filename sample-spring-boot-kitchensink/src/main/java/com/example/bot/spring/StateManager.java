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
    private final int[] FROM_STANDBY_STATE = {1, 2, 3, 5};

    // Value to keep track current state
    private static Map<String, Integer> currentState; 
    
    private State[] states = {
            new StandbyState(),
            new CollectUserInfoState(),
            new ProvideInfoState(),
            new InputMenuState(),
            new RecommendationState(),
            new PostEatingState()
        };
    
    private RiveScript bot;    
    
    /**
     * Default constructor for StateManager
     */
    public StateManager(String path) {

            // Rivescript objectg
            bot = new RiveScript();
            
            currentState = new HashMap<String, Integer>();

            // Load rive files for Rivescript object
            File resourcesDirectory = new File(path);
            bot.loadDirectory(resourcesDirectory.getAbsolutePath());
            bot.sortReplies();
    }

    /**
     * Get output message after inputting text
     * @param text A String data type
     * @return A String data type
     */
    public Vector<String> chat(String userId, String text, boolean debug) throws Exception {
    	Vector<String> replyText = new Vector<String>(0);

        try{
            // Get the next state after current message
            if (currentState.containsKey(userId) == false) {
                currentState.put(userId, 1);
                bot.setUservar(userId, "state", "collect_user_info");
            }
            
        	replyText.add(states[currentState.get(userId)].reply(userId, text, bot));
            currentState.put(userId, decodeState(bot.getUservar(userId, "state"))); 
            
            if(currentState.get(userId) == RECOMMEND_STATE) {            	
            	String[] splitString = (replyText.lastElement()).split("AAAAAAAAAA");       	            	          	
            	replyText.add(0, splitString[0]);         	          	
            	replyText.remove(replyText.size() - 1);
         
            	String temp = states[currentState.get(userId)].reply(userId, splitString[1], bot);           	
            	replyText.add(temp);
            }
            currentState.put(userId, decodeState(bot.getUservar(userId, "state")));
            
        } catch (Exception e) {    // Modify to custom exception TextNotRecognized later
            // Text is not recognized, does not modify current state
        	replyText.clear();
            replyText.add("Your text is not recognized by us!");
        }
        
        if(replyText.size() > 0) {
            // Just for testing
        	if(debug == true) {
        		replyText.add("Current state is " +  Integer.toString(currentState.get(userId)));
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
    	
        try{
            if (currentState.containsKey(userId) == false || currentState.get(userId) == 1) {
            	replyText.add("Please finish giving us your personal information before sending photos!");
                return replyText;
            }
            // Pass the image into InputMenuState to check if the image is recognized as menu
            replyText.add(((InputMenuState) states[INPUT_MENU_STATE]).replyImage(userId, jpg, bot));
            currentState.put(userId, decodeState(bot.getUservar(userId, "state")));
            
            if(currentState.get(userId) == RECOMMEND_STATE) {            	
            	String[] splitString = (replyText.lastElement()).split("AAAAAAAAAA");       	            	          	
            	replyText.add(0, splitString[0]);         	          	
            	replyText.remove(replyText.size() - 1);
         
            	String temp = states[currentState.get(userId)].reply(userId, splitString[1], bot);           	
            	replyText.add(temp);
            }
            currentState.put(userId, decodeState(bot.getUservar(userId, "state")));

        } catch (Exception e) {    // Modify to custom exception ImageNotRecognized later
            // Image is not recognized as menu, does not modify current state
        	replyText.clear();
            replyText.add("Your img is not recognized by us!");
        }
        if(replyText.size() > 0) {
            // Just for testing
        	if(debug == true) {
        		replyText.add("Current state is " +  Integer.toString(currentState.get(userId)));
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