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
	
	// first check if a string is JSON
//	public boolean contentIsJSON(String content) {
//		try{
//			JSON.parse(content);
//		}
//		catch (Exception e) {
//			return false;
//		}
//		return true;
//	}
	
	public String getNameFromJSON(String url) throws IOException {
		String jsonString = JSONPreprocessing.readJSONUrl(url); // get the JSON String
		return Arrays.toString(JSONPreprocessing.getDishName(JSONPreprocessing.getDishFromJson(jsonString)));
	}
	
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
	
	public static Dish[] getDishFromJson(String rawJsonString){ 

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
	
	
	
	// this is the test function
//	public static void main(String[] args){
//		JSONPreprocessing t = new JSONPreprocessing();
//		final String jsonString = "[{\"price\": 35,\"name\": \"Spicy Bean curd with Minced Pork served with Rice\",\"ingredients\": [\"Pork\",\"Bean curd\",\"Rice\"]},{\"price\": 36,\"name\": \"Sweet and Sour Pork served with Rice\",\"ingredients\": [\"Pork\",\"Sweet and Sour Sauce\",\"Pork\"]},{\"price\": 28,\"name\": \"Chili Chicken on Rice\",\"ingredients\": [\"Chili\",\"Chicken\",\"Rice\"]}]";
//		ArrayList<String> names = t.getDishName(t.getDishFromJson(jsonString)); 
//		
//		for(String s : names){
//			System.out.println(s);
//		}
//	}
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




// this class is needed to parse the Json string into Java objects
class Dish{
	private double price;
	private String name;
	private ArrayList<String> ingredients = new ArrayList<String>();
	
	@Override
	public String toString() {
		return "Menu [price=" + price + ", name=" + name + ", ingredients=" + ingredients + ";";
	}
	
	public double getPrice() {
		return this.price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ArrayList<String> getIngredients() {
		return this.ingredients;
	}
	
	public void setIngredients(ArrayList<String> ingredients){
		this.ingredients = ingredients;
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if(obj instanceof Dish) {
//			if(this.price == ((Dish) obj).getPrice() && this.name.equals(((Dish) obj).getName()) && this.ingredients.equals(((Dish) obj).getIngredients())) {
//				return true;
//			}
//			else return false;
//		}
//		else return false; // not a dish type
//	}
}
	

