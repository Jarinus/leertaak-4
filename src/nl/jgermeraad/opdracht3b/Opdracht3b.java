package nl.jgermeraad.opdracht3b;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import nl.jgermeraad.Opdracht3;

public class Opdracht3b {
	private Connection connection;
	private boolean debug = false;
	
	public Opdracht3b() {
		try {
			initialize();
			
			new Thread(
				new InputHandler()
			).start();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void initialize() throws SQLException {
		connection = Opdracht3.createConnection();
	}
	
	public static void main(String[] args) {
		Opdracht3b opdracht3b = new Opdracht3b();
		
		if(args.length > 0)
			if(args[0].equals("debug"))
				opdracht3b.debug = true;
	}
	
	private class InputHandler implements Runnable {
		private boolean running;
		private BufferedReader inputReader;
		
		private final String READY_MESSAGE = "Please enter a command:\n> ";
		private final String PROCESSING_COMMAND = "Processing command...";
		private final String INVALID_COMMAND = "Invalid command '%s'. Try again.\n";
		private final String COMMAND_SUCCESS = "Succesfully executed command.\n";
		private final String COMMAND_FAIL = "Command failed.\n";
		
		private HashMap<String, Command> validCommands;

		@Override
		public void run() {
			setRunning(true);
			
			initializeInputReader(System.in);
			initializeValidCommands();
			
			print(READY_MESSAGE);
			
			String input = null;
			
			while(isRunning()) {
				try {
					if((input = inputReader.readLine()) == null)
						continue;
					
					println(PROCESSING_COMMAND);
					
					String command = getCommandForInput(input);
					
					if(!isValidCommand(command)) {
						printf(INVALID_COMMAND, input);	
						print(READY_MESSAGE);
						continue;
					}
					
					if(validCommands.get(command).run(input))
						println(COMMAND_SUCCESS);
					else
						println(COMMAND_FAIL);
					
					print(READY_MESSAGE);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private String getCommandForInput(String input) {
			for(String s : validCommands.keySet())
				if(input.startsWith(s))
					return s;
			return null;
		}
		
		private boolean isValidCommand(String command) {
			if(validCommands.containsKey(command))
					return true;
			return false;
		}

		private void initializeInputReader(InputStream input) {
			inputReader = new BufferedReader(new InputStreamReader(input));
		}
		
		private void initializeValidCommands() {
			validCommands = new HashMap<>();
			
			validCommands.put("select", (String input) -> {
				try {
					return select(input);
				} catch(SQLException e) {
					if(debug)
						e.printStackTrace();
				}
				return false;
			});
			
			validCommands.put("generate invoice", (String input) -> {
				try {
					return generateInvoice(input);
				} catch(SQLException e) {
					if(debug)
						e.printStackTrace();
				}
				return false;
			});
			
			validCommands.put("exit", (String input) -> exit());
		}
		
		private boolean exit() {
			setRunning(false);
			return true;
		}

		private boolean select(String input) throws SQLException {
			
			Statement statement = connection.createStatement();
			
			ResultSet result = statement.executeQuery(input);
			
			println("Query result:");
			
			printResults(result);
			
			return true;
		}

		private boolean generateInvoice(String input) throws SQLException {
			int month = 0;
			int year = 0;
			String phoneNumber = null;
			
			String[] split = input.split(" ");
			
			if(split.length < 5)
				return false;

			month = Integer.parseInt(split[2]);
			year = Integer.parseInt(split[3]);
			phoneNumber = split[4];
			
			if(month == 0 || year == 0 || phoneNumber == null) {
				printf(INVALID_COMMAND, input);	
				return false;
			}
			
			CallableStatement cs = connection.prepareCall(
					"{call GENERATE_INVOICE(?, ?, ?)}");
			
			cs.setInt(1, month);
			cs.setInt(2, year);
			cs.setString(3, phoneNumber);
			
			cs.executeQuery();
			
			return true;
		}

		private void print(String message) {
			System.out.print(message);
		}
		
		private void printf(String format, Object... args) {
			System.out.printf(format + "\n", args);
		}
		
		private void println(String message) {
			print(message + "\n");
		}
		
		private void printResults(ResultSet result) throws SQLException {
		    ResultSetMetaData meta = result.getMetaData();
		    int columnCount = meta.getColumnCount();
		    
		    while (result.next()) {
		        for (int i = 1; i <= columnCount; i++) {
		            if (i > 1)
		            	print(", ");
		            
		            String columnValue = result.getString(i);
		            print(meta.getColumnName(i) + ": " + columnValue);
		        }
		        
		        println("");
		    }
		}
		
		public boolean isRunning() {
			return running;
		}
		
		public void setRunning(boolean running) {
			this.running = running;
		}
		
	}
	
	private interface Command {
		boolean run(String s);
	}
	
}
