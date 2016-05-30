package nl.jgermeraad;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import nl.jgermeraad.db.OracleDatabaseHandler;
import nl.jgermeraad.seed.DatabaseSeeder;

public class Opdracht3 {
	public static final String CONNECTION_USER = "hr";
	public static final String CONNECTION_PASSWORD = "oracle";
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Connection conn = null;
		int records = 10;
		
		System.out.println("Starting database seeding for " + records + " records.");
		
		try {
			conn = Opdracht3.createConnection();
			
			System.out.print("Connected.");
			
			Statement statement = conn.createStatement();
			statement.addBatch("DELETE FROM SMS");
			statement.addBatch("DELETE FROM KLANT");
			statement.addBatch("DELETE FROM BANK");
			statement.executeBatch();
			
			System.out.println(" Cleared Database. Ready for seeding.");
			
			DatabaseSeeder.seed(conn, records);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("Took: " + (System.currentTimeMillis() - start) + "ms.");
	}
	
	public static Connection createConnection() throws SQLException {
		OracleDatabaseHandler.connect(CONNECTION_USER, CONNECTION_PASSWORD);
		return OracleDatabaseHandler.getConnection();
	}
	
}
