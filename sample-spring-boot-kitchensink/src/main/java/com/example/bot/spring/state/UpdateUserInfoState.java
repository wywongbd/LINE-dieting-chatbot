package com.example.bot.spring;

//import java.io.IOException;
import java.util.*;
import com.rivescript.RiveScript;

public class UpdateUserInfoState extends State {
	public UpdateUserInfoState(){

	}

	public String reply(String userId, String text, RiveScript bot) {
		String output = bot.reply(userId, text);
		updateDatabase(userId, bot);
		return output;
	}
}