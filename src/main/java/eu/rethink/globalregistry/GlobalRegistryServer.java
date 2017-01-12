package globalregistry;

import globalregistry.configuration.Config;
import globalregistry.dht.DHTManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

/**
 * Main class for GlobalRegistry daemon
 * 
 * @date 10.01.2017
 * @version 1
 * @author Sebastian Göndör, Parth Singh
 */
@SpringBootApplication
public class GlobalRegistryServer implements Daemon
{
	private static Logger LOGGER;
	
	public static void main(String[] args)
	{
		System.out.println("Initializing GlobalRegistry");
		
		Config config = Config.getInstance();
		
		Options options = new Options();
		
		OptionBuilder.withLongOpt("help");
		OptionBuilder.withDescription("displays help on cli parameters");
		Option helpOption = OptionBuilder.create("h");
		
		OptionBuilder.withLongOpt("port_rest");
		OptionBuilder.withDescription("sets the port for the REST interface [" + config.getPortREST() + "]");
		OptionBuilder.hasArg();
		Option portRESTOption = OptionBuilder.create("p");
		
		OptionBuilder.withLongOpt("network_interface");
		OptionBuilder.withDescription("sets the network interface [" + config.getNetworkInterface() + "]");
		OptionBuilder.hasArg();
		Option networkInterfaceOption = OptionBuilder.create("n");
		
		OptionBuilder.withLongOpt("log_path");
		OptionBuilder.withDescription("sets the directory for the log files [" + config.getLogPath() + "]");
		OptionBuilder.hasArg();
		Option logPathOption = OptionBuilder.create("l");
		
		OptionBuilder.withLongOpt("connect_node");
		OptionBuilder.withDescription("sets the GReg node to connect to [" + config.getConnectNode() + "]");
		OptionBuilder.hasArg();
		Option connectNodeOption = OptionBuilder.create("c");
		
		options.addOption(helpOption);
		options.addOption(portRESTOption);
		options.addOption(networkInterfaceOption);
		options.addOption(logPathOption);
		options.addOption(connectNodeOption);
		
		// parse comman line parameters
		CommandLineParser parser = new BasicParser();
		
		try
		{
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("h"))
			{
				HelpFormatter formater = new HelpFormatter();
				formater.printHelp("GReg help", options);
				System.exit(0);
			}
			
			if(cmd.hasOption("p"))
			{
				config.setPortREST(Integer.parseInt(cmd.getOptionValue("p"))); // TODO check for valid values
			}
			if(cmd.hasOption("n"))
			{
				config.setNetworkInterface(cmd.getOptionValue("n")); // TODO check for valid values
			}
			if(cmd.hasOption("l"))
			{
				config.setLogPath(cmd.getOptionValue("l")); // TODO check for valid values
			}
			if(cmd.hasOption("c"))
			{
				config.setConnectNode(cmd.getOptionValue("c")); // TODO check for valid values
			}
			
			System.out.println("\n-----Configuration: ");
			System.out.println("connectNode: " + config.getConnectNode());
			System.out.println("portREST: " + config.getPortREST());
			System.out.println("networkInterface: " + config.getNetworkInterface());
			System.out.println("logPath: " + config.getLogPath() + "\n-----");
			
			// setup logging
			System.setProperty("loginfofile", Config.getInstance().getLogPath() + "log-info.log");
			System.setProperty("logdebugfile", Config.getInstance().getLogPath() + "log-debug.log");
			LOGGER = LoggerFactory.getLogger(GlobalRegistryServer.class);
			
			LOGGER.info(Config.getInstance().getProductName() + " "
					+ Config.getInstance().getVersionName() + " "
					+ Config.getInstance().getVersionCode());
			
			LOGGER.info("Build #" + Config.getInstance().getVersionNumber() + " ("
					+ Config.getInstance().getVersionDate() + ")\n");
		}
		catch (ParseException e)
		{
			System.out.println("Wrong parameter. Error: " + e.getMessage());
		}
		
		try
		{
			LOGGER.info("initializing DHT... ");
			
			DHTManager.getInstance().initDHT();
			LOGGER.info("DHT initialized successfully");
		
			LOGGER.info("initializing Global Registry server... ");
			
			//JettyServer.start();
			System.getProperties().put("server.port", 5002); //<-- what was that!?
			SpringApplication.run(GlobalRegistryServer.class, args);
		}
		catch (Exception e)
		{
			LOGGER.info("failed!");
			e.printStackTrace();
		}
	}
	
	@Override
	public void init(DaemonContext daemonContext) throws DaemonInitException, Exception
	{
		//System.out.println("deamon: init()");
		String arguments[] = daemonContext.getArguments();
		System.out.println(arguments);
		GlobalRegistryServer.main(arguments);
	}
	
	@Override
	public void start() throws Exception
	{
		//System.out.println("deamon: start()");
	}
	
	@Override
	public void stop() throws Exception
	{
		//System.out.println("deamon: exception()");
	}
	
	@Override
	public void destroy()
	{
		//System.out.println("deamon: destroy()");
	}
}