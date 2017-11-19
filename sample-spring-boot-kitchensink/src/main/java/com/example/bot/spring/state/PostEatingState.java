package com.example.bot.spring;

//import java.io.IOException;
import com.rivescript.RiveScript;

public class PostEatingState extends State {

    boolean extractFood;

    /**
     * Default constructor for PostEatingState
     */
	public PostEatingState() {
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

            // simple preprocessing by trimming new line character
            text = text.replace("\r", " ").replace("\n", " ").replace("  ", " ");

            // save the result in DB if the text is not empty
            if (text.length() > 1) {
                // save the date, userId, food in DB
                sql.addUserEatingHistory(userId, text);
            }
        }

		afterState = bot.getUservar(userId, "state");

		// write to DB
		syncSQLWithRiveScript(userId, bot);
		return output;
	}
}