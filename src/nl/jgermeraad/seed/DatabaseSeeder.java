package nl.jgermeraad.seed;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Random;

public class DatabaseSeeder {
	private static final String MOBILE_NUMBER_PRESET = "0600000000";

	private static Random random = new Random();
	private static Connection connection = null;
	private static Statement statement = null;

	private static String[] messages = {
			"Hey, how are you?",
			"How do you do?",
			"Nice party yesterday!",
			"Hiya!",
			"Thanks for letting me borrow your car.",
			"Are you okay?",
			"I am sorry...",
			"I was wondering if you were free tonight."
	};

	public static void seed(Connection connection, int records) {
		DatabaseSeeder.connection = connection;

		try {
			statement = connection.createStatement();
			
			statement.executeUpdate("INSERT INTO BANK(REKENINGNUMMER, SALDO) "
					+ "VALUES ('NL00RABO0000000000', '0')");
			
			for(int i = 0; i < records; i++) {
				DatabaseSeeder.createPerson(i);

				if(i % 100 == 0)
					System.out.println("Current position: " + i);
			}

			System.out.println("Current position: " + records);

			System.out.println("Started seeding of SMS:");

			for(int i = 0; i < records; i++) {
				long start = System.currentTimeMillis();
				System.out.print((i + 1) + ": ");

				DatabaseSeeder.seedSmsForPerson(i, records);
				
				System.out.println(" took: " + (System.currentTimeMillis() - start) + "ms.");
			}
			
			statement.close();
			
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private static void createPerson(int id) throws SQLException {
		String bankAccountNumber = DatabaseSeeder.replaceFinalDigitsById("NL85RABO0000000000", id);
		String mobileNumber = DatabaseSeeder.replaceFinalDigitsById(MOBILE_NUMBER_PRESET, id);

		String[] memberships = {"SMS Budget", "SMS Always", "Total Care"};
		String membership = memberships[random.nextInt(3)];

		statement.executeUpdate("INSERT INTO BANK(REKENINGNUMMER, SALDO) "
				+ "VALUES('" + bankAccountNumber + "', 0)");
		statement.executeUpdate("INSERT INTO KLANT(VOORNAAM, ACHTERNAAM, GESLACHT, "
				+ "STRAAT, HUISNUMMER, POSTCODE, WOONPLAATS, MOBIEL_NUMMER, "
				+ "REKENINGNUMMER, ABONNEMENT, START_CONTRACT, END_CONTRACT) "
				+ "VALUES('Jan " + id +"', 'Germeraad', 'M', 'Neptunusstraat', 18, '9742JM', "
				+ "'Groningen', '" + mobileNumber + "', '" + bankAccountNumber
				+ "', '" + membership + "', '04-04-2015', '04-04-2017')");
	}

	private static void seedSmsForPerson(int id, int max) throws SQLException, ParseException {
		String mobileNumber = DatabaseSeeder.replaceFinalDigitsById(MOBILE_NUMBER_PRESET, id);
		int maxSms = getMaxSmsForMobileNumber(mobileNumber);

		for(int i = 1; i <= 12; i++)
			seedSmsForPerson(id, max, maxSms, mobileNumber, i);
	}

	private static void seedSmsForPerson(int id, int max, int maxSms, String mobileNumber, int month) throws SQLException, ParseException {
		maxSms = maxSms - 50 + random.nextInt(100);

		for(int i = 0; i < maxSms; i++) {
			int randInt = random.nextInt(max);
			int messageIndex = randInt % DatabaseSeeder.messages.length;

			String message = encodeMessage(DatabaseSeeder.messages[messageIndex]);

			String randomDate = getRandomDate(month);

			statement.executeUpdate("INSERT INTO SMS(NUMMER_VERZENDER, NUMMER_ONTVANGER, VERSTUURD_OP, BERICHT_INHOUD, LENGTE) VALUES"
					+ "('" + mobileNumber + "', "
					+ "'" + DatabaseSeeder.replaceFinalDigitsById(MOBILE_NUMBER_PRESET, random.nextInt(max)) + "', "
					+ randomDate + ", "
					+ "'" + message + "', "
					+ DatabaseSeeder.messages[messageIndex].length() + ")");
		}
		
		System.out.print(".");
	}

	private static int getMaxSmsForMobileNumber(String mobileNumber) throws SQLException {
		int maxSms = 0;

		Statement statement = connection.createStatement();

		String query = "SELECT SMS_TEGOED FROM ABONNEMENT "
				+ "JOIN KLANT ON ABONNEMENT.NAAM = KLANT.ABONNEMENT "
				+ "WHERE MOBIEL_NUMMER = '" + mobileNumber + "'";

		ResultSet result = statement.executeQuery(query);

		while(result.next())
			maxSms = Integer.parseInt(result.getString(1));

		statement.close();

		return maxSms;
	}

	private static String getRandomDate(int month) throws ParseException {
		return getRandomDateBetween(LocalDate.of(2015, month, 1),
				LocalDate.of(2016, month, 28));
	}

	private static String getRandomDateBetween(LocalDate firstDate, LocalDate secondDate) {
		int minDay = (int) firstDate.toEpochDay();
		int maxDay = (int) secondDate.toEpochDay();
		long randomDay = minDay + random.nextInt(maxDay - minDay);

		LocalDate randomBirthDate = LocalDate.ofEpochDay(randomDay);

		return "TO_DATE('" + randomBirthDate + "', 'YYYY-MM-DD')";
	}

	private static String encodeMessage(String input) {
		String output = "";
		int length = input.length();

		if (length % 2 == 1)
			input = "0" + input;

		for(int i = 0; i < length; i += 2) {
			output += input.charAt(i + 1);
			output += input.charAt(i);
		}

		return output;
	}

	private static String replaceFinalDigitsById(String input, int id) {
		return input.substring(0, 
				input.length() - String.valueOf(id).length())
				+ String.valueOf(id);
	}

}
