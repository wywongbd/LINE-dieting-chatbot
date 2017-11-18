/**
* StateManager.java - A class for managing different states and transitions
*/

package com.example.bot.spring;

import com.rivescript.RiveScript;
import com.rivescript.macro.Subroutine;
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
    
    public static final Map<String, State> states; 
    private static RiveScript bot;    
    private static boolean adminAccessing;

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
        states.put("admin", new AdminState());

        adminAccessing = false;
    };
    
    /**
     * Default constructor for StateManager
     */
    public StateManager(String path) {
        // Load rive files for Rivescript object
        File resourcesDirectory = new File(path);
        bot.loadDirectory(resourcesDirectory.getAbsolutePath());
        bot.sortReplies();
        bot.setSubroutine("setVariableToDB", new setVariableToDB());
    }

    public void updateBot(String userId){
        SQLDatabaseEngine sql = new SQLDatabaseEngine();
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
        isRegisteredUser = sql.searchUser(userId, "userinfo");

        if (!isRegisteredUser) {
            currentState = "collect_user_info";
            bot.setUservar(userId, "state", "collect_user_info");
        }
        else{
            updateBot(userId);
            currentState = bot.getUservar(userId, "state");
            currentTopic = bot.getUservar(userId, "topic");
            bot.setUservar(userId, "met", "true");
        }

        if(currentState.equals("standby") && (((AdminState) states.get("admin")).matchTrigger(text) == 1)){
            // currentState = "admin";
            adminAccessing = true;
        }
        if(adminAccessing == true){
            replyText.add(states.get("admin").reply(userId, text, bot));
        }
        else{
            replyText.add(states.get(currentState).reply(userId, text, bot));
        }

        currentState = bot.getUservar(userId, "state");

        if(currentState.equals("recommend")) {            	
        	String[] splitString = (replyText.lastElement()).split("AAAAAAAAAA");       	            	          	
        	replyText.add(0, splitString[0]);         	          	
        	replyText.remove(replyText.size() - 1);
        
        	String temp = states.get(currentState).reply(userId, splitString[1], bot);           	
        	replyText.add(temp);
        }

        if(replyText.size() > 0) {
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
        isRegisteredUser = sql.searchUser(userId, "userinfo");

        if (!isRegisteredUser) {
            replyText.add("Please finish giving us your personal information before sharing photos!");
            return replyText;
        }
        else{
        	updateBot(userId);
            currentState = bot.getUservar(userId, "state");
            currentTopic = bot.getUservar(userId, "topic");
            
            if (currentState.equals("update_user_info")){
                replyText.add("Please finish updating your personal information before sharing photos!");
                return replyText;
            }
        }

        if (currentState.equals("input_menu") || currentState.equals("standby")){
            if (adminAccessing == false) {
                replyText.add(((InputMenuState) states.get("input_menu")).replyImage(userId, jpg, bot));
            }
            else{
                replyText.add(((AdminState) states.get("admin")).replyImage(userId, jpg, bot));
                adminAccessing = false;
            }
        }

        currentState = bot.getUservar(userId, "state");
        
        if(currentState.equals("recommend")) {               
            String[] splitString = (replyText.lastElement()).split("AAAAAAAAAA");                                       
            replyText.add(0, splitString[0]);                       
            replyText.remove(replyText.size() - 1);
        
            String temp = states.get(currentState).reply(userId, splitString[1], bot);              
            replyText.add(temp);
        }

        if(replyText.size() > 0) {
        	if(debug == true) {
        		replyText.add("Current state is " +  bot.getUservar(userId, "state"));
                replyText.add("Current topic is " +  bot.getUservar(userId, "topic"));
        	}
        	return replyText;
        }
        throw new Exception("NOT FOUND");
    }
    

    //use for save user info to database inside Rivescript
    public class setVariableToDB implements Subroutine {

        // assume the order of parameter is: variable name, value1, ... , userID
        public String call(RiveScript rs, String[] args) {

            SQLDatabaseEngine sql = new SQLDatabaseEngine();

            if ( args[0].equals("weight") || args[0].equals("height") ) {

                // double
                if ( args.length == 3 ) {
                    sql.setUserInfo(args[2], args[0], Double.parseDouble(args[1]));
                }

            } else if ( args[0].equals("age") ) {

                // integer
                if ( args.length == 3 ) {
                    sql.setUserInfo(args[2], args[0], Integer.parseInt(args[1]));
                }

            } else if ( args[0].equals("allergies") ) {

                if (args.length == 6) {

                    // leave to be implemented later
                    ArrayList<String> temp = new ArrayList<String>();
                    if ( args[1].equals("true") ) {
                        temp.add("milk");
                    }
                    if ( args[2].equals("true") ) {
                        temp.add("egg");
                    }
                    if ( args[3].equals("true") ) {
                        temp.add("nut");
                    }
                    if ( args[4].equals("true") ) {
                        temp.add("seafood");
                    }
                    sql.setUserAllergies(args[5], temp);
                }

            } else {

                // string
                if (args.length == 3) {
                    sql.setUserInfo(args[2], args[0], args[1]);
                }
            }

            return "";
        }
    }
}