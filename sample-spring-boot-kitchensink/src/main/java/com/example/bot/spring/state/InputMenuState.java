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
     * Inherited from abstract base class
     * @param text A String data type
     * @return A bool data type
     */
	public boolean checkTrigger(String text) {
		return true;
	}

    /**
     * Reply a message for input text
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String text) {
		return "Your text has been well received!"; 
	}

    /**
     * Reply a message for input text
     * Overload the function inherited from abstract base class
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
	public String reply(DownloadedContent jpg) {
		return "Your image has been well received!";
	}
}