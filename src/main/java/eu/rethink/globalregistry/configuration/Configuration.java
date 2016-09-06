package eu.rethink.globalregistry.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration
{
	private static Configuration instance = null;
	
	private Properties prop;
	
	// fixed info
	private static final String versionName = "0.2.0";
	private static final int versionNumber = 1240;
	private static final String versionCode = "";
	private static final String versionDate = "2016-09-05";
	private static final String productName = "reTHINK Global Registry";
	private static final String productNameShort = "gReg";
	private static String filename = "greg.config";
	
	// cvars
	private int portServer;
	private String[] knownHosts;
	private String networkInterface;
	private String logPath;
	
	//Database config
	private  int db_backup = 0;
	private  String db_database = "globalregistry";
	private  String db_table = "dataset";
	private  String db_user = "root";
	private  String db_pass = "";
	
	// private Scanner scan;
	private Configuration() {
		prop = new Properties();
	}
	
	public static Configuration getInstance()
	{
		if (instance == null)
			instance = new Configuration();
		return instance;
	}
	
	// /////////////////////////////////////////////////////////////////////////////////////////////
	// cvar getters
	// /////////////////////////////////////////////////////////////////////////////////////////////

	public int getPortDHT() {
		return 5002;
	}

	public int getPortServer() {
		return this.portServer;
	}

	public String[] getKnownHosts() {
		return this.knownHosts;
	}

	public String getNetworkInterface() {
		return this.networkInterface;
	}

	public String getLogPath() {
		return logPath;
	}

	public String getConfigFilename() {
		return filename;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	
	public int getStartDatabase() {
		return db_backup;
	}

	public String getDatabase() {
		return db_database;
	}

	public String getUsername() {
		return db_user;
	}
	public String getPassword() {
		return db_pass;
	}
	public String getTable() {
		return db_table;
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////
	// config file handling
	// /////////////////////////////////////////////////////////////////////////////////////////////

	// // TODO: validate config data

	/**
	 * Loads configuration file.
	 */
	public void loadConfigurationFile(String pathToConfig) {

		String fullPathToConfig = pathToConfig + File.separator + "greg.config";

		if (!Configuration.propFileExists(fullPathToConfig)) {
			this.portServer = 5001;
			this.knownHosts = new String[0];
			this.networkInterface = "eth0";
			this.logPath = "/usr/local/gReg/logs";
			
		} else {

			InputStream input = null;

			try {
				input = new FileInputStream(filename);

				prop.load(input);

				this.portServer = Integer.parseInt(prop.getProperty("port_server"));
				String hosts = prop.getProperty("known_hosts");
				this.knownHosts = hosts.split(";");
				this.networkInterface = prop.getProperty("network_interface");
				this.db_backup = Integer.parseInt(prop.getProperty("start_database"));
				this.db_table = prop.getProperty("table");
				this.db_database = prop.getProperty("database");
				this.db_user = prop.getProperty("username");
				this.db_pass = prop.getProperty("password");
				this.setLogPath(prop.getProperty("log_path"));

				input.close();
			} catch (IOException e) {
				this.portServer = 5001;
				this.knownHosts = new String[0];
				this.networkInterface = "eth0";
				this.logPath = "/usr/local/gReg/logs";
			}
		}

	}

	/**
	 * Checks if a config file is available.
	 * 
	 * @return boolean
	 */
	public static boolean propFileExists(String path) {
		filename = path;
		InputStream input = null;
		try {
			input = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			System.out.println("no config file found");
			return false;
		} finally {
			if (input != null) {
				try {
					input.close();
					System.out.println("config file found");
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// static final getters
	// /////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * retrieves the product name as a String
	 * 
	 * @return String
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * retrieves the product name short as a String
	 * 
	 * @return String
	 */
	public String getProductNameShort() {
		return productNameShort;
	}

	/**
	 * retrieves the version number as a String, e.g. "0.1.2"
	 * 
	 * @return String
	 */
	public String getVersionName() {
		return versionName;
	}

	/**
	 * retrieves the date of the build, e.g. 2014-08-22
	 * 
	 * @return String
	 */
	public String getVersionDate() {
		return versionDate;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public String getVersionCode() {
		return versionCode;
	}
}