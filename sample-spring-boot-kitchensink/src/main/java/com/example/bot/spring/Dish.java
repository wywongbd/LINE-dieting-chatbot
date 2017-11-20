package com.example.bot.spring;

import java.util.ArrayList;

public class Dish{
	private double price;
	private String name;
	private ArrayList<String> ingredients = new ArrayList<String>();
	
    /**
     * Default constructor for Dish
     */
	public Dish() {
		
	}

    /**
     * Constructor for Dish
     * @param price A double data type indicating the price
     * @param name A String data type indicating the name
     * @param ingredients A ArrayList<String> data type indicating the ingredients
     */
	public Dish(double price, String name, ArrayList<String> ingredients) {
		this.price = price;
		this.name = name;
		this.ingredients = ingredients;
	}
	
    /**
     * Convert to string
     * @return String data type
     */
	@Override
	public String toString() {
		return "Menu [price=" + price + ", name=" + name + ", ingredients=" + ingredients + ";";
	}

    /**
     * Get the price of current dish
     * @return double data type
     */
	public double getPrice() {
		return this.price;
	}

    /**
     * Set the price of current dish
     * @param double data type as the price
     */
	public void setPrice(double price) {
		this.price = price;
	}

    /**
     * Get the name of current dish
     * @return String data type
     */
	public String getName() {
		return this.name;
	}

    /**
     * Set the name of current dish
     * @param String data type as the name
     */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * Get the ingredients of current dish
     * @return ArrayList<String> data type
     */
	public ArrayList<String> getIngredients() {
		return this.ingredients;
	}

    /**
     * Set the ingredients of current dish
     * @param ArrayList<String> data type as the list of ingredients
     */
	public void setIngredients(ArrayList<String> ingredients){
		this.ingredients = ingredients;
	}

}