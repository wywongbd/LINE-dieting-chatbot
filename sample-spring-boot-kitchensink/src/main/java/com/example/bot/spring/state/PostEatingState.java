package com.example.bot.spring;

//import java.io.IOException;
import com.rivescript.RiveScript;

public class PostEatingState extends State {

    boolean extractFood;

    /**
     * Default constructor for PostEatingState
     */
	public PostEatingState() {
		extractFood = false;
	}

    /**
     * Reply a message for input text
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		String currentState = bot.getUservar(userId, "state");
        String topic = bot.getUservar(userId, "topic");

        String output = bot.reply(userId, text);
        String afterState = null;

        // when user is not typing leave
        if ( !output.equals("Okay. Tell me when you need help~~~") ) {

            // simple reprocessing by trimming new line character
            text = text.replace("\r", " ").replace("\n", " ");

            // save the result in DB if the text is not empty
            if (text.length() > 1) {
                // save the date, userId, food in DB
            }
        }

		afterState = bot.getUservar(userId, "state");

		// write to DB
		updateDatabase(userId, bot);
		return output;
	}
}