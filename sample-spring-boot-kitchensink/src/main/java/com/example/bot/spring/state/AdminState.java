package com.example.bot.spring;

import com.example.bot.spring.DietbotController.DownloadedContent;
import com.rivescript.RiveScript;

import java.util.Set;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.nio.file.Paths;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class AdminState extends State {
    // Constant values
    private static final String ADMIN_TRIGGER = "admin:upload_coupon";
    private static String couponImageUrl = null;

    /**
     * Default constructor for AdminState
     */
    public AdminState() {
        
    }


    public int matchTrigger(String text) {
		if(text.toLowerCase().equals(ADMIN_TRIGGER)) {
			return 1;
		}
		return 0;
    }

    public String replyImage(String userId, DownloadedContent jpg, RiveScript bot) {
        sql.setCouponUrl(jpg.getUrl());
		return "Hi Admin, your image has been well received!";
    }

    public static String getImageUrl() {
		return sql.getCouponUrl();
    }

    /**
     * Reply a message for input text
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		if(matchTrigger(text) == 1) {
			return "Hi admin, please input a coupon image!";
		}
		else {
			// Still in this state, but user doesn't want to input image
			return "You should input a coupon image!";
		}
	}
}