package com.example.bot.spring;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import com.example.bot.spring.Dish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class DishDeserializer implements JsonDeserializer<Dish> {
	
	/**
	 * This is an overriden function of the GSON class
	 * The purpose of this is to tell the GSON parse function 
	 * how to create the custom java objects 
	 * which is defined by the Dish Class
	 * @param JsonElement and JsonDeserializationContext 
	 * @return A customized Dish Object
	 * @throws IOException
	 */
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