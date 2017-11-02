package com.example.bot.spring;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;



public class InputStringProcessing {
	
	/**
     * Return the "valid" contents
     * Definition: valid contents are alphabetical strings only, 
     * usually nouns of food/drinks
     * @param unitContent A String data type 
     * @return A String data type
     */
	
	public static String getValidContent(String unitContent){
		
		// unitContent = Word1 + Word2 + ... + WordN
		// unitWordArray is the char [] of each word in wordArray
		String validUnitContent = null; // return this string
		String [] wordArr = null;
		if(unitContent.contains(",")){
			wordArr = unitContent.split(",");
		} else {
			wordArr = new String[1];
			wordArr[0] = unitContent;
		}
		
		for(int i=0; i< wordArr.length; i++){
			char[] unitWordArr= wordArr[i].toCharArray();
				
				for(int j=0; j<unitWordArr.length; j++){
					if((int) unitWordArr[j] < 97 || (int) unitWordArr[j] >122){
						break;
					}
				}
				// append string if word is valid
				if(validUnitContent == null){
					validUnitContent = wordArr[i];
				}
				else{
					validUnitContent += " " +  wordArr[i];
				}
			}
	
		return validUnitContent;
	}
		
	/**
     * Return the contents from one line of HTML code
     * @param unitContent A String data type
     * This is usually one line of HTML code 
     * @return A String data type
     * This is the meaningful content inside the HTML line
     */

	public static String getHTMLContents(String htmlString){
		
		/**
		 *  Goal: to obtain words in between the divider.
		 *  ie, <bla_bla> GET_THE_WORD_HERE <bla_bla_bla> GET_THE_WORD_HERE <bla_bla_bla>
		 *  
		 */
		
		char[] htmlStringArr = htmlString.toCharArray(); 
		String content = null;
		
		// also dummy tool. Odd num stores indexOf("<"), even stores indexOf(">")
		ArrayList<Integer> listOfSigns = new ArrayList<Integer>(); 
		
		// get all positions of '<' and '>' characters
		for(int i=0; i< htmlStringArr.length; i++){
			if(htmlStringArr[i] == '<' || htmlStringArr[i] == '>'){
				listOfSigns.add(i);
			}
		}
		
		// now proceed to get the contents
		for(int j=1; j<listOfSigns.size()-1; j += 2){
			
			if(listOfSigns.size()==2){
				// if htmlString = <abc>
				break;
			}
			else if(listOfSigns.get(j) + 1 == listOfSigns.get(j+1)){
				// if <abc><abc> get_content_here <abc>
				continue;
			}
			else{
				// done get content, now need to preprocess
				String unitContent = htmlString.substring(listOfSigns.get(j) + 1, listOfSigns.get(j+1)).toLowerCase();
				
				// need to remove the content contains rubbish like sign characters, numbers etc
					if(content == null){
						content = unitContent;	
					}
					else{
						content += ", " + unitContent;
					}
			}
		}
		
//		TODO: return getValidContent(content); 
// 		for now, return non-valid contents, because getValidContent function is still under construction
		return content;
		
	}
	
	/**
     * Return the contents from one the entire HTML code
     * @param url A String data type
     * this is the url link passed by the user
     * @return A String[] data type
     * This is the meaningful content inside the HTML line,
     * which should be passed into database for query -> recommend food 
     * based on food available on the website
     */
	
  public static ArrayList<String> readFromUrl(String url) throws IOException {
    InputStream is = new URL(url).openStream();
    ArrayList<String> foodContent = new ArrayList<String>();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
     
      // print all the html lines
      String temp;
      boolean isNull = false;
      while (!isNull)
      {
    	  temp = rd.readLine();
    	  	if(temp == null){ isNull = true;}
    	  	else if(temp.toLowerCase().contains("<h")){
    			if(getHTMLContents(temp) == null){
    				continue;
    			}
    			else{
    				foodContent.add(getHTMLContents(temp));
    			}
    		}
    		// TODO: else throw no menu exception		  
      }
    } finally {
      is.close();
    }
    
    return foodContent;
  } 
}
