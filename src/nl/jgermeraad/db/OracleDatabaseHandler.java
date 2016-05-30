package nl.jgermeraad.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import oracle.jdbc.driver.OracleDriver;

public class OracleDatabaseHandler {
	private static Connection connection;
	
	public static void connect(String user, String pass) throws SQLException {
		DriverManager.registerDriver(new OracleDriver());
		connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", user, pass);
	}
	
	public static Connection getConnection() {
		return connection;
	}
	
}
