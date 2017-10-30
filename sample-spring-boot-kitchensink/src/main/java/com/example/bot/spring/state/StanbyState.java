package com.example.bot.spring;

//import java.io.IOException;

public class StanbyState extends State {
    /**
     * Default constructor for StanbyState
     */
	public StanbyState() {
		
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
}