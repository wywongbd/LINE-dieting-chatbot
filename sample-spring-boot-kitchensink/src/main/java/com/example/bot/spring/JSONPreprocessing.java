package com.example.bot.spring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

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
* 
*/

public class JSONPreprocessing {

	/**
	 * This function takes in a url that leads to json content
	 * To process the result, the function 
	 * @param a String that is the url 
	 * @return the names of the dishes in the URL list
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

	public static String[] getDishName(Dish[] dishes){
		String [] dishNames = new String[dishes.length];
		for(int i = 0; i < dishNames.length; i++) {
			dishNames[i] = new String(dishes[i].getName());
		}
		return dishNames;
	}
	

}

/**
* 
*/

class DishDeserializer implements JsonDeserializer<Dish> {
@Override
public Dish deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
    JsonObject decodeObj = arg0.getAsJsonObject();
    Gson gson = new Gson();
    Dish dish = gson.fromJson(arg0, Dish.class);
    ArrayList<String> ingredients = null;
    if (decodeObj.get("ingredients").isJsonArray()) {
        ingredients = gson.fromJson(decodeObj.get("ingredients"), new TypeToken<ArrayList<String>>() {
        }.getType());
    } 
    else {
        String single = gson.fromJson(decodeObj.get("ingredients"), String.class);
        ingredients = new ArrayList<String>();
        ingredients.add(single);
    }
    dish.setIngredients(ingredients); 
    return dish;
 }
}


