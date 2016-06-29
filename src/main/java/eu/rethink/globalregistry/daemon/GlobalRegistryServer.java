package eu.rethink.globalregistry.daemon;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rethink.globalregistry.server.JettyServer;
import eu.rethink.globalregistry.util.XSDDateTime;
import io.jsonwebtoken.impl.Base64UrlCodec;
import eu.rethink.globalregistry.configuration.Configuration;
import eu.rethink.globalregistry.dao.AccessManager;
import eu.rethink.globalregistry.dht.DHTManager;
import eu.rethink.globalregistry.model.UsersDataset;

/**
 * Main class of the GlobalRegistryDaemon.
 *
 */
public class GlobalRegistryServer implements Daemon {
	private static Logger LOGGER;

	public static void main(String[] args) {

		String configFile = Configuration.getInstance().getConfigFilename();
		Configuration.getInstance().loadConfigurationFile(configFile);
		// setup logging
		System.setProperty("loginfofile", Configuration.getInstance().getLogPath() + "log-info.log");
		System.setProperty("logdebugfile", Configuration.getInstance().getLogPath() + "log-debug.log");
		LOGGER = LoggerFactory.getLogger(GlobalRegistryServer.class);

		LOGGER.info(Configuration.getInstance().getProductName() + " " + Configuration.getInstance().getVersionName()
				+ " " + Configuration.getInstance().getVersionCode());
		LOGGER.info("Build #" + Configuration.getInstance().getVersionNumber() + " ("
				+ Configuration.getInstance().getVersionDate() + ")\n");

		// init dht
		LOGGER.info("initializing DHT... ");
		try {
			DHTManager.getInstance().initDHT();
			LOGGER.info("DHT initialized successfully");

			// init GlobalRegistry server
			LOGGER.info("initializing Global Registry server... ");

			JettyServer.start();

			// put dataset
			if (Configuration.getInstance().getStartDatabase() == 1) {

				ArrayList<UsersDataset> UsersDatasetList = new AccessManager().getUsersDataset();
				Iterator<UsersDataset> list = UsersDatasetList.iterator();
				String guid;
				String jwt;
				while (list.hasNext()) {
					UsersDataset UsersDataset = list.next();

					guid = UsersDataset.getGuid();
					jwt = UsersDataset.getJwt();

					String dhtResult = DHTManager.getInstance().get(guid);

					if (dhtResult != null) {

						JSONObject jwtPayloadFromDHT = new JSONObject(
								new String(Base64UrlCodec.BASE64URL.decodeToString(dhtResult.split("\\.")[1])));

						JSONObject existingData = new JSONObject(
								Base64UrlCodec.BASE64URL.decodeToString(jwtPayloadFromDHT.get("data").toString()));

						// step by step:
						JSONObject jwtPayload = new JSONObject(
								new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
						LOGGER.info("payload: " + jwtPayload.toString());

						// the data claim is a base64url-encoded json object
						JSONObject data = new JSONObject(
								Base64UrlCodec.BASE64URL.decodeToString(jwtPayload.get("data").toString()));

						// get the Last update time and the Time out to compare
						// them
						DateTime dataLastupdate = XSDDateTime.parseXSDDateTime(data.getString("lastUpdate"));
						DateTime existingDataLastupdate = XSDDateTime
								.parseXSDDateTime(existingData.getString("lastUpdate"));
						DateTime dataTimeout = XSDDateTime.parseXSDDateTime(data.getString("timeout"));
						DateTime existingDataTimeout = XSDDateTime.parseXSDDateTime(existingData.getString("timeout"));

						// see if Data set has a new version than the one in the
						// rang
						if (dataLastupdate.compareTo(existingDataLastupdate) == 1
								|| dataTimeout.compareTo(existingDataTimeout) == 1) {

							// if yes we write the JWT to the DHT
							DHTManager.getInstance().put(guid, jwt);

							// check if the Data set is a old version than the
							// one in
							// the rang
						} else if (dataLastupdate.compareTo(existingDataLastupdate) == -1
								|| dataTimeout.compareTo(existingDataTimeout) == -1) {

							// if yes delete it from the db because its old
							// version
							new AccessManager().deleteUserDataset(guid);

						}

					} else {
						// in this case, there is no dataset for this GUID in
						// the DHT so we write the JWT to the DHT
						DHTManager.getInstance().put(guid, jwt);
					}
				}
			}
		} catch (

		Exception e)

		{
			LOGGER.info("failed!");
			e.printStackTrace();
		}

	}

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception {
		System.out.println("deamon: init()");
		String arguments[] = arg0.getArguments();
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