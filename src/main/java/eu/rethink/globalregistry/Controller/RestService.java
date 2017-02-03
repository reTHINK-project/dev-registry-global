package eu.rethink.globalregistry.Controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.Base64UrlCodec;
import net.tomp2p.peers.PeerAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import eu.rethink.globalregistry.configuration.Config;
import eu.rethink.globalregistry.dht.DHTManager;
import eu.rethink.globalregistry.dht.GUIDNotFoundException;
import eu.rethink.globalregistry.model.Dataset;
import eu.rethink.globalregistry.model.DatasetIntegrityException;
import eu.rethink.globalregistry.model.GUID;
import eu.rethink.globalregistry.util.ECDSAKeyPairManager;
import eu.rethink.globalregistry.util.IntegrityException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

/**
 * Main class for GlobalRegistry daemon
 * 
 * @date 03.02.2017
 * @version 2
 * @author Sebastian Göndör, Parth Singh
 */
@RestController
@RequestMapping("/")
public class RestService
{
	@Autowired
	private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> index() throws URISyntaxException
	{
		LOGGER.error("Incoming request: GET /");
		List<PeerAddress> AllNeighbors = DHTManager.getInstance().getAllNeighbors();
		
		JSONArray connectedNodes = new JSONArray();
		
		for (PeerAddress neighbor : AllNeighbors) {
			connectedNodes.put(neighbor.inetAddress().getHostAddress());
		}
		
		JSONObject version = new JSONObject();
		version.put("date", Config.getInstance().getVersionDate());
		version.put("version", Config.getInstance().getVersionName());
		version.put("build", Config.getInstance().getVersionNumber());
		
		JSONObject response = new JSONObject();
		response.put("Code", 200);
		response.put("Description", "OK");
		response.put("Value", "");
		response.put("version", version);
		response.put("connectedNodes", connectedNodes);
		
		return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
		
	}
	
	//STATUS: Index Function is working absolutely fine.
	
	@RequestMapping(value = "guid/{GUID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> getEntitybyGUID(@PathVariable("GUID") String GUID)
	{
		LOGGER.error("Incoming request: GET /guid/" + GUID);
		
		if(GUID == null)
		{
			// received get request for path /guid/, but no guid.
			JSONObject response = new JSONObject();
			
			response.put("Code", 400);
			response.put("Description", "Bad Request");
			response.put("Explanation", "GUID not specified in request URL");
			response.put("Value", "");
			
			return new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
		}
		else //if(GUID != null)
		{
			String jwt = null;
			
			try
			{
				// get JWT from DHT
				try
				{
					jwt = DHTManager.getInstance().get(GUID);
				}
				catch (GUIDNotFoundException e)
				{
					// tried to get dataset from dht, caught an exception
					JSONObject response = new JSONObject();
					
					response.put("Code", 404);
					response.put("Description", "Not found");
					response.put("Explanation", "GUID not found");
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.NOT_FOUND);
				}
				
				if(jwt == null)
				{
					// tried to get dataset from dht, found null. this should NEVER happen!
					JSONObject response = new JSONObject();
					
					response.put("Code", 404);
					response.put("Description", "Not found");
					response.put("Explanation", "GUID not found. DHT returned NULL.");
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.NOT_FOUND);
				}
				else //if(jwt != null)
				{
					// decode JWT
					JSONObject jwtPayload = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
					JSONObject data = new JSONObject(Base64UrlCodec.BASE64URL.decodeToString(jwtPayload.get("data").toString()));
					
					LOGGER.info("decoded JWT payload: " + data.toString());
					
					// verify dataset integrity
					try
					{
						Dataset.checkDatasetValidity(data);
					}
					catch (DatasetIntegrityException e)
					{
						// read jwt from dht, integrity check for the enclosed json failed
						LOGGER.error("Integrity Exception found for JWT: " + jwt + " e: " + e.getMessage());
						
						JSONObject response = new JSONObject();
						
						response.put("Code", 500);
						response.put("Description", "Internal Server Error");
						response.put("Explanation", "Malformed JWT found in DHT: " + jwt + " e: " + e.getMessage());
						response.put("Value", "");
						
						return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
					}
					
					// decode key
					PublicKey publicKey;
					
					try
					{
						publicKey = ECDSAKeyPairManager.decodePublicKey(data.getString("publicKey"));
					}
					catch (InvalidKeySpecException | NoSuchAlgorithmException e)
					{
						// got jwt from dht, tried to extract public key, failed while doing so
						LOGGER.error("Malformed public key found in DHT: " + jwt + " e: " + e.getMessage());
						
						JSONObject response = new JSONObject();
						
						response.put("Code", 500);
						response.put("Description", "Internal Server Error");
						response.put("Explanation", "Malformed public key found in DHT: " + jwt + " e: " + e.getMessage());
						response.put("Value", "");
						
						return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
					}
					
					// verify jwt
					try
					{
						Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
					}
					catch (MalformedJwtException | UnsupportedJwtException e)
					{
						// got jwt from dht, jwt seems to be malformed
						LOGGER.error("Malformed JWT found in DHT: " + jwt + " e: " + e.getMessage());
						
						JSONObject response = new JSONObject();
						
						response.put("Code", 500);
						response.put("Description", "Internal Server Error");
						response.put("Explanation", "Malformed JWT found in DHT: " + jwt + " e: " + e.getMessage());
						response.put("Value", "");
						
						return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
					}
					catch (SignatureException e)
					{
						// got jwt from dht, jwt signature check failed
						LOGGER.error("Malformed JWT found in DHT: " + jwt + e.getMessage());
						
						JSONObject response = new JSONObject();
						
						response.put("Code", 500);
						response.put("Description", "Internal Server Error");
						response.put("Explanation", "Malformed signature for JWT found in DHT: " + jwt + " e: " + e.getMessage());
						response.put("Value", "");
						
						return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
					}
					
					LOGGER.info("JWT for GUID " + GUID + " verified");
					
					JSONObject response = new JSONObject();
					
					response.put("Code", 200);
					response.put("Description", "OK");
					response.put("Value", jwt);
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
				}
			}
			catch(JSONException e)
			{
				// somewhere, a json exception was thrown
				LOGGER.error("Faulty JSON data in DHT: " + jwt + " e: " + e.getMessage());
				
				JSONObject response = new JSONObject();
				
				response.put("Code", 500);
				response.put("Description", "Internal Server Error");
				response.put("Explanation", "Faulty JSON data in DHT: " + jwt + " e: " + e.getMessage());
				response.put("Value", "");
				
				return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			catch (IOException | ClassNotFoundException e)
			{
				// somewhere, a more severe exception was thrown
				LOGGER.error("Internal Server Error: " + jwt + " e: "+ e.getMessage());
				
				JSONObject response = new JSONObject();
				
				response.put("Code", 500);
				response.put("Description", "Internal server error");
				response.put("Explanation", "Internal Server Error: " + jwt + " e: " + e.getMessage());
				response.put("Value", "");
				
				return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@RequestMapping(value = "guid/{GUID}", method = RequestMethod.PUT)
	public ResponseEntity<String> putdata(@RequestBody String jwt, @PathVariable("GUID") String GUID)
	{
		LOGGER.error("Incoming request: PUT /guid/" + GUID + " - JWT: " + jwt);
		
		JSONObject newData; // the new version of the jwt
		JSONObject existingData; // the already existing version (if there is any)
		
		PublicKey newDatasetPublicKey; // the public key of the NEW version
		
		try
		{
			// decode JWT
			JSONObject jwtPayload = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
			newData = new JSONObject(Base64UrlCodec.BASE64URL.decodeToString(jwtPayload.get("data").toString()));
			
			//LOGGER.info("decoded JWT payload: " + newData.toString());
			
			// verify dataset integrity
			try
			{
				Dataset.checkDatasetValidity(newData);
			}
			catch (DatasetIntegrityException e)
			{
				LOGGER.error("Integrity Exception found for received JWT: " + jwt + " e: " + e.getMessage());
				
				JSONObject response = new JSONObject();
				
				response.put("Code", 400);
				response.put("Description", "Bad Request");
				response.put("Explanation", "JWT is malformed: " + jwt + " e: " + e.getMessage());
				response.put("Value", "");
				
				return new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
			}
			
			// decode key
			try
			{
				newDatasetPublicKey = ECDSAKeyPairManager.decodePublicKey(newData.getString("publicKey"));
			}
			catch (InvalidKeySpecException | NoSuchAlgorithmException e)
			{
				LOGGER.error("Malformed public key found in JWT: " + jwt + " e: " + e.getMessage());
				
				JSONObject response = new JSONObject();
				
				response.put("Code", 400);
				response.put("Description", "Bad Request");
				response.put("Explanation", "Malformed public key found in JWT: " + jwt + " e: " + e.getMessage());
				response.put("Value", "");
				
				return new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
			}
			
			// verify jwt
			try
			{
				Jwts.parser().setSigningKey(newDatasetPublicKey).parseClaimsJws(jwt);
			}
			catch (MalformedJwtException | UnsupportedJwtException e)
			{
				LOGGER.error("Malformed JWT found in DHT: " + jwt + " e: " + e.getMessage());
				
				JSONObject response = new JSONObject();
				
				response.put("Code", 400);
				response.put("Description", "Bad Request");
				response.put("Explanation", "Malformed JWT: " + jwt + " e: " + e.getMessage());
				response.put("Value", "");
				
				return new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
			}
			catch (SignatureException e)
			{
				LOGGER.error("Malformed JWT found in DHT: " + jwt + e.getMessage());
				
				JSONObject response = new JSONObject();
				
				response.put("Code", 400);
				response.put("Description", "Bad Request");
				response.put("Explanation", "Malformed signature for JWT: " + jwt + " e: " + e.getMessage());
				response.put("Value", "");
				
				return new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
			}
			
			LOGGER.info("JWT for GUID " + GUID + " verified");
			
			// match new JWT to existing JWT
			String existingJWT = null;
			
			try
			{
				existingJWT = DHTManager.getInstance().get(GUID);
				
				// GUID found. Ergo, we are updating an existing dataset
				
				JSONObject jwtPayloadFromDHT = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(existingJWT.split("\\.")[1])));
				existingData = new JSONObject(Base64UrlCodec.BASE64URL.decodeToString(jwtPayloadFromDHT.get("data").toString()));
				//---
				
				// verify the existing dataset's integrity
				try
				{
					Dataset.checkDatasetValidity(existingData);
				}
				catch (DatasetIntegrityException e)
				{
					// tried to write dataset. found an existing one. the existing one failed the integrity test
					LOGGER.error("Integrity exception found for existing dataset: " + existingJWT + " e: " + e.getMessage());
					
					JSONObject response = new JSONObject();
					
					response.put("Code", 500);
					response.put("Description", "Internal Server Error");
					response.put("Explanation", "Malformed JWT found in DHT: " + existingJWT + " e: " + e.getMessage());
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				// decode key
				PublicKey existingDatasetPublicKey;
				
				try
				{
					existingDatasetPublicKey = ECDSAKeyPairManager.decodePublicKey(existingData.getString("publicKey"));
				}
				catch (InvalidKeySpecException | NoSuchAlgorithmException e)
				{
					// tried to write dataset. found an existing one. the public key of the existing one couldnt be extracted
					LOGGER.error("Malformed public key found in DHT: " + jwt + " e: " + e.getMessage());
					
					JSONObject response = new JSONObject();
					
					response.put("Code", 500);
					response.put("Description", "Internal Server Error");
					response.put("Explanation", "Malformed public key found in DHT: " + jwt + " e: " + e.getMessage());
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				// verify jwt
				try
				{
					Jwts.parser().setSigningKey(existingDatasetPublicKey).parseClaimsJws(jwt);
				}
				catch (MalformedJwtException | UnsupportedJwtException e)
				{
					// tried to write dataset. found an existing one. the existing one seems to be malformed jwt
					LOGGER.error("Malformed JWT found in DHT: " + jwt + " e: " + e.getMessage());
					
					JSONObject response = new JSONObject();
					
					response.put("Code", 500);
					response.put("Description", "Internal Server Error");
					response.put("Explanation", "Malformed JWT found in DHT: " + jwt + " e: " + e.getMessage());
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
				catch (SignatureException e)
				{
					// tried to write dataset. found an existing one. the signature check of the existing one failed
					LOGGER.error("Malformed JWT found in DHT: " + jwt + e.getMessage());
					
					JSONObject response = new JSONObject();
					
					response.put("Code", 500);
					response.put("Description", "Internal Server Error");
					response.put("Explanation", "Malformed signature for JWT found in DHT: " + jwt + " e: " + e.getMessage());
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				try
				{
					// verify that GUIDs are matching
					if(!newData.getString("guid").equals(existingData.getString("guid")))
						throw new IntegrityException("GUIDs are not matching!");
				}
				catch (IntegrityException e)
				{
					// tried to write dataset, found an existing one. GUIDs do not match. Should NEVER happen!
					JSONObject response = new JSONObject();
					
					response.put("Code", 400);
					response.put("Description", "Bad Request");
					response.put("Explanation", "GUIDs do not match: " + jwt + " e: " + e.getMessage());
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
				}
				
				// everything is fine. overwrite existing dataset with new one
				
				LOGGER.info("Dataset for GUID " + GUID + " updated: \n" + jwt);
				
				try
				{
					DHTManager.getInstance().put(GUID, jwt);
				}
				catch (IOException e)
				{
					// tried to write dataset, found an existing one. Encountered an IO error while overwriting the existing one
					JSONObject response = new JSONObject();
					
					response.put("Code", 500);
					response.put("Description", "Internal Server Error");
					response.put("Explanation", "Error while writing to DHT: " + jwt + " e: " + e.getMessage());
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				LOGGER.info("Dataset for [" + GUID + "] written to DHT: \n" + jwt);
				
				JSONObject response = new JSONObject();
				
				response.put("Code", 200);
				response.put("Description", "OK");
				response.put("Explanation", "Dataset for GUID " + GUID + " updated: " + jwt);
				response.put("Value", "");
				
				return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
			}
			catch (GUIDNotFoundException e)
			{
				// GUID not found. Ergo, we are writing a new dataset to the DHT
				
				// TODO try/catch with error handling here!
				try
				{
					DHTManager.getInstance().put(GUID, jwt);
				}
				catch (IOException e1)
				{
					// tried to write dataset, found an existing one. Encountered an IO error while overwriting the existing one
					JSONObject response = new JSONObject();
					
					response.put("Code", 500);
					response.put("Description", "Internal Server Error");
					response.put("Explanation", "Error while writing to DHT: " + jwt + " e: " + e1.getMessage());
					response.put("Value", "");
					
					return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				LOGGER.info("Dataset for [" + GUID + "] written to DHT: \n" + jwt);
				
				JSONObject response = new JSONObject();
				
				response.put("Code", 200);
				response.put("Description", "OK");
				response.put("Explanation", "Dataset for GUID " + GUID + " written to DHT: \n" + jwt);
				response.put("Value", "");
				
				return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
			}
		}
		catch(JSONException e)
		{
			// somewhere, a json exception was thrown
			LOGGER.error("Faulty JSON data in DHT: " + jwt + " e: " + e.getMessage());
			
			JSONObject response = new JSONObject();
			
			response.put("Code", 500);
			response.put("Description", "Internal Server Error");
			response.put("Explanation", "Faulty JSON data in DHT: " + jwt + " e: " + e.getMessage());
			response.put("Value", "");
			
			return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch (IOException | ClassNotFoundException e)
		{
			// somewhere, a more severe exception was thrown
			LOGGER.error("Internal Server Error: " + jwt + " e: "+ e.getMessage());
			
			JSONObject response = new JSONObject();
			
			response.put("Code", 500);
			response.put("Description", "Internal server error");
			response.put("Explanation", "Internal Server Error: " + jwt + " e: " + e.getMessage());
			response.put("Value", "");
			
			return new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Easteregg. Just returning "I'm a teapot" as of RFC #2324
	 * 
	 */
	@RequestMapping(value = "teapot", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> teapot() throws URISyntaxException
	{
		LOGGER.error("Incoming request: GET /teapot");
		
		JSONObject response = new JSONObject();
		
		response.put("Code", 418);
		response.put("Description", "I'm a teapot");
		response.put("Value", "See RFC #2324");
		
		return new ResponseEntity<String>(response.toString(), HttpStatus.I_AM_A_TEAPOT);
		
	}
}