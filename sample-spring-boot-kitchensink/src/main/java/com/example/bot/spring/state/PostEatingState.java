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

        if (topic.equals("post_eating")) {
            string[] foods = text.split(",");
        }


		String output = bot.reply(userId, text);
		String afterState = bot.getUservar(userId, "state");

		// write to DB
		updateDatabase(userId, bot);
		return output;
	}
}