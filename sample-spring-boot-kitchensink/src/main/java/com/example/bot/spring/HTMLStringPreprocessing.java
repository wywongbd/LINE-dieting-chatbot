package com.example.bot.spring;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.jsoup.*;
import org.jsoup.nodes.*;

public class HTMLStringPreprocessing extends StringPreprocessing{
	
	public String getHTMLContents(String htmlString){
		/**
		 *  Goal: to obtain woscs in between the divider.
		 *  ie, <bla_bla> GET_THE_WOsc_HERE <bla_bla_bla> GET_THE_WOsc_HERE <bla_bla_bla>
		 *  
		 */
		
		char[] htmlStringArr = htmlString.toCharArray(); // "Working String"
		String content = null;
		ArrayList<Integer> listOfSigns = new ArrayList<Integer>(); // also dummy tool. Odd num stores indexOf("<"), even stores indexOf(">")
		int count = 0;
		
		for(int i=0; i< htmlStringArr.length; i++){
			if(htmlStringArr[i] == '<' || htmlStringArr[i] == '>'){
				listOfSigns.add(i);
			}
		}
		// now proceed to get the contents
		for(int j=1; j<listOfSigns.size()-1; j += 2){
			if(count==2){
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
		return content;
	}

	/**
     * Return the unprocessed contents from one the entire HTML code
     * @param url A String data type
     * this is the url link passed by the user
     * @return A ArrayList<String> data type
     * This is the meaningful content inside the HTML line,
     * which should be passed to getValidContent method
     * before passing into database for query to recommend food 
     */

	  public ArrayList<String> readFromUrl(String url) throws IOException {
		  	ArrayList<String> foodContent = new ArrayList<String>();
		    InputStream is = new URL(url).openStream();
		    try {
		      // print all the html lines
		    	BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		    	String temp;
		      boolean isNull = false;
		      while (!isNull) {
		    	  temp = rd.readLine();
		    	  	if(temp == null){ isNull = true;}
		    	  	else {
		    			if(getHTMLContents(temp) == null){
		    				continue;
		    			}
		    			else if(temp.toLowerCase().contains("<h") || temp.toLowerCase().contains("<p")) {
    				
		    				Document doc = Jsoup.parse(temp);
		    				foodContent.add(doc.body().text().toLowerCase());

		    			} 
		    		}
		    		// else throw no menu exception	  
		      }
		    } finally {
		      is.close();
		    }
		    return foodContent;
	  	}

		public ArrayList<String> processURLRawContent(ArrayList<String> URLRawContent){
			ArrayList<String> result = new ArrayList<String>();
			String processUnitContentString = null;    // Working variable
			  
			for(String unitContent: URLRawContent){
				// remove those with character > 150
				if(unitContent.length()> MAX_LINE_LENGTH) {continue;}
				
				processUnitContentString = processUnitContent(unitContent);
				if(!processUnitContentString.equals("")) {
					result.add(processUnitContentString); 
				}
			}
			return result;
		}
}
