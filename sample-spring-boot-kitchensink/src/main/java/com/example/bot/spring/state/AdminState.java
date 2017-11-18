package com.example.bot.spring;

import com.example.bot.spring.DietbotController.DownloadedContent;

import java.util.Set;
import java.util.Random;

public class AdminState extends State {
    // Constant values
    private static final String ADMIN_TRIGGER = "admin_upload_image";

    private bool isCouponUploaded = false;

    /**
     * Default constructor for AdminState
     */
    public AdminState() {
        
    }

    public int matchTrigger(String text) {
		if(text.equals(CODE_TADMIN_TRIGGERRIGGER)) {
			return 1;
		}
		return 0;
    }

    /**
     * Reply a message for input text
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		if(matchTrigger(text) == 1) {
			return "Hi admin, please input a coupon image!"
		}
		else {
			// Still in this state, but user doesn't want to 
			return "You should input a coupon image!"
		}
	}
}