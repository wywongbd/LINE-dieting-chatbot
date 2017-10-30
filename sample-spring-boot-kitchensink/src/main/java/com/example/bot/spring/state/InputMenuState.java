package com.example.bot.spring;

import com.example.bot.spring.DietbotController.DownloadedContent;

public class InputMenuState extends State {
    /**
     * Default constructor for InputMenuState
     */
	public InputMenuState() {
		
	}

    /**
     * Check if input message will trigger this state
     * @param text A String data type
     * @return A bool data type
     */
	public boolean checkTrigger(String text) {
		return true;
	}

    /**
     * Reply a message for input text
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String text) {
		return "Your text has been well received!"; 
	}
    /**
     * Reply a message for input text
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
	public String replyJPG(DownloadedContent jpg) {
		return "Your image has been well received!";
	}
}