/**
* StateManager.java - A class for managing different states and transitions
*/

package com.example.bot.spring;

import com.rivescript.RiveScript;
import com.rivescript.macro.Subroutine;
import java.io.File;
import java.util.*;

import com.example.bot.spring.DietbotController.DownloadedContent;



// line api
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StateManager {
    
    // Constant values
    private final int STANDBY_STATE = 0;
    private final int INPUT_MENU_STATE = 3;
    private final int RECOMMEND_STATE = 4;
    private final String ADMIN_USER_ID = "Udfd2991f287cc5c75f6c1d2c30c58a3a";
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
        states.put("recommend_friend", new RecommendFriendState());

        adminAccessing = false;
    };
    
    /**
     * Prints two messages indicating current state and topic of RiveScript bot for debugging purposes
     * @param path A string indicating the path of RiveScript resources 
     */
    public StateManager(String path) {
        // Load rive files for Rivescript object
        File resourcesDirectory = new File(path);
        bot.loadDirectory(resourcesDirectory.getAbsolutePath());
        bot.sortReplies();
        bot.setSubroutine("setVariableToDB", new setVariableToDB());
        bot.setSubroutine("getNutritionOfFood", new getNutritionOfFood());
        bot.setSubroutine("getNutritionHistory", new getNutritionHistory());
    }

    /**
     * Prints two messages indicating current state and topic of RiveScript bot for debugging purposes
     * @param userId A String data type
     * @return a String indicating whether the userId is a recognized by Database or not
     */
    public String syncRiveScriptWithSQL(String userId){
        boolean isRegisteredUser = sql.searchUser(userId, "userinfo");
        
        if(isRegisteredUser){
            bot.setUservar(userId, "topic", sql.getUserInfo(userId, "topic"));
            bot.setUservar(userId, "state", sql.getUserInfo(userId, "state"));
            bot.setUservar(userId, "met", "true");

            return "REGISTERED USER";
        }
        else{
            // rivescript still recognize this uesr, but DB doesn't -> set rivescript to default topic & state
            if (bot.getUservar(userId, "state") != null){
                if (!bot.getUservar(userId, "state").equals("collect_user_info")){
                    bot.setUservar(userId, "state", "collect_user_info");
                    bot.setUservar(userId, "topic", "new_user");
                }
            }
            return "NEW USER";
        }
    }

    /**
     * Prints two messages indicating current state and topic of RiveScript bot for debugging purposes
     * @param userId A String data type
     * @param replyMessages A Vector of String 
     * @param debug A boolean 
     * @return nothing
     */
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
    public List<Message> chat(String userId, String text, boolean debug) throws Exception {
    	Vector<String> replyMessages = new Vector<String>(0);
        String currentState = null;
        String currentTopic = null;
        String userStatus = syncRiveScriptWithSQL(userId);
        List<Message> replyList = new ArrayList<Message>(0);

        if (userStatus.equals("NEW USER")) {
            bot.setUservar(userId, "state", "collect_user_info");
            replyMessages.add(states.get("collect_user_info").reply(userId, text, bot));
        }
        else if (userStatus.equals("REGISTERED USER")){
            currentState = bot.getUservar(userId, "state");

            if (currentState.equals("standby") && (((AdminState) states.get("admin")).matchTrigger(text) == 1)  && userId.equals(ADMIN_USER_ID)){
                adminAccessing = true;
                replyMessages.add(states.get("admin").reply(userId, text, bot));
            }
            else if (currentState.equals("standby") && (((RecommendFriendState) states.get("recommend_friend")).matchTrigger(text).equals("FRIEND"))){
                replyMessages.add(((RecommendFriendState) states.get("recommend_friend")).replyForFriendCommand(userId));
            }
            else{
                // normally will enter here
                replyMessages.add(states.get(currentState).reply(userId, text, bot));
                currentState = bot.getUservar(userId, "state");
                currentTopic = bot.getUservar(userId, "topic");

                if (currentState.equals("recommend")) {
                    String[] splitString = (replyMessages.lastElement()).split("AAAAAAAAAA");                                       
                    replyMessages.add(0, splitString[0]);                       
                    replyMessages.remove(replyMessages.size() - 1);
                
                    String recommendation = states.get("recommend").reply(userId, splitString[1], bot);              
                    replyMessages.add(recommendation);
                }

                // reply special message for special case
                if ( currentTopic.equals("provide_info_nutrient_history") ) {
                	// need to send the reply from Rivescript and create a datetime picker template

                	// reply text
                	replyList.add(new TextMessage(replyMessages.get(0)));

                	// reply datetime picker
                	replyList.add(((ProvideInfoState)states.get("provide_info")).getButton());
                    return replyList;
                }


            }
        }

        if(replyMessages.size() > 0) {
            debugMessage(userId, replyMessages, debug);

            for (String message : replyMessages) {
                log.info("In StateManager returns echo message, userId: {} message: {}", userId, message);
                replyList.add(new TextMessage(message));
            }
            return replyList;
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
        String userStatus = syncRiveScriptWithSQL(userId);        
        
        if (userStatus.equals("NEW USER")) {
            replyMessages.add("Please finish giving us your personal information before sharing photos!");
        }
        else if (userStatus.equals("REGISTERED USER")) {
            currentState = bot.getUservar(userId, "state");

            if (adminAccessing == true && currentState.equals("standby")){
                replyMessages.add(((AdminState) states.get("admin")).replyImage(userId, jpg, bot));
                adminAccessing = false;
            }
            else if (adminAccessing == false && (currentState.equals("input_menu") || currentState.equals("standby"))){
                replyMessages.add(((InputMenuState) states.get("input_menu")).replyImage(userId, jpg, bot));
                currentState = bot.getUservar(userId, "state");

                if(currentState.equals("recommend")) {               
                    String[] splitString = (replyMessages.lastElement()).split("AAAAAAAAAA");                                       
                    replyMessages.add(0, splitString[0]);                       
                    replyMessages.remove(replyMessages.size() - 1);
                
                    String temp = states.get(currentState).reply(userId, splitString[1], bot);              
                    replyMessages.add(temp);
                }
            }
            else if (currentState.equals("update_user_info")){
                replyMessages.add("Please finish updating your personal information before sharing me photos!");
            }
            else if (currentState.equals("post_eating")){
                replyMessages.add("Please let me finish recording your food intake before sharing me photos!");
            }
            else{
                replyMessages.add("Sorry, I am lost and I don't know how to respond. Please continue with your previous activity first.");
            }
        }

        if(replyMessages.size() > 0) {
            debugMessage(userId, replyMessages, debug);
            return replyMessages;
        }
        else{
            throw new Exception("NOT FOUND");
        }
    }
    

    public class setVariableToDB implements Subroutine {

        /**
        * Subroutine to be used within RiveScript to update user info 
        * @param rs A RiveScript object
        * @param args A String array
        * @return a String 
        */
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
                
            } else if ( args[0].equals("allergies") ) {
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


    //use for query nutrient of a food
    public class getNutritionOfFood implements Subroutine {

        // assume the order of parameter is: food name
        public String call(RiveScript rs, String[] args) {
        	ArrayList<Double> result = null;
        	String resultString = "";
        	if (args.length > 0) {
        		result = sql.getNutritionInfo(args[0]);
				resultString = args[0] + "(per 100g) contains "
								+ "\n*energy: " + Double.toString(result.get(0)) + "kcal"
								+ "\n*sodium: " + Double.toString(result.get(1)) + "mg"
								+ "\n*fat: " 	+ Double.toString(result.get(2)) + "g";
				return resultString;
        	}

            return "";
        }
    }

    //use for query nutrient history
    public class getNutritionHistory implements Subroutine {

        // assume the order of parameter is: # of days, userId
        public String call(RiveScript rs, String[] args) {
            ArrayList<Double> result = null;
            String resultString = "";
            if (args.length == 2) {
                result = sql.getAverageConsumptionInfo(args[1], Integer.parseInt(args[0]));
                resultString = "Your average nutrition comsumption per day over the past " + Integer.parseInt(args[0]) + " days:"
                                + "\n*energy: " + Double.toString(result.get(0)) + "kcal"
                                + "\n*sodium: " + Double.toString(result.get(1)) + "mg"
                                + "\n*fat: "    + Double.toString(result.get(2)) + "g";
                return resultString;
            }

            return "";
        }
    }

}