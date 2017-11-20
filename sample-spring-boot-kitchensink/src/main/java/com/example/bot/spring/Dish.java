package com.example.bot.spring;

import java.util.ArrayList;

public class Dish{
	private double price;
	private String name;
	private ArrayList<String> ingredients = new ArrayList<String>();
	
	public Dish() {
		
	}
	
	public Dish(double price, String name, ArrayList<String> ingredients) {
		this.price = price;
		this.name = name;
		this.ingredients = ingredients;
	}
	
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

}