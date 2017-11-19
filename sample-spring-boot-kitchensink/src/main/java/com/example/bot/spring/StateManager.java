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
    private final String ADMIN_USER_ID = "Ub6f064e9c47d12622346a14556305165";
    // Must first go through InputMenuState before going to RecommendationState,
    // so 4 is not included
//    private final int[] FROM_STANDBY_STATE = {1, 2, 3, 5};

    // Value to keep track current state
    // private static Map<String, Integer> currentState; 
    
    public static final Map<String, State> states; 
    private static RiveScript bot;    
    private static boolean adminAccessing;
    private static SQLDatabaseEngine sql;

    static
    {
        bot = new RiveScript();
        sql = new SQLDatabaseEngine();
        
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

    public void syncRiveScriptWithSQL(String userId){
        bot.setUservar(userId, "topic", sql.getUserInfo(userId, "topic"));
        bot.setUservar(userId, "state", sql.getUserInfo(userId, "state"));
    }

    public void debugMessage(String userId, Vector<String> replyMessages, boolean debug){
        if(debug == true) {
            replyMessages.add("Current state is " + bot.getUservar(userId, "state"));
            replyMessages.add("Current topic is " + bot.getUservar(userId, "topic"));
        }
    }

    /**
     * Get output message after inputting text
     * @param text A String data type
     * @return A String data type
     */
    public Vector<String> chat(String userId, String text, boolean debug) throws Exception {
    	Vector<String> replyMessages = new Vector<String>(0);
        String currentState = null;        
        boolean isRegisteredUser = true;
        isRegisteredUser = sql.searchUser(userId, "userinfo");

        System.out.println("chatText: Point 1");

        if (!isRegisteredUser) {
            System.out.println("chatText: Point 1a");

            currentState = "collect_user_info";
            bot.setUservar(userId, "state", "collect_user_info");
        }
        else{
            System.out.println("chatText: Point 1b");

            syncRiveScriptWithSQL(userId);
            currentState = bot.getUservar(userId, "state");
            bot.setUservar(userId, "met", "true");
        }

        System.out.println("chatText: Point 2");

        if(currentState.equals("standby") 
            && (((AdminState) states.get("admin")).matchTrigger(text) == 1)
            && userId.equals(ADMIN_USER_ID)){

            System.out.println("chatText: Point 2a");

            adminAccessing = true;
            replyMessages.add(states.get("admin").reply(userId, text, bot));
        }
        else{
            System.out.println("chatText: Point 2b");

            replyMessages.add(states.get(currentState).reply(userId, text, bot));
        }

        currentState = bot.getUservar(userId, "state");

        if(currentState.equals("recommend")) {            	
        	String[] splitString = (replyMessages.lastElement()).split("AAAAAAAAAA");       	            	          	
        	replyMessages.add(0, splitString[0]);         	          	
        	replyMessages.remove(replyMessages.size() - 1);
        
        	String temp = states.get(currentState).reply(userId, splitString[1], bot);           	
        	replyMessages.add(temp);
        }
        
        System.out.println("chatText: Point 3");

        if(replyMessages.size() > 0) {
            debugMessage(userId, replyMessages, debug)
        	return replyMessages;
        }
        else{
            throw new Exception("NOT FOUND");
        }
    }

    /**
     * Get output message after inputting image
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
    public Vector<String> chat(String userId, DownloadedContent jpg, boolean debug) throws Exception {
    	Vector<String> replyMessages = new Vector<String>(0);
    	String currentState = null;        
        boolean isRegisteredUser = true;
        isRegisteredUser = sql.searchUser(userId, "userinfo");

        if (!isRegisteredUser) {
            replyMessages.add("Please finish giving us your personal information before sharing photos!");
            return replyMessages;
        }
        else{
        	syncRiveScriptWithSQL(userId);
            currentState = bot.getUservar(userId, "state");
            
            if (currentState.equals("update_user_info")){
                replyMessages.add("Please finish updating your personal information before sharing photos!");
                return replyMessages;
            }
        }

        if (currentState.equals("input_menu") || currentState.equals("standby")){
            if (adminAccessing == false) {
                replyMessages.add(((InputMenuState) states.get("input_menu")).replyImage(userId, jpg, bot));
            }
            else{
                replyMessages.add(((AdminState) states.get("admin")).replyImage(userId, jpg, bot));
                adminAccessing = false;
            }
        }

        currentState = bot.getUservar(userId, "state");
        
        if(currentState.equals("recommend")) {               
            String[] splitString = (replyMessages.lastElement()).split("AAAAAAAAAA");                                       
            replyMessages.add(0, splitString[0]);                       
            replyMessages.remove(replyMessages.size() - 1);
        
            String temp = states.get(currentState).reply(userId, splitString[1], bot);              
            replyMessages.add(temp);
        }

        if(replyMessages.size() > 0) {
            debugMessage(userId, replyMessages, debug)
            return replyMessages;
        }
        else{
            throw new Exception("NOT FOUND");
        }
    }
    

    //use for save user info to database inside Rivescript
    public class setVariableToDB implements Subroutine {

        // assume the order of parameter is: variable name, value1, ... , userID
        public String call(RiveScript rs, String[] args) {

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