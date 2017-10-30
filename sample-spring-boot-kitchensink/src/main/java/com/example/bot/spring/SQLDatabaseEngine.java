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
public class SQLDatabaseEngine extends DatabaseEngine {
	@Override
	String search(String text) throws Exception {
		//Write your code here
		String result = null;
		Connection connection = null;
		PreparedStatement stmtQuery = null;
		PreparedStatement stmtUpdate = null;

		ResultSet rs = null;
		try {
			connection = this.getConnection();
			stmtQuery = connection.prepareStatement(
				"SELECT response, num_hits FROM keyword_response "
				+ "WHERE ? LIKE concat('%', LOWER(keyword), '%')"
			);
			stmtQuery.setString(1, text.toLowerCase());
			rs = stmtQuery.executeQuery(); 
			while(result == null && rs.next()) {
				String response = rs.getString(1);
				int numHits = rs.getInt(2) + 1;  // rs.getInt(2) does not include current call
				result = response + " " + String.valueOf(numHits);

				// Perform update for number of hits in db
				try {
					stmtUpdate = connection.prepareStatement(
						"UPDATE keyword_response "
						+ "SET num_hits = num_hits + 1 "
						+ "WHERE ? LIKE concat('%', LOWER(keyword), '%')"
					);
					stmtUpdate.setString(1, text.toLowerCase());
				    // Call executeUpdate to execute our sql update statement
					stmtUpdate.executeUpdate();
				} catch (SQLException e) {
					log.info("Exception while updating number of hits in database: {}", e.toString());
				} finally {
					if(stmtUpdate != null)
						stmtUpdate.close();
				}
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

}
