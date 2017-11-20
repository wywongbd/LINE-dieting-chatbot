package com.example.bot.spring;

import java.util.*;

//import java.io.IOException;
import com.rivescript.RiveScript;


import com.linecorp.bot.model.action.DatetimePickerAction;

// line api
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import java.time.LocalDate;

public class ProvideInfoState extends State {
    /**
     * Default constructor for ProvideInfoState
     */
	public ProvideInfoState() {
		
	}


    /**
     * Reply a message for input text
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
		String currentState = bot.getUservar(userId, "state");
        String currentTopic = bot.getUservar(userId, "topic");

        // put the whole input from user to db to query
        if (currentTopic.equals("provide_info_food_nutrient")) {
            // dummy output to go to standby state
            String output = bot.reply(userId, text);

            ArrayList<Double> result = null;
            String resultString = null;
            result = sql.getNutritionInfo(text);
            resultString = text + "(per 100g) contains "
                            + "\n*energy: " + Double.toString(result.get(0)) + "kcal"
                            + "\n*sodium: " + Double.toString(result.get(1)) + "mg"
                            + "\n*fat: "    + Double.toString(result.get(2)) + "g";
            return resultString;
        }

		String output = bot.reply(userId, text);
		String afterState = bot.getUservar(userId, "state");
		
		syncSQLWithRiveScript(userId, bot);
		return output;
	}


    public Message getButton() {
        LocalDate today = LocalDate.now().plusDays(1);
        String date = today.toString();

        List<Message> ReplyMessage = new ArrayList<Message>(0);
        ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                null, // image url
                null, // title
                "Click here to check nutrient history~", // reply from rivescript
                Arrays.asList(
                        new DatetimePickerAction("Date",
                                                "set starting date for nutrient history",
                                                "date",
                                                date,
                                                date,
                                                "2017-11-01")
                ));
        TemplateMessage templateMessage = new TemplateMessage("Button alt text", buttonsTemplate);

        return templateMessage;
    }
}