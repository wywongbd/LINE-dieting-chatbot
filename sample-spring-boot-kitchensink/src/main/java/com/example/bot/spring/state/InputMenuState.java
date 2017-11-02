package com.example.bot.spring;

import com.example.bot.spring.DietbotController.DownloadedContent;
import com.example.bot.spring.StringPreprocessing;
import com.asprise.ocr.Ocr;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;

public class InputMenuState extends State {
    // Constant values
    private final String NO_CHARACTER_MESSAGE = "There is no chracter in the image!";
    /**
     * Default constructor for InputMenuState
     */
    public InputMenuState() {
        
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
     * Reply a message for input image
     * Overload the function inherited from abstract base class
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
    public String reply(DownloadedContent jpg) {
    		ArrayList<String> processedOcrImage = processImage(jpg);
        if(processedOcrImage.size() > 0){
            // Convert to string to be replied as message for testing
            return Arrays.toString(processedOcrImage.toArray());
        }
        else {
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
        StringPreprocessing stringProcessing = new StringPreprocessing();
        return stringProcessing.processOcrRawString(ocrRawString);
    }

    /**
     * Perform OCR on the image and return the raw string
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
    public String ocrImage(DownloadedContent jpg) {
        Ocr.setUp();    // One time setup
        Ocr ocr = new Ocr();    // Create a new OCR engine
        ocr.startEngine("eng", Ocr.SPEED_SLOW);    // English
        String ocrRawString = ocr.recognize(new File[] {new File(jpg.getPathString())},
                                        Ocr.RECOGNIZE_TYPE_ALL,
                                        Ocr.OUTPUT_FORMAT_PLAINTEXT);
        ocr.stopEngine();    // Stop the OCR engine
        return ocrRawString;
    }
}