package com.example.bot.spring;

import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import com.linecorp.bot.model.profile.UserProfileResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;

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

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LineMessageHandler
public class DietbotController {
	
	@Autowired
	private LineMessagingClient lineMessagingClient;
	
	private StateManager stateManager;
	private final String defaultString = "I don't understand"; 
	private RecommendFriendState recommendFriendState = new RecommendFriendState();
	
	protected DietbotController() {
		stateManager = new StateManager("sample-spring-boot-kitchensink/src/main/resources/rivescript");
	}
	
	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
		log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		log.info("This is your entry point:");
		log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		TextMessageContent message = event.getMessage();
		handleTextContent(event.getReplyToken(), event, message);
	}
	
	@EventMapping
	public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
		final MessageContentResponse response;
		String replyToken = event.getReplyToken();
		String messageId = event.getMessage().getId();
		
		try {
			response = lineMessagingClient.getMessageContent(messageId).get();
		} catch (InterruptedException | ExecutionException e) {
			reply(replyToken, new TextMessage("Cannot get image: " + e.getMessage()));
			throw new RuntimeException(e);
		}
		
		DownloadedContent jpg = saveContent("jpg", response);
		handleImageContent(event.getReplyToken(), event, jpg);

	}
    
    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got followed event");
        String userId = event.getSource().getUserId();
		SQLDatabaseEngine sql = new SQLDatabaseEngine();

		if(!sql.searchUser(userId, "userinfo")
			&& !sql.searchUser(userId, "campaign_user")){
			sql.addCampaignUser(userId);
		}
    }
	
	private void replyText(@NonNull String replyToken, @NonNull String message) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken must not be empty");
		}
		if (message.length() > 1000) {
			message = message.substring(0, 1000 - 2) + "..";
		}
		this.reply(replyToken, new TextMessage(message));
	}

	private void replyImage(@NonNull String replyToken, @NonNull String url) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken must not be empty");
		}
		this.reply(replyToken, new ImageMessage(url, url));
	}
	
	private void reply(@NonNull String replyToken, @NonNull Message message) {
		reply(replyToken, Collections.singletonList(message));
	}

	private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
		try {
			BotApiResponse apiResponse = lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messages)).get();
			log.info("Sent messages: {}", apiResponse);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void pushText(@NonNull String to, @NonNull String message) {
		if (to.isEmpty()) {
			throw new IllegalArgumentException("user id must not be empty");
		}
		if (message.length() > 1000) {
			message = message.substring(0, 1000 - 2) + "..";
		}
		this.push(to, new TextMessage(message));
	}

	private void pushImage(@NonNull String to, @NonNull String url) {
		if (to.isEmpty()) {
			throw new IllegalArgumentException("user id must not be empty");
		}
		this.push(to, new ImageMessage(url, url));
	}

	private void push(@NonNull String to, @NonNull Message message) {
		push(to, Collections.singletonList(message));
	}

	private void push(@NonNull String to, @NonNull List<Message> messages) {
		try {
			BotApiResponse apiResponse = lineMessagingClient.pushMessage(new PushMessage(to, messages)).get();
			log.info("Sent messages: {}", apiResponse);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) {
        String text = content.getText();
        log.info("Got text message from {}: {}", replyToken, text);
        
        Vector<String> reply = null;
        List<Message> replyList = new ArrayList<Message>(0);
        String userId = event.getSource().getUserId();
        try {
			UserProfileResponse profile = lineMessagingClient.getProfile(event.getSource().getUserId()).get();

			// text: "code 123456"
			// Exception couponIsValid
			if (recommendFriendState.matchTrigger(text).equals("FRIEND")){
				reply = recommendFriendState.replyForFriendCommand(userId);
			}
			// else if (recommendFriendState.matchTrigger(text).equals("CODE")){
			// 	String code = recommendFriendState.decodeCodeMessage(text);
			// 	reply = recommendFriendState.actionForCodeCommand(userId, code);
			// 	if(reply.size() == 2) {
			// 		SQLDatabaseEngine sql = new SQLDatabaseEngine();
			// 		String url = sql.getCouponUrl();
			// 		String requestUser = reply.get(0);
   //          		String claimUser = reply.get(1);				
   //          		// Reply image to claimUser
   //          		this.replyImage(replyToken, url);
   //          		// Push image to requestUser
   //          		this.pushImage(requestUser, url);
			// 		return;
			// 	}
			// }
			else {
				reply = stateManager.chat(userId, text, true);
			}
    	} catch (Exception e) {
    		this.replyText(replyToken,defaultString);
    		return;
    	}
    	
    	for (String replyMessage:reply) {
         	log.info("Returns echo message {}: {}", replyToken, replyMessage);
         	replyList.add(new TextMessage(replyMessage));
        }
    	
        this.reply(replyToken,replyList);
     
    }
	
	private void handleImageContent(String replyToken, Event event, DownloadedContent jpg) {
		Vector<String> reply = null;
		List<Message> replyList = new ArrayList<Message>(0);
	    	try {
	    		reply = stateManager.chat(event.getSource().getUserId(), jpg, true);
	    	} catch (Exception e) {
	    		this.replyText(replyToken,defaultString);
	    		return;
	    	}
	        
	    	for (String replyMessage:reply) {
	         log.info("Returns echo message {}: {}", replyToken, replyMessage);
	         replyList.add(new TextMessage(replyMessage));
	    	}
	    this.reply(replyToken,replyList);
    }
	
	private static DownloadedContent saveContent(String ext, MessageContentResponse responseBody) {
		log.info("Got content-type: {}", responseBody);

		DownloadedContent tempFile = createTempFile(ext);
		try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
			ByteStreams.copy(responseBody.getStream(), outputStream);
			log.info("Saved {}: {}", ext, tempFile);
			return tempFile;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static DownloadedContent createTempFile(String ext) {
		String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
		Path tempFile = DietbotApplication.downloadedContentDir.resolve(fileName);
		tempFile.toFile().deleteOnExit();
		return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));
	}
	
	static String createUri(String path) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString();
	}

	//The annontation @Value is from the package lombok.Value
	//Basically what it does is to generate constructor and getter for the class below
	//See https://projectlombok.org/features/Value
	@Value
	public static class DownloadedContent {
		Path path;
		String uri;

		public String getPathString() {
			return path.toString();
		}
		public String getUrl() {
			return uri;
		}
	}
}