package eu.rethink.globalregistry.dao;

import java.sql.Connection;
import java.sql.DriverManager;

import eu.rethink.globalregistry.configuration.Configuration;

public class Database {

	public Connection getConnection() throws Exception {
		try {

			String connectionURL = "jdbc:mysql://localhost:3306/"+Configuration.getInstance().getDatabase();
			Connection connection = null;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(connectionURL,Configuration.getInstance().getUsername() , Configuration.getInstance().getPassword());
			return connection;
		} catch (Exception e) {
			throw e;
		}

	}
}
