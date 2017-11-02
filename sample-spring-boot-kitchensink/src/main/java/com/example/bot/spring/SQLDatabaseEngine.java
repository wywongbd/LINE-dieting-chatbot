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

@Slf4j
public class SQLDatabaseEngine {
	// Get connection to PostgreSQL Database
	private Connection getConnection() throws URISyntaxException, SQLException {
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
	public void writeUserInfo(String userId, int age, String gender, double height, double weight, String[] allergies) throws Exception {
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
			} catch (SQLException e) {
				log.info("Exception while deleting existing user info from database: {}", e.toString());
			}
			
			// Insert user info into the database
			try {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO userinfo " +
					"VALUES (?, ?, ?, ?, ?)"
				);
				stmtUpdate.setString(1, userId);
				stmtUpdate.setInt(2, age);
				stmtUpdate.setString(3, gender);
				stmtUpdate.setDouble(4, height);
				stmtUpdate.setDouble(5, weight);
				stmtUpdate.executeUpdate();
			} catch (SQLException e) {
				log.info("Exception while inserting user info into database: {}", e.toString());
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (SQLException e) {  // Exception or IOException??
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}

	
	public String readUserInfo(String[] columns) {
		String result = "test";
		return result;
	}
	
	
	// Deletes user info from database
	public void deleteUserInfo(String userId) throws Exception {
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
			} catch (SQLException e) {
				log.info("Exception while deleting existing user info from database: {}", e.toString());
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtDelete != null) {stmtDelete.close();}
				if (connection != null) {connection.close();}
			} catch (SQLException e) {  // Exception or IOException??
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}


	// Adds meal name from input menu into meal table
	public void addMenu(String[] menu) throws Exception {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Insert meal names into menu table
			try {
				stmtUpdate = connection.prepareStatement(
					"INSERT INTO menu VALUES (?)"
				);
				for (String meal : menu) {
					stmtUpdate.setString(1, meal);
					stmtUpdate.addBatch();
				}
				stmtUpdate.executeBatch();
			} catch (SQLException e) {
				log.info("Exception while inserting data into menu table: {}", e.toString());
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (SQLException e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}
	

	// Deletes all records in the menu table
	public void resetMenu() throws Exception {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Deletes records from menu table
			try {
				stmtUpdate = connection.prepareStatement(
					"DELETE FROM menu"
				);
				stmtUpdate.executeUpdate();
			} catch (SQLException e) {
				log.info("Exception while deleting records from menu table: {}", e.toString());
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (SQLException e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}		
	}
	
	
	/*
	 *  Reads meal names from meal table and matches them to the food in our nutrient table
	 *  Stores the closest match for each meal name into the recommendation table
	 */
	public void addRecommendations() throws Exception {
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
						"menu.meal_name, " +
						"nutrient_table.description, " +
						"similarity(menu.meal_name, nutrient_table.description) AS sim " +
					"FROM menu " +
					"JOIN nutrient_table ON menu.meal_name % nutrient_table.description " +
					"ORDER BY menu.meal_name, sim DESC"
				);
				stmtUpdate.executeUpdate();
			} catch (SQLException e) {
				log.info("Exception while inserting records into the recommendations table: {}", e.toString());
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (SQLException e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}		
	}
	
	
	// Deletes all records in the recommendations table
	public void resetRecommendations() throws Exception {
		Connection connection = null;
		PreparedStatement stmtUpdate = null;
		
		try {
			connection = this.getConnection();
			
			// Deletes records from recommendations table
			try {
				stmtUpdate = connection.prepareStatement(
					"DELETE FROM recommendations"
				);
				stmtUpdate.executeUpdate();
			} catch (SQLException e) {
				log.info("Exception while deleting records from recommendations table: {}", e.toString());
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (SQLException e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}
	
	
	// Removes recommendations that the user is allergic to
	public void processRecommendationsByAllergies(String userId) throws Exception {
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
					"SELECT allergy " + 
					"FROM userallergies " + 
					"WHERE userid = ?"
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
			} catch (SQLException e) {
				log.info("Exception while removing recommendations from recommendations table: {}", e.toString());
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null) {rs.close();}
				if (stmtQuery != null) {stmtQuery.close();}
				if (stmtUpdate != null) {stmtUpdate.close();}
				if (connection != null) {connection.close();}
			} catch (SQLException e) {
				log.info("Exception while closing connection to database: {}", e.toString());
			}
		}
	}
	
	
	// Searches for a meal in the menu table (for debugging purposes)
	public String searchMenu(String text) throws Exception {
		//Write your code here
		String result = null;
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();
			stmtQuery = connection.prepareStatement(
				"SELECT meal_name FROM menu " +
				"WHERE meal_name LIKE CONCAT('%', ?, '%')"
			);
			stmtQuery.setString(1, text);
			rs = stmtQuery.executeQuery(); 
			while(result == null && rs.next()) {
				result = rs.getString(1);
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (SQLException ex) {  // Exception or IOException??
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		if (result != null)
			return result;
		throw new Exception("NOT FOUND");
	}
	

	// Searches for a meal in the recommendations table (for debugging purposes)
	public String searchRecommendations(String text) throws Exception {
		//Write your code here
		String result = null;
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		ResultSet rs = null;
		try {
			connection = this.getConnection();
			stmtQuery = connection.prepareStatement(
				"SELECT meal_name FROM recommendations " +
				"WHERE meal_name LIKE CONCAT('%', ?, '%')"
			);
			stmtQuery.setString(1, text);
			rs = stmtQuery.executeQuery(); 
			while(result == null && rs.next()) {
				result = rs.getString(1);
			}
		} catch (SQLException e) {
			log.info("Exception while connecting to database: {}", e.toString());
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmtQuery != null)
					stmtQuery.close();
				if (connection != null)
					connection.close();
			} catch (SQLException ex) {  // Exception or IOException??
				log.info("Exception while closing connection of database: {}", ex.toString());
			}
		}
		if (result != null)
			return result;
		throw new Exception("NOT FOUND");
	}
}
