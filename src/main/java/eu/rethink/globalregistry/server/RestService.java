package eu.rethink.globalregistry.server;

import java.util.List;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.ws.rs.GET;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rethink.globalregistry.configuration.Configuration;

import eu.rethink.globalregistry.dao.AccessManager;

import eu.rethink.globalregistry.dht.DHTManager;
import eu.rethink.globalregistry.model.Dataset;
import eu.rethink.globalregistry.model.DatasetIntegrityException;
import eu.rethink.globalregistry.model.GUID;
import eu.rethink.globalregistry.util.ECDSAKeyPairManager;
import eu.rethink.globalregistry.util.IntegrityException;
import eu.rethink.globalregistry.util.XSDDateTime;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.Base64UrlCodec;
import net.tomp2p.peers.PeerAddress;

/**
 * RestSerivce of GlobalRegistry.
 * 
 * @author Felix Beierle, Sebastian G�nd�r
 *
 */
@Path("/")
public class RestService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);

	@GET
	@Produces("application/json")
	public String index() throws URISyntaxException {
		// TODO multiple requests in one?
		LOGGER.error("GET Request without GUID received");
		JSONObject outerJson = new JSONObject(ResponseFactory.createStatusResponse());
		List<PeerAddress> AllNeighbors = DHTManager.getInstance().getAllNeighbors();

		JSONArray connectedNodes = new JSONArray();
		for (PeerAddress neighbor : AllNeighbors) {
			connectedNodes.put(neighbor.inetAddress().getHostAddress());
		}

 		outerJson.put("version", Configuration.getInstance().getVersionName());
 		outerJson.put("build", Configuration.getInstance().getVersionNumber());
 		outerJson.put("connectedNodes", connectedNodes);
 		
		return outerJson.toString();
	}

	@GET
	@Produces("application/json")
	@Path("guid/{guid}")
	public String getData(@PathParam("guid") String guid) {
		LOGGER.info("GET Request received for GUID " + guid);
		// TODO verify format of GlobalID

		JSONObject jsonResponse = new JSONObject(ResponseFactory.createDataNotFoundResponse());

		if (guid != null) {
			// retrieve data from TomP2P
			try {
				String jwt = DHTManager.getInstance().get(guid);

				if (jwt != null) {
					// TODO finish check of "active" flag
					// TODO check for outdated data
					try {
						JSONObject data;
						PublicKey publicKey;

						JSONObject jwtHeader = new JSONObject(
								new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[0])));
						LOGGER.info("header: " + jwtHeader.toString());

						// step by step:
						JSONObject jwtPayload = new JSONObject(
								new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
						LOGGER.info("payload: " + jwtPayload.toString());

						// the data claim is a base64url-encoded json object
						data = new JSONObject(
								Base64UrlCodec.BASE64URL.decodeToString(jwtPayload.get("data").toString()));
						LOGGER.info("decoded payload: " + data.toString());

						Dataset.checkDatasetValidity(data);

						// extract public key for signature verification
						publicKey = ECDSAKeyPairManager.decodePublicKey(data.getString("publicKey")); // TODO
																										// build
																										// key
																										// from
						//TODO : time out 																				// string

						// verify jwt
						Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
						LOGGER.info("token verified");

						jsonResponse = new JSONObject(ResponseFactory.createOKResponse());
						jsonResponse.put("data", jwt);
					} catch (JSONException | DatasetIntegrityException e) {
						LOGGER.error("Faulty data in DHT! This should not happen! " + e.getMessage());
					} catch (InvalidKeySpecException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
				}
			} catch (ClassNotFoundException | IOException e) {
				LOGGER.error("Error while getting data from DHT: " + e.getMessage());
			}
		}
		// respond with data from TomP2P
		return jsonResponse.toString();
	}

	/**
	 * Writes a JWT to the DHT, if valid
	 * 
	 * @param dataset
	 * @return
	 */
	@PUT
	@Path("guid/{guid}")
	// @Consumes("application/json") // there is no application/json+jwt content
	// type yet
	public String putData(String jwt, @PathParam("guid") String guid) {
		LOGGER.info("PUT Request received: " + jwt);

		JSONObject data; // the new version of the jwt
		JSONObject existingData; // the already existing version (if there is
									// any)
		PublicKey publicKey; // the public key of the NEW version
		AccessManager accessManager = new AccessManager();
		try {
			// verification of passed JWT
			// get payload from jwt
			JSONObject jwtHeader = new JSONObject(
					new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[0])));
			LOGGER.info("header: " + jwtHeader.toString());

			// step by step:
			JSONObject jwtPayload = new JSONObject(
					new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
			LOGGER.info("payload: " + jwtPayload.toString());

			// the data claim is a base64url-encoded json object
			data = new JSONObject(Base64UrlCodec.BASE64URL.decodeToString(jwtPayload.get("data").toString()));
			LOGGER.info("decoded payload: " + data.toString());

			Dataset.checkDatasetValidity(data);

			// extract public key for signature verification
			publicKey = ECDSAKeyPairManager.decodePublicKey(data.getString("publicKey")); // TODO
																							// build
																							// key
																							// from
																							// string

			// verify jwt
			Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
			LOGGER.info("token verified");

			// verify GUID
			if (!data.getString("guid").equals(GUID.createGUID(data.getString("publicKey"), data.getString("salt"))))
				throw new IntegrityException("GUID is invalid!");

			// get the already existing jwt from the DHT
			if (!guid.equals(data.getString("guid")))
				throw new IntegrityException("GUID mismatch!");

			// TODO verify data of jwt claim

			// if no exception has been thrown until here, the jwt signature has
			// been verified

			// verification of JWT claim "data"

			// check for an already existing jwt in the DHT

			String dhtResult = DHTManager.getInstance().get(guid);

			if (dhtResult != null) {

				// updating an existing dataset

				JSONObject jwtPayloadFromDHT = new JSONObject(
						new String(Base64UrlCodec.BASE64URL.decodeToString(dhtResult.split("\\.")[1])));

				existingData = new JSONObject(
						Base64UrlCodec.BASE64URL.decodeToString(jwtPayloadFromDHT.get("data").toString()));

				// TODO also check the validity of THIS jwt and payload!

				// verify that GUIDs are matching
				if (!data.getString("guid").equals(existingData.getString("guid")))
					throw new IntegrityException("GUIDs are not matching!");

				// get the Last update time and the Time out to compare them
				DateTime dataLastupdate = XSDDateTime.parseXSDDateTime(data.getString("lastUpdate"));
				DateTime existingDataLastupdate = XSDDateTime.parseXSDDateTime(existingData.getString("lastUpdate"));
				DateTime dataTimeout = XSDDateTime.parseXSDDateTime(data.getString("timeout"));
				DateTime existingDataTimeout = XSDDateTime.parseXSDDateTime(existingData.getString("timeout"));

				// see if Data set has a new version than the one in the rang
				if (dataLastupdate.compareTo(existingDataLastupdate) == 1
						|| dataTimeout.compareTo(existingDataTimeout) == 1) {

					// if yes we write the JWT to the DHT
					DHTManager.getInstance().put(guid, jwt);

					if (Configuration.getInstance().getStartDatabase() == 1) {

						// we check if the Data set is already in the db
						if (accessManager.findUserDataset(guid)) {
							// update if yes
							accessManager.updateUserDataset(guid, jwt);

						} else {
							// save if no
							accessManager.insertUserDataset(guid, jwt);
						}
					}
					// check if the Data set is a old version than the one in
					// the rang
				} else if (dataLastupdate.compareTo(existingDataLastupdate) == -1
						|| dataTimeout.compareTo(existingDataTimeout) == -1) {

					if (Configuration.getInstance().getStartDatabase() == 1) {

						// we check if the Data set is already in the db
						if (accessManager.findUserDataset(guid)) {
							// if yes delete it because its old version
							accessManager.deleteUserDataset(guid);
						}
					}
				}

				LOGGER.info("Dataset for [" + guid + "] updated: \n" + jwt);

				JSONObject jsonResponse = new JSONObject(ResponseFactory.createOKResponse());
				return jsonResponse.toString();
			} else {

				// writing a new dataset

				// in this case, there is no dataset for this GUID in the DHT
				LOGGER.info("Dataset for [" + guid + "] written to DHT: \n" + jwt);

				// write the JWT to the DHT
				DHTManager.getInstance().put(guid, jwt);
				// saving the new dataset in the db
				new AccessManager().insertUserDataset(guid, jwt);

				JSONObject jsonResponse = new JSONObject(ResponseFactory.createOKResponse());
				return jsonResponse.toString();
			}
		} catch (UnsupportedJwtException | MalformedJwtException e) {
			LOGGER.error("Malformed JWT Exception: " + e.getMessage() + "\n" + e);

			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		} catch (IntegrityException | DatasetIntegrityException e) {
			LOGGER.error("Integrity Exception: " + e.getMessage() + "\n" + e);

			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		} catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException | ClassNotFoundException
				| IOException e) {
			LOGGER.error("Error while putting data into DHT: " + ExceptionUtils.getStackTrace(e) + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return e.getMessage();
		}
	}
}