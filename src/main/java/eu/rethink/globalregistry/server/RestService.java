package eu.rethink.globalregistry.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rethink.globalregistry.dht.DHTManager;
import eu.rethink.globalregistry.model.GUID;
import eu.rethink.globalregistry.util.IntegrityException;
import eu.rethink.globalregistry.util.KeyPairManager;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.Base64UrlCodec;

/**
 * RestSerivce of GlobalRegistry.
 * 
 * @author Felix Beierle, Sebastian G�nd�r
 *
 */
@Path("/")
public class RestService
{
	private static final Logger	LOGGER	= LoggerFactory.getLogger(RestService.class);
	
	@GET
	@Produces("application/json")
	public String index() throws URISyntaxException
	{
		// TODO multiple requests in one?
		LOGGER.error("GET Request without GUID received");
		JSONObject outerJson = new JSONObject(ResponseFactory.createStatusResponse());
		return outerJson.toString();
	}
	
	@GET
	@Produces("application/json")
	@Path("guid/{guid}")
	public String getData(@PathParam("guid") String guid)
	{
		LOGGER.info("GET Request received for GUID " + guid);
		// TODO verify format of GlobalID
		
		JSONObject jsonResponse = new JSONObject(ResponseFactory.createDataNotFoundResponse());
		
		if(guid != null)
		{
			// retrieve data from TomP2P
			try
			{
				String jwt = DHTManager.getInstance().get(guid);
				
				if(jwt != null)
				{
					// TODO finish check of "active" flag and add handling of migrating records (active=2)
					// check if social record is inactive and then return
					// n/a
					try
					{
						jsonResponse = new JSONObject(ResponseFactory.createOKResponse());
						jsonResponse.put("data", jwt);
					}
					catch (JSONException e)
					{
						LOGGER.error("Faulty data in DHT! This should not happen! " + e.getMessage());
					}
				}
			}
			catch (ClassNotFoundException | IOException e)
			{
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
	//@Consumes("application/json") // there is no application/json+jwt content type yet
	public String putData(String jwt, @PathParam("guid") String guid)
	{
		LOGGER.info("PUT Request received: " + jwt);
		
		JSONObject data; // the new version of the jwt
		JSONObject existingData; // the already existing version (if there is any)
		String guidFromDataset; // the guid of the jwt
		
		PublicKey publicKey; // the public key of the NEW version
		
		try
		{
// verification of passed JWT
			// get payload from jwt
			JSONObject jwtHeader = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[0])));
			LOGGER.info("header: " + jwtHeader.toString());
			
			// step by step:
			JSONObject jwtPayload = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
			LOGGER.info("payload: " + jwtPayload.toString());
			
			// the data claim is a base64url-encoded json object
			data = new JSONObject(Base64UrlCodec.BASE64URL.decodeToString(jwtPayload.get("data").toString()));
			LOGGER.info("decoded payload: " + data.toString());
			
			// extract public key for signature verification
			publicKey = KeyPairManager.decodePublicKey(data.getString("publicKey")); // TODO build key from string
			
			// verify jwt
			Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
			LOGGER.info("token verified");
			
			// verify GUID
			if(!data.getString("guid").equals(GUID.createGUID(data.getString("publicKey"), data.getString("salt"))))
				throw new IntegrityException("GUID is invalid!");
			
			// get the already existing jwt from the DHT
			if(!guid.equals(data.getString("guid")))
				throw new IntegrityException("GUID mismatch!");
			
			// TODO verify data of jwt claim
			
			// if no exception has been thrown until here, the jwt signature has been verified
			
// verification of JWT claim "data"
			
// check for an already existing jwt in the DHT
			
			String dhtResult = DHTManager.getInstance().get(guid);
			
			if(dhtResult != null)
			{
				
// updating an existing dataset
				
				JSONObject jwtPayloadFromDHT = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(dhtResult.split("\\.")[1])));
				
				existingData = jwtPayloadFromDHT.getJSONObject("data");
				
				// TODO also check the validity of THIS jwt and payload!
				
				// verify that GUIDs are matching
				if(!data.getString("guid").equals(existingData.getString("guid")))
					throw new IntegrityException("GUIDs are not matching!");
				
				LOGGER.info("Dataset for [" + guid + "] updated: \n" + jwt);
				
				// here, everything is alright. so we write the jwt to the dht
				DHTManager.getInstance().put(guid, jwt);
				
				JSONObject jsonResponse = new JSONObject(ResponseFactory.createOKResponse());
				return jsonResponse.toString();
			}
			else
			{
				
// writing a new dataset
				
				// in this case, there is no dataset for this GUID in the DHT
				LOGGER.info("Dataset for [" + guid + "] written to DHT: \n" + jwt);
				
				DHTManager.getInstance().put(guid, jwt);
				
				JSONObject jsonResponse = new JSONObject(ResponseFactory.createOKResponse());
				return jsonResponse.toString();
			}
		}
		catch (UnsupportedJwtException | MalformedJwtException e)
		{
			LOGGER.error("Malformed JWT Exception: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
		catch (IntegrityException e)
		{
			LOGGER.error("Integrity Exception: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
		catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException | ClassNotFoundException | IOException e)
		{
			LOGGER.error("Error while putting data into DHT: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
	}

	/**
	 * Takes valid JSON-file, verifies it and puts it into DHT.
	 * 
	 * @param socialRecord
	 * @return
	 */
	/*@POST
	//@Consumes("application/json") // there is no application/json+jwt content type yet
	public String postData(String jwt)
	{
		LOGGER.info("POST Request received: " + jwt);
		
		JSONObject data; // payload data from the jwt
		String guid; // the guid (lookup key)
		
		PublicKey publicKey; // the account public key of the socialRecord
		
		try
		{
			// get payload from jwt
			JSONObject jwtHeader = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[0])));
			LOGGER.info("header: " + jwtHeader.toString());
			
			// step by step:
			JSONObject jwtPayload = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
			LOGGER.info("payload: " + jwtPayload.toString());
			
			data = new JSONObject(jwtPayload.get("data").toString());
			
			// extract public key for signature verification
			publicKey = KeyPairManager.decodePublicKey(data.getString("publicKey"));
			
			// verify jwt
			Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
			LOGGER.info("token verified");
			
			// verify GID
			guid = data.getString("guid");
			if(!guid.equals(GUID.createGUID(data.getString("publicKey"), data.getString("salt"))))
				throw new IntegrityException("GUID is invalid!");
			
			// TODO verify data of social record
			
			// if no exception has been thrown until here, the jwt signature has been verified
			
			// check for existing socialRecord
			String dhtResult = DHTManager.getInstance().get(guid);
			if(dhtResult != null)
			{
				// in this case, a socialRecord for this globalID exists
				ResponseFactory response = new ResponseFactory();
				response.setResponseCode(ResponseFactory.CODE_INVALID_REQUEST);
				response.setMessage(ResponseFactory.MESSAGE_INVALID_PUT_ALREADY_EXISTS);
				
				JSONObject jsonResponse = new JSONObject(response);
				return jsonResponse.toString();
			}
			else
			{
				// in this case, there is no socialRecord for this globalID in the DHT
				LOGGER.info("Dataset for [" + guid + "] written to DHT: \n" + jwt);
				
				DHTManager.getInstance().put(guid, jwt);
				
				JSONObject jsonResponse = new JSONObject(ResponseFactory.createOKResponse());
				return jsonResponse.toString();
			}
		}
		catch (UnsupportedJwtException | MalformedJwtException e)
		{
			LOGGER.error("Malformed JWT Exception: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
		catch (IntegrityException e)
		{
			LOGGER.error("Integrity Exception: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
		catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException | ClassNotFoundException | IOException e)
		{
			LOGGER.error("Error while putting data into DHT: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
	}*/

	/**
	 * Takes valid JSON-file, verifies it and puts it into DHT; for updates and
	 * de-activation.
	 * 
	 * @param dataset
	 * @return
	 */
	/*@PUT
	//@Consumes("application/json") // there is no application/json+jwt content type yet
	public String putData(String jwt)
	{
		LOGGER.info("PUT Request received: " + jwt);
		
		JSONObject data; // the new version of the socialRecord
		JSONObject existingData; // the already existing version
		String guid; // the globalID of the socialRecord
		
		PublicKey publicKey; // the account public key of the NEW version
		
		try
		{
			// get payload from jwt
			JSONObject jwtHeader = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[0])));
			LOGGER.info("header: " + jwtHeader.toString());
			
			// step by step:
			JSONObject jwtPayload = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
			LOGGER.info("payload: " + jwtPayload.toString());
			
			data = new JSONObject(jwtPayload.get("data").toString());
			
			// extract public key for signature verification
			publicKey = KeyPairManager.decodePublicKey(data.getString("publicKey"));
			
			// verify jwt
			Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
			LOGGER.info("token verified");
			
			// verify GID
			if(!data.getString("guid").equals(GUID.createGUID(data.getString("publicKey"), data.getString("salt"))))
				throw new IntegrityException("GUID is invalid!");
			
			// TODO verify data of social record
			
			// if no exception has been thrown until here, the jwt signature has been verified
			
			// get the already existing social record
			guid = data.getString("guid");
			
			String dhtResult = DHTManager.getInstance().get(guid);
			if(dhtResult == null)
			{
				// in this case, no data could be retrieved from the DHT for the given globalID
				JSONObject jsonResponse = new JSONObject(ResponseFactory.createDataNotFoundResponse());
				return jsonResponse.toString();
			}
			else
			{
				// in this case, we got a jwt from the DHT
				JSONObject jwtPayloadFromDHT = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(dhtResult.split("\\.")[1])));
				
				existingData = jwtPayloadFromDHT.getJSONObject("data");
			}
			
			// verify that GIDs are matching
			if(!data.getString("guid").equals(existingData.getString("guid")))
				throw new IntegrityException("GUIDs are not matching!");
			
			// here, everything is alright. so we write the jwt to the dht
			DHTManager.getInstance().put(guid, jwt);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createOKResponse());
			return jsonResponse.toString();
		}
		catch (UnsupportedJwtException | MalformedJwtException e)
		{
			LOGGER.error("Malformed JWT Exception: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
		catch (IntegrityException e)
		{
			LOGGER.error("Integrity Exception: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
		catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException | ClassNotFoundException | IOException e)
		{
			LOGGER.error("Error while putting data into DHT: " + e.getMessage() + "\n" + e);
			
			JSONObject jsonResponse = new JSONObject(ResponseFactory.createInvalidRequestResponse());
			return jsonResponse.toString();
		}
	}*/
}