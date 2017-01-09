package com.MVC;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.MVC.configuration.Configuration;
import com.MVC.dht.DHTManager;
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
 * Created by Half-Blood on 1/4/2017.
 */
@SpringBootApplication
public class GlobalRegistryServer implements Daemon {
     private static Logger LOGGER;

    public static void main(String[] args) {
        // init SONIC, configure
        Options options = new Options();

        OptionBuilder.withLongOpt("help");
        OptionBuilder.withDescription("displays help on cli parameters");
        Option helpOption = OptionBuilder.create("h");

        OptionBuilder.withLongOpt("cfg");
        OptionBuilder
                .withDescription("required: give the path to the folder that contains greg.config");
        //OptionBuilder.isRequired();
        OptionBuilder.hasArg();
        Option cfgOption = OptionBuilder.create("c");

        options.addOption(helpOption);
        options.addOption(cfgOption);

        // parse comman line parameters
        CommandLineParser parser = new BasicParser();

        try{
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h"))
            {
                HelpFormatter formater = new HelpFormatter();
                formater.printHelp("gRegs help", options);
                System.exit(0);
            }

            String configFile;

            //configFile = Configuration.getInstance().getConfigFilename();

            if(cmd.hasOption("c"))
            {
                System.out.println(cmd.getOptionValue("c"));
                configFile = cmd.getOptionValue("c");
            }
            else
            {
                configFile = Configuration.getInstance().getConfigFilename();
            }

            System.out.println("Initializing GlobalRegistry - loading greg.config");
            String pathToConfig = configFile;
            //String fullPathToConfig = pathToConfig + File.separator + "greg.config";
            String fullPathToConfig = pathToConfig;
            System.out.println("Trying to load config file: " + fullPathToConfig);

            if(!Configuration.propFileExists(fullPathToConfig))
            {
                System.out.println("No configuration file found. Please create valid gsls.config and provide path to it via command line option -c.");
                System.exit(0);
            }

            Configuration.getInstance().loadConfigurationFile();

            // setup logging
            System.setProperty("loginfofile", Configuration.getInstance().getLogPath() + "log-info.log");
            System.setProperty("logdebugfile", Configuration.getInstance().getLogPath() + "log-debug.log");
             LOGGER = LoggerFactory.getLogger(GlobalRegistryServer.class);

            LOGGER.info(Configuration.getInstance().getProductName() + " "
                + Configuration.getInstance().getVersionName() + " "
                + Configuration.getInstance().getVersionCode());
            LOGGER.info("Build #" + Configuration.getInstance().getVersionNumber() + " ("
              + Configuration.getInstance().getVersionDate() + ")\n");
        } catch (ParseException e) {
            System.out.println("Wrong parameter. Error: " + e.getMessage());
        }
        LOGGER.info("initializing DHT... ");

        try{
            DHTManager.getInstance().initDHT();
            LOGGER.info("DHT initialized successfully");

            // init GlobalRegistry server
            LOGGER.info("initializing Global Registry server... ");

            //JettyServer.start();
            System.getProperties().put("server.port", 5002);
            SpringApplication.run(GlobalRegistryServer.class, args);
        }
        catch (Exception e)
        {
            LOGGER.info("failed!");
            e.printStackTrace();
        }
    }

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        System.out.println("deamon: init()");
        String arguments[] = daemonContext.getArguments();
        System.out.println(arguments);
        GlobalRegistryServer.main(arguments);
    }

    @Override
    public void start() throws Exception {
        System.out.println("deamon: start()");
    }

    @Override
    public void stop() throws Exception {
        System.out.println("deamon: exception()");
    }

    @Override
    public void destroy() {
        System.out.println("deamon: destroy()");
    }
}

