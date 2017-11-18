package com.example.bot.spring;

import com.example.bot.spring.DietbotController.DownloadedContent;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.util.Set;



public class RecommendFriendState extends State {
    // Constant values
    private static final String FRIEND_TRIGGER = "friend";
    private static final String CODE_TRIGGER_REGEX = "^\\code\\b \\d+$";

    /**
     * Default constructor for RecommendFriendState
     */
    public RecommendFriendState() {
        
    }

    public int matchTrigger(String text) {
    		if(text.equals(CODE_TRIGGER)) {
    			return 1;
    		}
    		if(text.matches(FRIEND_TRIGGER_REGEX)) {
    			return 2;
    		}
    		return 0;
    }

    public int decodeCode(String text) {
    		return str.split(" ")[1];
    }
 
    /**
     * Reply a message for input text
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		if(text.equals(FRIEND_TRIGGER)) {
			int newCode = generateAndStoreCode(userId);
			return "Your code is" + Integer.toString(newCode);
		}
		else {
			int inputCode = decodeCode(text);
			String[] info = getInfoOfCode(inputCode);

			return "haha";
		}
	}
}