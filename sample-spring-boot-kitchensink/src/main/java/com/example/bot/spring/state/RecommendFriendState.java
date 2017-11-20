package com.example.bot.spring;

import com.example.bot.spring.DietbotController.DownloadedContent;
import com.rivescript.RiveScript;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;



public class RecommendFriendState extends State {
    // Constant values
    private static final String FRIEND_TRIGGER = "friend:generate";
    private static final String DIGIT_REGEX = "[0-9]+";

    /**
     * Default constructor for RecommendFriendState
     */
    public RecommendFriendState() {
        
    }

    public String matchTrigger(String text) {
		if(text.toLowerCase().equals(FRIEND_TRIGGER)) {
			return "FRIEND";
		}
        else{
            String[] splitText = text.toLowerCase().split(":");
            if(splitText.length == 2){
                if(splitText[0].equals("code") && splitText[1].matches(DIGIT_REGEX)){
                    return "CODE";
                }
            }
            return "nothing";
        }
    }

    public String decodeCodeMessage(String text) {
    	return text.split(":")[1];
    }
 
    public String replyForFriendCommand(String userId) {
        String reply = null;
        if(sql.couponExceeds5000(5000)){
            reply = "Sorry, all coupon has been claimed already!";
            
        }
        else{
            int newCode = sql.generateAndStoreCode(userId);
            reply = "Thank you, your code is " + Integer.toString(newCode);
        }
        return reply;
    }

    public Vector<String> actionForCodeCommand(String userId, String code) {
        Vector<String> vec = new Vector<String>(0);

        if(!sql.searchUser(userId, "campaign_user")){
            // The user cannot claim
            vec.add("Sorry, you cannot claim coupon!");
        }
        else{
            ArrayList<String> ls = sql.getCodeInfo(Integer.valueOf(code));
            // ls is either size 0 or size 2
            if(ls.size() == 0){
                // This code does not exist
                vec.add("Sorry, this code does not exist!");
            }
            else{
                String requestUser = ls.get(0);
                String claimUser = ls.get(1);
                if(claimUser != null){
                    // Someone claimed this coupon ady
                    vec.add("Sorry, this code had been claimed!");
                }
                else if(requestUser.equals(userId)) {
                    vec.add("Sorry, You cannot claim your own code!");
                }
                else if(sql.couponExceeds5000(5000)){
                    vec.add("Sorry, all coupon has been claimed already!");
                }
                else{
                    // Can claim code
                    System.out.println("===============");
                    sql.claimCode(userId, Integer.valueOf(code));
                    System.out.println("DONE CLAIMING");
                    vec.add(requestUser);
                    vec.add(" ");    // To indicate that it's successful
                }
            }
        }
        return vec;
    }

    /**
     * Reply a message for input text
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		return "This function is not used";
	}
}