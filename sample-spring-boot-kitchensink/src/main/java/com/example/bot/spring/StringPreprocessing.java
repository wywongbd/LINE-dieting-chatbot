package com.example.bot.spring;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class StringPreprocessing {
	Set<String> stopWordsSet = null;

	// Constant values
	private static final int MIN_LINE_LENGTH = 4;
	protected static final int MAX_LINE_LENGTH = 150;
	private static final int MIN_WORD_LENGTH = 3;
	
	private final String[] STOPWORDS_ARRAY = {
			"with", "and", "you", "your", "our", "date", "choice", "served", "served", "get",
			"are", "is", "am", "were", "was",
			"breakfast", "lunch", "dinner",
			"special", "restaurant", "offer", "order", "free",
			"monday", "tuesday", "wednesday", "thursday", "friday",
			"january", "february", "march", "april", "may", "june",
			"july", "august", "september", "october", "november", "december",
			"main", "dishes", "more", "cal", "side", "choice", "come", "charge", "signature",
			"item", "details", "select", "for", "have", "has", "had", "available",
			"premium", "seasoned", "less", "additional", "menus", "ultimate",
			"terms", "conditions", "privacy", "policy", "condition",
			"tomorrow", "today", "yesterday", "subject", "based",
			"currently", "viewing", "website", "book", "stay", "signup", "find", "the",
			"type", "book", "reserve", "reservation", "booking", "desktop", "version", "website",
			"when", "will", "numbers", "how", "many", "adults", "children", "guests", "hotel",
			"season", "seasons", "hong", "kong", "nights", "days",
			"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
			"traditional", "fresh", "buffet", "eat", "meal", "download", "food", "phone", "specialties",
			"set", "alacarte", "table"
	};

	/**
     * Default constructor for StringPreprocessing
     */
	public StringPreprocessing() {
		// Convert to has set for faster search
		stopWordsSet = new HashSet<String>();
		for(String stopWord : STOPWORDS_ARRAY) {
			stopWordsSet.add(stopWord);
		}
	}
	
    /**
     * Process the raw string returned by Ocr API
     * @param ocrRawString A String data type
     * @param minLineLength A int data type
     * @param minWordLength A int data type
     * @param stopWordsSet A Set<String> data type
     * @return A Set<String> data type
     */
	public ArrayList<String> processOcrRawString(String ocrRawString){
		ArrayList<String> result = new ArrayList<String>();

		String[] splitOcrRawString = ocrRawString.split("\\r?\\n");
		ArrayList<String> longLowerCaseString = getLongLowerCaseString(splitOcrRawString, MIN_LINE_LENGTH);

		String processUnitContentString = null;    // Working variable
		for(String unitContent : longLowerCaseString) {
			// remove those with character > 150
			if(unitContent.length()> MAX_LINE_LENGTH) {continue;}

			processUnitContentString = processUnitContent(unitContent);
			if(!processUnitContentString.equals("")) {
				result.add(processUnitContentString);
			}
		}
		return result;
	}

    /**
     * Process the unitContent string
     * @param unitContent A String data type
     * @return A String data type
     */
	public String processUnitContent(String unitContent){
		String processUnitContentString = null;    // Working variable
		processUnitContentString = removeSpecialCharacters(unitContent);
		processUnitContentString = removeShortAndStopWord(processUnitContentString, MIN_WORD_LENGTH, stopWordsSet);
		processUnitContentString = removeExtraSpace(processUnitContentString);

		return processUnitContentString;
	}
	
    /**
     * Get long string and convert to lower case
     * @param unitContentArray A String[] data type
     * @param minLineLength A int data type
     * @return A ArrayList<String> data type
     */
	public ArrayList<String> getLongLowerCaseString(String[] unitContentArray, int minLineLength) {
		// This function also converts to lower case
		ArrayList<String> wordsList = new ArrayList<String>();
		for(String unitContent: unitContentArray){
	        if(unitContent.length() >= minLineLength){
	        		wordsList.add(unitContent.toLowerCase());
	        }
	    }
	    return wordsList;
	}

    /**
     * Remove words having length shorter than minWordLength
     * @param unitContent A String data type
     * @param minWordLength A int data type
     * @return A String data type
     */
	public String removeShortWords(String unitContent, int minWordLength) {
		// Currently not used
		String pattern = "\\b\\w{1," + minWordLength + "}\\b\\s?";
		return unitContent.replaceAll(pattern, "");
	}

    /**
     * Remove special characters in the string
     * @param unitContent A String data type
     * @return A String data type
     */
	public String removeSpecialCharacters(String unitContent){
		return unitContent.replaceAll("[^a-zA-Z\\s]", "");	  
	}

    /**
     * Remove short and stopwords in the string
     * @param unitContent A String data type
     * @param minWordLength A int data type
     * @param stopWordsSet A Set<String> data type
     * @return A String data type
     */
	public String removeShortAndStopWord(String unitContent, int minWordLength, Set<String> stopWordsSet) {
		String result = "";
		String[] words = unitContent.split(" ");
		ArrayList<String> wordsList = new ArrayList<String>();

		for(String word : words){
	        if(word.length() >= minWordLength && !stopWordsSet.contains(word)){
	            wordsList.add(word);
	        }
	    }
	    for (String word : wordsList){
	        result += word + " ";
	    }	
		return result;
	}

    /**
    * Remove >1 space to 1 space in the string
    * @param unitContent A String data type
    * @return A String data type
    */
	public String removeExtraSpace(String unitContent) {
		// Remove >1 space to 1 space
		return unitContent.replaceAll("^ +| +$|( )+", "$1");
	}
}