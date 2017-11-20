package com.example.bot.spring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.bot.spring.DishDeserializer; 

import org.json.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;


/** 
*  This class will process JSON files from a given website
*/

public class JSONPreprocessing {

	/**
	 * This function takes in a url that leads to json content hosted online
	 * @param a String that is the url 
	 * @return a JSON String
	 * @throws IOException
	 */
		
	public static String readJSONUrl(String url) throws IOException {
	  	String jsonString = "" ;
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
	    			jsonString += temp.trim();
	    		}
	    		// else throw no menu exception	  
	      }
	    } finally {
	      is.close();
	    }
	    return jsonString;
  	} 
	
	/**
	 * This function takes in a JSON string and converts it into Java objects
	 * @param a String that is the JSON text
	 * @return Dish[] array that containss dish objects defined by the Dish class
	 */
	
	public static Dish[] getDishFromJSON(String rawJsonString){ 

		GsonBuilder b = new GsonBuilder();
		
		// set up custom deserializer
		// this is needed because we need to parse Json objects with array member variable
		b.registerTypeAdapter(Dish.class, new DishDeserializer()); 

		Dish[] dishes = null;

		try{
			dishes = b.create().fromJson(rawJsonString, Dish[].class);
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		return dishes;
	}

	/**
	 * This function takes array of dish objects and gets the dish name 
	 * @param a Dish[] array of the dish objects
	 * @return a String[] that contains the dish names
	 * @throws IOException
	 */
	
	public static String[] getDishName(Dish[] dishes){
		String [] dishNames = new String[dishes.length];
		for(int i = 0; i < dishNames.length; i++) {
			dishNames[i] = new String(dishes[i].getName());
		}
		return dishNames;
	}
	

}

