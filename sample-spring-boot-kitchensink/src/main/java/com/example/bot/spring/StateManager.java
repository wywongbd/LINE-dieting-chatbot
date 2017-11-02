/**
* StateManager.java - A class for managing different states and transitions
*/

package com.example.bot.spring;

import com.rivescript.RiveScript;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    public StateManager() {

            // Rivescript objectg
            bot = new RiveScript();
            
            currentState = new HashMap<String, Integer>();

            // Load rive files for Rivescript object
            File resourcesDirectory = new File("sample-spring-boot-kitchensink/src/main/resources/rivescript");
            bot.loadDirectory(resourcesDirectory.getAbsolutePath());
            bot.sortReplies();
    }

    /**
     * Get output message after inputting text
     * @param text A String data type
     * @return A String data type
     */
    public String chat(String userId, String text) throws Exception {
        String replyText = null;
        try{
            // Get the next state after current message
            if (currentState.containsKey(userId) == false) {
                currentState.put(userId, 1);
            }
            
            if(text.matches(InputMenuState.URL_PATTERN_REGEX)){
                // The text message is URL
                replyText = ((InputMenuState) states[INPUT_MENU_STATE]).replyUrl(text);
                currentState.put(userId, INPUT_MENU_STATE);
            }
            else {
                replyText = bot.reply(userId, text);
                currentState.put(userId, decodeState(bot.getUservar(userId, "state")));    
            }
            
        } catch (Exception e) {    // Modify to custom exception TextNotRecognized later
            // Text is not recognized, does not modify current state
            replyText = "Your text is not recognized by us!";
        }
        
        if(replyText != null) {
            // Just for testing
            return replyText + " Current state is " +  Integer.toString(currentState.get(userId));
        }
        throw new Exception("NOT FOUND");
    }

    /**
     * Get output message after inputting image
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
    public String chat(String userId, DownloadedContent jpg) throws Exception {
        String replyText = null;
        try{
                if (currentState.containsKey(userId) == false) {
                    currentState.put(userId, INPUT_MENU_STATE);
                }
            // Pass the image into InputMenuState to check if the image is recognized as menu
            replyText = ((InputMenuState) states[INPUT_MENU_STATE]).replyImage(jpg);
            // If above line does not return exception, then the image is recognized as menu
            currentState.put(userId, INPUT_MENU_STATE);  
        } catch (Exception e) {    // Modify to custom exception ImageNotRecognized later
            // Image is not recognized as menu, does not modify current state
            replyText = "Your image is not recognized by us!";
        }
        if(replyText != null) {
            // Just for testing
            return replyText + " Current state is " +  Integer.toString(currentState.get(userId));
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