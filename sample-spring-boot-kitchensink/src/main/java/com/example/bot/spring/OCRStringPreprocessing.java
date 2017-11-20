package com.example.bot.spring;

import java.util.ArrayList;

public class OCRStringPreprocessing extends StringPreprocessing{
    /**
     * Process the raw string returned by Ocr API
     * @param ocrRawString String data type
     * @return ArrayList<String> data type
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
			result.add(processUnitContentString);
		}
		return result;
	}
}
