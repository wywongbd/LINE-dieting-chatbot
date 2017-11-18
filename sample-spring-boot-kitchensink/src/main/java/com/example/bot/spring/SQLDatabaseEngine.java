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
	public void writeUserInfo(String userId, int age, String gender, double height, double weight, ArrayList<String> allergies, String topic, String state) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Delete user info if it already exists
			try {
				stmtUpdate = connection.prepareStatement(
					"DELETE FROM userinfo " +
					"WHERE userId = ?"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while deleting existing user info from database: {}", e.toString());
			}

			// Delete user allergies if it already exists
			try {
				stmtUpdate = connection.prepareStatement(
					"DELETE FROM userallergies " +
					"WHERE userId = ?"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while deleting existing user allergies from database: {}", e.toString());
			}			
			
			// Insert user info into the database
			try {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO userinfo " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.setInt(2, age);
				stmtUpdate.setString(3, gender);
				stmtUpdate.setDouble(4, height);
				stmtUpdate.setDouble(5, weight);
				stmtUpdate.setString(6, topic);
				stmtUpdate.setString(7, state);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while inserting user info into database: {}", e.toString());
			}

			// Insert user allergies into the database if they have any
			if (allergies.size() != 0) {
				try {
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
				} catch (Exception e) {
					log.info("Exception while inserting user allergies into database: {}", e.toString());
				}						
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
			
			try {
				statement = "UPDATE userinfo " +
							"SET " + info + " = ? " +
							"WHERE userid = ?";
				stmtUpdate = connection.prepareStatement(statement);
				stmtUpdate.setString(1, newInfo);
				stmtUpdate.setString(2, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while updating user info: {}", e.toString());
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {  // Exception or IOException??
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
			
			try {
				statement = "UPDATE userinfo " +
							"SET " + info + " = ? " +
							"WHERE userid = ?";
				stmtUpdate = connection.prepareStatement(statement);
				stmtUpdate.setInt(1, newInfo);
				stmtUpdate.setString(2, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while updating user info: {}", e.toString());
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {  // Exception or IOException??
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
			
			try {
				statement = "UPDATE userinfo " +
							"SET " + info + " = ? " +
							"WHERE userid = ?";
				stmtUpdate = connection.prepareStatement(statement);
				stmtUpdate.setDouble(1, newInfo);
				stmtUpdate.setString(2, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while updating user info: {}", e.toString());
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {  // Exception or IOException??
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
			try {
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
				log.info("Exception while retrieving user info: {}", e.toString());
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
		if (result != null) {
			return result;
		} else {
			return "Error in retrieving user info";
		}
	}
	

	// Sets the user allergies
	public void setUserAllergies(String userId, ArrayList<String> allergies) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			// Delete user allergies if it already exists
			try {
				stmtUpdate = connection.prepareStatement(
					"DELETE FROM userallergies " +
					"WHERE userId = ?"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while deleting existing user allergies from database: {}", e.toString());
			}

			// Insert user allergies into the database if they have any
			if (allergies.size() != 0) {
				try {
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
				} catch (Exception e) {
					log.info("Exception while inserting user allergies into database: {}", e.toString());					
				}
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
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (Exception ex) {  // Exception or IOException??
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
			try {
				stmtDelete = connection.prepareStatement(
					"DELETE FROM userinfo " +
					"WHERE userId = ?"
				);
				stmtDelete.setString(1, userId);
				stmtDelete.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while deleting existing user info from database: {}", e.toString());
			}

			// Delete user allergies if it already exists
			try {
				stmtDelete = connection.prepareStatement(
					"DELETE FROM userallergies " +
					"WHERE userId = ?"
				);
				stmtDelete.setString(1, userId);
				stmtDelete.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while deleting existing user allergies from database: {}", e.toString());
			}			
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtDelete != null) {stmtDelete.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {  // Exception or IOException??
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
			try {
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
				log.info("Exception while inserting data into menu table: {}", e.toString());
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
			try {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO recommendations " +
					"SELECT " +
						"DISTINCT ON (menu.meal_name) " +
						"userid, " +
						"menu.meal_name, " +
						"nutrient_table.description, " +
						"similarity(menu.meal_name, nutrient_table.description) AS sim, " +
						"? AS weightage " +
					"FROM menu " +
					"JOIN nutrient_table " +
						"ON menu.meal_name % nutrient_table.description " +
						"AND userid = ? " +
					"ORDER BY menu.meal_name, sim DESC"
				);
				stmtUpdate.setDouble(1, 1.0);
				stmtUpdate.setString(2, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while inserting records into the recommendations table: {}", e.toString());
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
	
	
	// Removes recommendations that the user is allergic to
	public void processRecommendationsByAllergies(String userId) {
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		PreparedStatement stmtUpdate = null;
		String update = null;
		ResultSet rs = null;
		
		try {
			connection = this.getConnection();
			
			// Removes recommendations from recommendations table that the user is allergic to
			try {
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
				log.info("Exception while removing recommendations from recommendations table: {}", e.toString());
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
	
	
	// Adjusts the weightages of meals corresponding to the user in the recommendations table based on the recommended daily intake
	public void processRecommendationsByIntake(String userId) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Adjusts the weightages of meals corresponsing to the user in the recommendations table
			try {
				stmtUpdate = connection.prepareStatement(
					"WITH daily_intake AS ( " + 
						"SELECT userinfo.userid, description, daily_serve " + 
						"FROM ( " + 
							"SELECT userid, description " + 
							"FROM recommendations " + 
							"WHERE userid = ? " + 
						") AS R " + 
						"JOIN food_type ON description LIKE CONCAT('%', food, '%') " + 
						"JOIN userinfo ON R.userid = userinfo.userid " + 
						"JOIN recommended_intake RI " + 
							"ON userinfo.age >= RI.age_min " + 
							"AND userinfo.age <= RI.age_max " + 
							"AND userinfo.gender = RI.gender " + 
							"AND food_type.type = RI.type " + 
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
				log.info("Exception while updating the weightages of the user in the recommendations table: {}", e.toString());
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
			try {
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
				log.info("Exception while retrieving recommendations from recommendations table: {}", e.toString());
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
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
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
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (Exception ex) {  // Exception or IOException??
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
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (Exception ex) {  // Exception or IOException??
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
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (Exception ex) {  // Exception or IOException??
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
			
			try {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO campaign_user " +
					"VALUES (?)"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while generating and storing code into the coupon_code table: {}", e.toString());
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
			try {
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
				log.info("Exception while generating and storing code into the coupon_code table: {}", e.toString());
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
		return result;
	}


	// Sets the value of a the claimUser column with corresponding code to userId in the coupon_code table
	public void claimCode(String userId, int code) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();

			// Set claimUser of code to be userId
			try {
				stmtUpdate = connection.prepareStatement(
					"UPDATE coupon_code " +
					"SET claimUser = ? " +
					"WHERE code = ?"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.setInt(2, code);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while updating user info: {}", e.toString());
			}

			// Delete userId from campaignUser table
			try {
				stmtUpdate = connection.prepareStatement(
					"DELETE FROM campaign_user " +
					"WHERE userid = ?"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while updating user info: {}", e.toString());
			}
		} catch (Exception e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (Exception e) {  // Exception or IOException??
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
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (Exception ex) {  // Exception or IOException??
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
			try {
				stmtUpdate = connection.prepareStatement(
					"DELETE FROM coupon_url"
				);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while deleting existing coupon url from database: {}", e.toString());
			}

			// Insert coupon url into database
			try {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO coupon_url " +
					"VALUES (?)"
				);
				stmtUpdate.setString(1, url);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while inserting coupon url into database: {}", e.toString());					
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
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Adds the input meals to the user's eating history
	public void addUserEatingHistory(String userId, String meals) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		// String date = LocalDate.now().toString();
		
		try {
			connection = this.getConnection();
			try {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO eating_history " +
					"VALUES (?, ?, CURRENT_DATE)"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.setString(2, meals);
				// stmtUpdate.setString(3, date);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while generating and storing code into the coupon_code table: {}", e.toString());
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


	// Retrieves the user's eating history for the past 3 days
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
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (Exception ex) {
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		return result;
	}


	// Deletes all records corresponding to the userId in the input table
	public void reset(String userId, String table) {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		String statement = null;
		
		try {
			connection = this.getConnection();
			
			// Deletes records corresponding to the user from the input table
			try {
				statement = "DELETE FROM " + table + " WHERE userid = ?";
				stmtUpdate = connection.prepareStatement(statement);
				stmtUpdate.setString(1, userId);
				stmtUpdate.executeUpdate();
			} catch (Exception e) {
				log.info("Exception while deleting records from recommendations table: {}", e.toString());
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
}
