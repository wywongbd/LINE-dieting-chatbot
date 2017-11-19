package com.example.bot.spring;

import com.example.bot.spring.DietbotController.DownloadedContent;
import com.example.bot.spring.OCRStringPreprocessing;
import com.example.bot.spring.HTMLStringPreprocessing;
import com.example.bot.spring.JSONPreprocessing;
import com.rivescript.RiveScript;
import com.asprise.ocr.Ocr;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;



public class InputMenuState extends State {
    // Constant values
    private static final String NO_CHARACTER_MESSAGE = "There is no chracter in the image!";
    static final String URL_PATTERN_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    /**
     * Default constructor for InputMenuState
     */
    public InputMenuState() {
        
    }

    /**
     * Reply a message for input text
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String userId, String text, RiveScript bot) {
        String replyText = null;
        String urlContent = null;
        
		if(text.matches(InputMenuState.URL_PATTERN_REGEX)){
            // The text message is URL
			
			try {
				urlContent = replyUrl(text);
				bot.setUservar(userId, "url_received", "true");
				replyText = bot.reply(userId, "InputUrl");
	            bot.setUservar(userId, "url_received", "false");
	            bot.setUservar(userId, "topic", "recommend");
	            bot.setUservar(userId, "state", "recommend");
			}
			catch (Exception e) {
				urlContent = "";
				replyText = "Your text has been well received! But this URL is not reachable. :(";
			}

            syncSQLWithRiveScript(userId, bot);
            return replyText + "AAAAAAAAAA " + urlContent + " ";
		}
		else {
            // input text menu
			bot.setUservar(userId, "url_received", "false");
            bot.setUservar(userId, "topic", "recommend");
            bot.setUservar(userId, "state", "recommend");
            syncSQLWithRiveScript(userId, bot);
			return  "Thanks, I'm looking at your text menu now! I'll try to give you some recommendations." + "AAAAAAAAAA" + text;
		}
	}

    /**
     * Reply a message for input URL
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
    public String replyUrl(String text) throws Exception {
    	// this is HTML content
//    		try{
//  			HTMLStringPreprocessing h = new HTMLStringPreprocessing();
//  			ArrayList<String> URLRawContent = h.readFromUrl(text);
//  			ArrayList<String> processedUrlContent = h.processURLRawContent(URLRawContent); 
//  			
//  			// Convert to string to be replied as message for testing
//  			return Arrays.toString(processedUrlContent.toArray()); // this follows from proceddesURLRawContent
//  			
//  		  } catch(Exception e){ 
//  			  //TODO: handle user input invalid url
//  			  return "Your text has been well received! This URL is not reachable :(";
//  		  }
    	JSONPreprocessing j = new JSONPreprocessing();
    	String response = "";
    	try {
    		response += j.getNameFromJSON(text);  
    	}
    	catch (Exception e){ 
    		return "Not a JSON Website! :(";
    	}
    	 
    	return response;
    } 
    
    /**
     * Reply a message for input image
     * Overload the function inherited from abstract base class
     * @param jpg A DownloadedContent data type 
     * @return A String data type
     */
    public String replyImage(String userId, DownloadedContent jpg, RiveScript bot) {
	    ArrayList<String> processedOcrImage = processImage(jpg);
	    String replyText = null;
	    	
	    if(processedOcrImage.size() > 0){
            // Convert to string to be replied as message for testing
    		bot.setUservar(userId, "img_received", "true");
    		bot.setUservar(userId, "topic", "input_menu");
	        bot.setUservar(userId, "state", "input_menu");
	        replyText = bot.reply(userId, "InputImage");
	        bot.setUservar(userId, "img_received", "false");
	        bot.setUservar(userId, "topic", "recommend");
	        bot.setUservar(userId, "state", "recommend");
	        	
	        syncSQLWithRiveScript(userId, bot);
	        return replyText + "AAAAAAAAAA" + Arrays.toString(processedOcrImage.toArray());
        }
        else {
        	bot.setUservar(userId, "img_received", "false");
            syncSQLWithRiveScript(userId, bot);
            return "There is no useful information in your image!";
        }
    }

    /**
     * Process and filter the content in the image
     * @param jpg A DownloadedContent data type
     * @return A Set<String> data type
     */
    public ArrayList<String> processImage(DownloadedContent jpg) {
        String ocrRawString = ocrImage(jpg);
        OCRStringPreprocessing o = new OCRStringPreprocessing();
        return o.processOcrRawString(ocrRawString);
    }

    
    /**
     * Perform OCR on the image and return the raw string
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
    public String ocrImage(DownloadedContent jpg) {
    		String pathString = jpg.getPathString();
        return ocrImagePath(pathString);
    }

    /**
     * Perform OCR on the image and return the raw string
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
    public String ocrImagePath(String pathString) {
        Ocr.setUp();    // One time setup
        Ocr ocr = new Ocr();    // Create a new OCR engine
        ocr.startEngine("eng", Ocr.SPEED_SLOW);    // English
        String ocrRawString = ocr.recognize(new File[] {new File(pathString)},
                                        Ocr.RECOGNIZE_TYPE_ALL,
                                        Ocr.OUTPUT_FORMAT_PLAINTEXT);
        ocr.stopEngine();    // Stop the OCR engine
        return ocrRawString;
    }
}