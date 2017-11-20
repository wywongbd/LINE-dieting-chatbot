package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.net.URISyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import java.time.LocalDate;

@Slf4j
public class SQLDatabaseEngine {
	// Get connection to PostgreSQL Database
	private Connection getConnection() throws URISyntaxException, Exception {
		Connection connection;
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() +  "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		log.info("Username: {} Password: {}", username, password);
		log.info ("dbUrl: {}", dbUrl);
		
		connection = DriverManager.getConnection(dbUrl, username, password);

		return connection;
	}
	
	
	/* 
	 * Writes input user info into the database
	 * Valid values for gender: male, female
	 * Unit for height: m
	 * Unit for weight: kg
	 */
	public void writeUserInfo(String userId, int age, String gender, double height, double weight, ArrayList<String> allergies, String diet, String topic, String state) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Delete user info if it already exists
			stmtUpdate = connection.prepareStatement(
				"DELETE FROM userinfo " +
				"WHERE userId = ?"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.executeUpdate();

			// Delete user allergies if it already exists
			stmtUpdate = connection.prepareStatement(
				"DELETE FROM userallergies " +
				"WHERE userId = ?"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.executeUpdate();	
			
			// Insert user info into the database
			stmtUpdate = connection.prepareStatement(
				"INSERT INTO userinfo " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.setInt(2, age);
			stmtUpdate.setString(3, gender);
			stmtUpdate.setDouble(4, height);
			stmtUpdate.setDouble(5, weight);
			stmtUpdate.setString(6, diet);
			stmtUpdate.setString(7, topic);
			stmtUpdate.setString(8, state);
			stmtUpdate.executeUpdate();

			// Insert user allergies into the database if they have any
			if (allergies.size() != 0) {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO userallergies " +
					"VALUES (?, ?)"
				);
				for (String allergy: allergies) {
					stmtUpdate.setString(1, userId);
					stmtUpdate.setString(2, allergy);
					stmtUpdate.addBatch();
				}
				stmtUpdate.executeBatch();					
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Sets the value of a single column for a user in userinfo table 
	// Overloaded function (String)
	public void setUserInfo(String userId, String info, String newInfo) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			statement = "UPDATE userinfo " +
						"SET " + info + " = ? " +
						"WHERE userid = ?";
			stmtUpdate = connection.prepareStatement(statement);
			stmtUpdate.setString(1, newInfo);
			stmtUpdate.setString(2, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Sets the value of a single column for a user in userinfo table 
	// Overloaded function (Int)
	public void setUserInfo(String userId, String info, int newInfo) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			statement = "UPDATE userinfo " +
						"SET " + info + " = ? " +
						"WHERE userid = ?";
			stmtUpdate = connection.prepareStatement(statement);
			stmtUpdate.setInt(1, newInfo);
			stmtUpdate.setString(2, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Sets the value of a single column for a user in userinfo table 
	// Overloaded function (Double)
	public void setUserInfo(String userId, String info, double newInfo) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			statement = "UPDATE userinfo " +
						"SET " + info + " = ? " +
						"WHERE userid = ?";
			stmtUpdate = connection.prepareStatement(statement);
			stmtUpdate.setDouble(1, newInfo);
			stmtUpdate.setString(2, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}

	
	// Returns the desired user info
	public String getUserInfo(String userId, String info) {
		Connection connection = null;
		String queryString = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		String result = null;
		
		try {
			connection = this.getConnection();
			
			// Returns user info if user exists
			queryString = "SELECT " + info + " FROM userinfo WHERE userid = '" + userId + "'"; 
			stmtQuery = connection.prepareStatement(queryString);
			rs = stmtQuery.executeQuery();
			while (result == null && rs.next()) {
				if (info.equals("age")) {
					result = Integer.toString(rs.getInt(1));

				}
				else if (info.equals("height") || (info.equals("weight"))) {
					result = Double.toString(rs.getDouble(1));
				}
				else {
					result = rs.getString(1);
				}
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
		return result;
	}
	

	// Sets the user allergies
	public void setUserAllergies(String userId, ArrayList<String> allergies) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			// Delete user allergies if it already exists
			stmtUpdate = connection.prepareStatement(
				"DELETE FROM userallergies " +
				"WHERE userId = ?"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.executeUpdate();

			// Insert user allergies into the database if they have any
			if (allergies.size() != 0) {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO userallergies " +
					"VALUES (?, ?)"
				);
				for (String allergy: allergies) {
					stmtUpdate.setString(1, userId);
					stmtUpdate.setString(2, allergy);
					stmtUpdate.addBatch();						
				}
				stmtUpdate.executeBatch();
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Returns the allergies of the input user
	public ArrayList<String> getUserAllergies(String userId) {
		ArrayList<String> result = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT allergy FROM userallergies " +
				"WHERE userid = ?"
			);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery(); 
			while(rs.next()) {
				result.add(rs.getString(1));
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}

	
	// Deletes user info from database
	public void deleteUserInfo(String userId) {
		Connection connection = null;
		PreparedStatement stmtDelete = null;
		
		try {
			connection = this.getConnection();
			
			// Delete user info if it already exists
			stmtDelete = connection.prepareStatement(
				"DELETE FROM userinfo " +
				"WHERE userId = ?"
			);
			stmtDelete.setString(1, userId);
			stmtDelete.executeUpdate();

			// Delete user allergies if it already exists
			stmtDelete = connection.prepareStatement(
				"DELETE FROM userallergies " +
				"WHERE userId = ?"
			);
			stmtDelete.setString(1, userId);
			stmtDelete.executeUpdate();	
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtDelete != null) {stmtDelete.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Adds meal name from input menu into meal table
	public void addMenu(String userId, ArrayList<String> menu) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Insert meal names into menu table
			stmtUpdate = connection.prepareStatement(
				"INSERT INTO menu VALUES (?, ?)"
			);
			stmtUpdate.setString(1, userId);
			for (String meal : menu) {
				stmtUpdate.setString(2, meal);
				stmtUpdate.addBatch();
			}
			stmtUpdate.executeBatch();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}
	
	
	/*
	 *  Reads meal names from meal table and matches them to the food in our nutrient table
	 *  Stores the closest match for each meal name into the recommendation table
	 */
	public void addRecommendations(String userId) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Insert meal names into menu table
			stmtUpdate = connection.prepareStatement(
				"INSERT INTO recommendations " +
				"SELECT " +
					"DISTINCT ON (menu.meal_name) " +
					"userid, " +
					"menu.meal_name, " +
					"food_type.food, " +
					"food_type.type, " +
					"similarity(menu.meal_name, food_type.food) AS sim, " +
					"? AS weightage " +
				"FROM menu " +
				"JOIN food_type " +
					"ON similarity(menu.meal_name, food_type.food) >= 0.2 " +
					"AND userid = ? " +
				"ORDER BY menu.meal_name, sim DESC"
			);
			stmtUpdate.setDouble(1, 1.0);
			stmtUpdate.setString(2, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}		
	}
	
	
	// Removes recommendations that the user is allergic to
	public void processRecommendationsByAllergies(String userId) {
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		PreparedStatement stmtUpdate = null;
		String update = null;
		ResultSet rs = null;
		
		try {
			connection = this.getConnection();
			
			// Retrieves user allergies
			stmtQuery = connection.prepareStatement(
				"SELECT description " +
				"FROM ( " +
					"SELECT allergy " +
					"FROM userallergies " +
					"WHERE userid = ? " +
				") AS UA " +
				"JOIN allergy_description " +
					"ON UA.allergy = allergy_description.allergy"
			);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery();
			
			// Removes recommendations that the user is allergic to			
			stmtUpdate = connection.prepareStatement(
				"DELETE FROM recommendations " + 
				"WHERE description LIKE CONCAT('%', ?, '%')"
			);
			while (rs.next()) {							
				stmtUpdate.setString(1, rs.getString(1));
				stmtUpdate.addBatch();
			}
			stmtUpdate.executeBatch();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}
	
	
	// Adjusts the weightages of meals corresponding to the user in the recommendations table based on the recommended daily intake
	public void processRecommendationsByIntake(String userId) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Adjusts the weightages of meals corresponsing to the user in the recommendations table
			stmtUpdate = connection.prepareStatement(
				"WITH daily_intake AS ( " + 
					"SELECT userinfo.userid, description, daily_serve " + 
					"FROM ( " + 
						"SELECT userid, description, food_type " + 
						"FROM recommendations " + 
						"WHERE userid = ? " + 
					") AS R " + 
					"JOIN userinfo ON R.userid = userinfo.userid " + 
					"JOIN recommended_intake RI " + 
						"ON userinfo.age >= RI.age_min " + 
						"AND userinfo.age <= RI.age_max " + 
						"AND userinfo.gender = RI.gender " + 
						"AND R.food_type = RI.type " + 
				") " + 
				"UPDATE recommendations " + 
				"SET weightage = daily_intake.daily_serve " + 
				"FROM daily_intake " + 
				"WHERE recommendations.description = daily_intake.description " +
				"AND recommendations.userid = daily_intake.userid"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Adjusts the weightages of meals corresponding to the user in the recommendations table based on their eating history
	public void processRecommendationsByEatingHistory(String userId) {
		Connection connection = null;
		ArrayList<String> foods = new ArrayList<String>();
		PreparedStatement stmtQuery = null;
		PreparedStatement stmtUpdate = null;
		ResultSet rs = null;

		try {
			connection = this.getConnection();
			
			// Collects the foods that the user has eaten in the past 2 days
			stmtQuery = connection.prepareStatement(
				"SELECT description " +
				"FROM eating_history " +
				"WHERE userid = ? " +
					"AND date >= CURRENT_DATE - 2"
			);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery();
			while (rs.next()) {
				for (String food: rs.getString(1).split(",")) {
					foods.add(food);
				}
			}

			// Adjusts the weightages of meals of the user in the recommendations table based on the collected foods
			stmtUpdate = connection.prepareStatement(
				"UPDATE recommendations " + 
				"SET weightage = weightage * 0.5 " + 
				"WHERE recommendations.description = ? " +
				"AND recommendations.userid = ?"
			);
			stmtUpdate.setString(2, userId);
			for (String food: foods) {
				stmtUpdate.setString(1, food);
				stmtUpdate.addBatch();
			}
			stmtUpdate.executeBatch();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Adjusts the weightages of meals corresponding to the user in the recommendations table based on their dieting goals
	public void processRecommendationsByGoal(String userId) {
		Connection connection = null;
		String diet = null;
		PreparedStatement stmtQuery = null;
		PreparedStatement stmtUpdate = null;
		ResultSet rs = null;
		
		try {
			connection = this.getConnection();
			
			// Retrieves the user's dieting goals
			stmtQuery = connection.prepareStatement(
				"SELECT diet " +
				"FROM userinfo " +
				"WHERE userid = ?"
			);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery();
			while (rs.next()) {
				diet = rs.getString(1);
			}

			// Adjusts the weightages of meals of the user in the recommendations table based on the user's dieting goals, if their diet is not "normal"
			if (!diet.equals("normal")) {
				stmtUpdate = connection.prepareStatement(
					"UPDATE recommendations R " + 
					"SET weightage = R.weightage * FDW.weightage " + 
					"FROM food_diet_weightage FDW " +
					"WHERE FDW.diet = ? " +
						"AND R.userid = ? " +
						"AND R.food_type = FDW.food"
				);
				stmtUpdate.setString(1, diet);
				stmtUpdate.setString(2, userId);
				stmtUpdate.executeUpdate();
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}	
	
	
	// Returns a HashMap of meal recommendations corresponding to the user
	public HashMap<String, Double> getRecommendationList(String userId) {
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		HashMap<String, Double> foodWeightage = new HashMap<String, Double>();
		String food = null;
		double weightage = 0;
		
		try {
			connection = this.getConnection();
			
			// Retrieves meal name and weightage to be put into the HashMap
			stmtQuery = connection.prepareStatement(
				"SELECT meal_name, weightage " + 
				"FROM recommendations " + 
				"WHERE userid = ?"
			);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery();
			while (rs.next()) {
				food = rs.getString(1);
				weightage = rs.getDouble(2);
				foodWeightage.put(food, weightage);
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
		return foodWeightage;
	}
	
	
	// Searchers for a user in the input table
	public boolean searchUser(String userId, String table) {
		String result = null;
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		String statement = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			statement = "SELECT userid FROM " + table + " WHERE userid = ?";
			stmtQuery = connection.prepareStatement(statement);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery(); 
			while(result == null && rs.next()) {
				result = rs.getString(1);
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		if (result != null) {return true;}
		else {return false;}
	}


	// Returns the input meal in the menu table if it exists. Returns null otherwise. (for debugging purposes)
	public String getMenu(String userId, String text) {
		String result = null;
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT meal_name FROM menu " +
				"WHERE userid = ? " +
					"AND meal_name LIKE CONCAT('%', ?, '%')"
			);
			stmtQuery.setString(1, userId);
			stmtQuery.setString(2, text);
			rs = stmtQuery.executeQuery(); 
			while(result == null && rs.next()) {
				result = rs.getString(1);
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}
	

	// Returns the input meal in the recommendations table if it exists. Returns null otherwise. (for debugging purposes)
	public String getRecommendation(String userId, String text) {
		//Write your code here
		String result = null;
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT meal_name FROM recommendations " +
				"WHERE userid = ? " +
					"AND meal_name LIKE CONCAT('%', ?, '%')"
			);
			stmtQuery.setString(1, userId);
			stmtQuery.setString(2, text);
			rs = stmtQuery.executeQuery(); 
			while(result == null && rs.next()) {
				result = rs.getString(1);
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Returns the weightage of the input meal in the recommendations table if it exists. Returns null otherwise. (for debugging purposes)
	public double getWeightage(String userId, String text) {
		//Write your code here
		double result = -1;
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT weightage FROM recommendations " +
				"WHERE userid = ? " +
					"AND meal_name LIKE CONCAT('%', ?, '%')"
			);
			stmtQuery.setString(1, userId);
			stmtQuery.setString(2, text);
			rs = stmtQuery.executeQuery(); 
			while(result == -1 && rs.next()) {
				result = rs.getDouble(1);
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Adds the input userId into the campaign_user table
	public void addCampaignUser(String userId) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			stmtUpdate = connection.prepareStatement(
				"INSERT INTO campaign_user " +
				"VALUES (?)"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}	
	}


	// Generates and stores a unique 6-digit code in the coupon_code database, with the userId of the user who requested it. Returns the code to the user.
	public int generateAndStoreCode(String userId) {
		int result = -1;
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		
		try {
			connection = this.getConnection();

			// Generate and insert the code into the coupon_code database, together with the userId
			stmtQuery = connection.prepareStatement(
				"SELECT count(*)+100000 " +
				"FROM coupon_code;"
			);
			rs = stmtQuery.executeQuery(); 
			while(result == -1 && rs.next()) {
				result = (rs.getInt(1));
			}

			stmtUpdate = connection.prepareStatement(
				"INSERT INTO coupon_code " +
				"VALUES (?, ?)"
			);
			stmtUpdate.setInt(1, result);
			stmtUpdate.setString(2, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
		return result;
	}


	// Sets the value of a the claimUser column with corresponding code to userId in the coupon_code table
	public void claimCode(String userId, int code) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;

		try {
			connection = this.getConnection();

			// Set claimUser of code to be userId
			stmtUpdate = connection.prepareStatement(
				"UPDATE coupon_code " +
				"SET claimUser = ? " +
				"WHERE code = ?"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.setInt(2, code);
			stmtUpdate.executeUpdate();

			// Delete userId from campaignUser table
			stmtUpdate = connection.prepareStatement(
				"DELETE FROM campaign_user " +
				"WHERE userid = ?"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Returns the allergies of the input user
	public ArrayList<String> getCodeInfo(int code) {
		ArrayList<String> result = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT requestUser, claimUser " +
				"FROM coupon_code " +
				"WHERE code = ?"
			);
			stmtQuery.setInt(1, code);
			rs = stmtQuery.executeQuery(); 
			while(result.size() <= 0 && rs.next()) {
				result.add(rs.getString(1));
				result.add(rs.getString(2));
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Sets the url in the coupon_url database
	public void setCouponUrl(String url) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			// Delete url if it already exists
			stmtUpdate = connection.prepareStatement(
				"DELETE FROM coupon_url"
			);
			stmtUpdate.executeUpdate();

			// Insert coupon url into database
			stmtUpdate = connection.prepareStatement(
				"INSERT INTO coupon_url " +
				"VALUES (?)"
			);
			stmtUpdate.setString(1, url);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Returns the coupon url
	public String getCouponUrl() {
		String result = null;
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT url " +
				"FROM coupon_url"
			);
			rs = stmtQuery.executeQuery(); 
			while(result == null && rs.next()) {
				result = rs.getString(1);
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Adds the input meals to the user's eating history
	public void addUserEatingHistory(String userId, String meals) {
		String[] mealList = meals.split(",");
		StringJoiner foodJoiner = new StringJoiner(",");
		StringJoiner typeJoiner = new StringJoiner(",");
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		PreparedStatement stmtUpdate = null;
		ResultSet rs = null;
		
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT " +
					"DISTINCT ON (meal_name) " +
					"food, " +
					"type, " +
					"? AS meal_name, " +
					"similarity(?, food_type.food) AS sim " +
				"FROM food_type " +
				"ORDER BY meal_name, sim DESC"
			);
			for (String meal: mealList) {
				stmtQuery.setString(1, meal);
				stmtQuery.setString(2, meal);
				rs = stmtQuery.executeQuery();
				while(rs.next()) {
					foodJoiner.add(rs.getString(1));
					typeJoiner.add(rs.getString(2));
				}
			}

			stmtUpdate = connection.prepareStatement(
				"INSERT INTO eating_history " +
				"VALUES (?, ?, ?, ?, CURRENT_DATE)"
			);
			stmtUpdate.setString(1, userId);
			stmtUpdate.setString(2, meals);
			stmtUpdate.setString(3, foodJoiner.toString());
			stmtUpdate.setString(4, typeJoiner.toString());
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}	
	}


	// Retrieves the user's eating history for the past input number of days
	public ArrayList<String> getUserEatingHistory(String userId, int days) {
		ArrayList<String> result = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT meals " +
				"FROM eating_history " +
				"WHERE userid = ? " +
					"AND date >= CURRENT_DATE - ?"
			);
			stmtQuery.setString(1, userId);
			stmtQuery.setInt(2, days);
			rs = stmtQuery.executeQuery(); 
			while(rs.next()) {
				result.add(rs.getString(1));
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Sets the isopen value of is_campaign_open
	public void setCampaign(int state) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			// Delete url if it already exists
			stmtUpdate = connection.prepareStatement(
				"DELETE FROM is_campaign_open"
			);
			stmtUpdate.executeUpdate();

			// Insert campaign state into database
			stmtUpdate = connection.prepareStatement(
				"INSERT INTO is_campaign_open " +
				"VALUES (?)"
			);
			stmtUpdate.setInt(1, state);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Returns true if campaign is open, returns false otherwise
	public boolean isCampaignOpen() {
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		boolean result = false;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT isopen " +
				"FROM is_campaign_open"
			);
			rs = stmtQuery.executeQuery(); 
			while(rs.next()) {
				if (rs.getInt(1) == 0) {result = false;}
				else {result = true;}
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Returns the nutrition info of the input food
	public ArrayList<Double> getNutritionInfo(String food) {
		ArrayList<Double> result = new ArrayList<Double>();
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT " +
					"DISTINCT ON (meal_name) " +
					"? AS meal_name, " +
					"energy_kcal, " +
					"sodium_mg, " +
					"fat_g, " +
					"similarity(?, nutrient_table.description) AS sim " +
				"FROM nutrient_table " +
				"ORDER BY meal_name, sim DESC"
			);
			stmtQuery.setString(1, food);
			stmtQuery.setString(2, food);
			rs = stmtQuery.executeQuery(); 
			while(rs.next()) {
				result.add(rs.getDouble(2));
				result.add(rs.getDouble(3));
				result.add(rs.getDouble(4));
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Returns true if the user has not claimed a cheat day in the past 7 days, returns false otherwise
	public boolean canClaimCheatDay(String userId) {
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		boolean result = true;
		try {
			connection = this.getConnection();

			stmtQuery = connection.prepareStatement(
				"SELECT meals " +
				"FROM eating_history " +
				"WHERE userid = ? " +
					"AND date >= CURRENT_DATE - 6"
			);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery(); 
			while(result == true && rs.next()) {
				if (rs.getString(1).equals("cheat day")) {
					result = false;
					break;
				}
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Returns true if the user has exceed their daily calorie quota, returns false otherwise
	public boolean exceedDailyCalorieQuota(String userId) {
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		double calorieQuota = 0;
		double consumedCalories = 0;
		ArrayList<String> eatenFoodTypes = new ArrayList<String>();
		try {
			connection = this.getConnection();

			// Retrieve the user's daily calorie quota
			stmtQuery = connection.prepareStatement(
				"SELECT calories " + 
				"FROM recommended_daily_calories RDC " +
				"JOIN userinfo ON userid = ? " + 
					"AND userinfo.age >= RDC.age_min " + 
					"AND userinfo.age <= RDC.age_max " + 
					"AND userinfo.gender = RDC.gender" 
			);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery(); 
			while(rs.next()) {
				calorieQuota = rs.getDouble(1);
			}

			// Collect the food types that the user has consumed today
			stmtQuery = connection.prepareStatement(
				"SELECT food_type " +
				"FROM eating_history " +
				"WHERE userid = ? " +
					"AND date = CURRENT_DATE"
			);
			stmtQuery.setString(1, userId);
			rs = stmtQuery.executeQuery(); 
			while(rs.next()) {
				for (String food_type: rs.getString(1).split(",")) {
					eatenFoodTypes.add(food_type);
				}
			}

			// Aggregate the total calories that the user has consumed today
			stmtQuery = connection.prepareStatement(
				"SELECT average_calories " +
				"FROM food_type_calories " +
				"WHERE food_type = ?"
			);
			for (String food_type: eatenFoodTypes) {
				stmtQuery.setString(1, food_type);
				rs = stmtQuery.executeQuery(); 
				while(rs.next()) {
					consumedCalories += rs.getDouble(1);
				}
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (connection != null) {connection.close();}
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		if (consumedCalories >= calorieQuota) {return true;}
		else {return false;}
	}


	// Deletes all records corresponding to the userId in the input table
	public void reset(String userId, String table) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			// Deletes records corresponding to the user from the input table
			statement = "DELETE FROM " + table + " WHERE userid = ?";
			stmtUpdate = connection.prepareStatement(statement);
			stmtUpdate.setString(1, userId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Deletes all records corresponding to the requestUserId in the coupon_code table
	public void resetCoupon(String requestUserId) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			stmtUpdate = connection.prepareStatement(
				"DELETE FROM coupon_code " +
				"WHERE requestuser = ?"
			);
			stmtUpdate.setString(1, requestUserId);
			stmtUpdate.executeUpdate();
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}	
}
