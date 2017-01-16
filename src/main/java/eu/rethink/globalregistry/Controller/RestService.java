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
import eu.rethink.globalregistry.model.Dataset;
import eu.rethink.globalregistry.model.DatasetIntegrityException;
import eu.rethink.globalregistry.model.GUIDs;
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
 * @date 16.01.2017
 * @version 1
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
		LOGGER.error("Incomin request: GET /");
		List<PeerAddress> AllNeighbors = DHTManager.getInstance().getAllNeighbors();
		
		JSONArray connectedNodes = new JSONArray();
		
		for (PeerAddress neighbor : AllNeighbors) {
			connectedNodes.put(neighbor.inetAddress().getHostAddress());
		}
		
		JSONObject version = new JSONObject();
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
			JSONObject response = new JSONObject();
			
			response.put("Code", 400);
			response.put("Description", "Bad Request");
			response.put("Explanation", "GUID not specified in request URL");
			response.put("Value", "");
			
			return new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
		}
		else //if(GUID != null)
		{
			String jwt = "";
			
			try
			{
				// get JWT from DHT
				jwt = DHTManager.getInstance().get(GUID);
				
				if(jwt == null)
				{
					JSONObject response = new JSONObject();
					
					response.put("Code", 404);
					response.put("Description", "Not found");
					response.put("Explanation", "GUID not found");
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
    public ResponseEntity<String> putdata(@RequestBody String jwt, @PathVariable("GUID") String GUID ){
        LOGGER.info("PUT Request received: " + jwt);
        JSONObject data; // the new version of the jwt
        JSONObject existingData; // the already existing version (if there is
        // any)
        String guidFromDataset; // the guid of the jwt

        PublicKey publicKey; // the public key of the NEW version
        JSONObject response = new JSONObject();
        try{
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
            publicKey = ECDSAKeyPairManager.decodePublicKey(data.getString("publicKey"));
            // TODO build key from string

            // verify jwt
            Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
            LOGGER.info("token verified");

            // verify GUID
            if (!data.getString("guid").equals(GUIDs.createGUID(data.getString("publicKey"), data.getString("salt"))))
                throw new IntegrityException("GUID is invalid!");

            // get the already existing jwt from the DHT
            if (!GUID.equals(data.getString("guid")))
                throw new IntegrityException("GUID mismatch!");


            // TODO verify data of jwt claim

            // if no exception has been thrown until here, the jwt signature has
            // been verified

            // verification of JWT claim "data"

            // check for an already existing jwt in the DHT

            String dhtResult = DHTManager.getInstance().get(GUID);

            if(dhtResult != null){
                // updating an existing dataset

                JSONObject jwtPayloadFromDHT = new JSONObject(
                        new String(Base64UrlCodec.BASE64URL.decodeToString(dhtResult.split("\\.")[1])));

                existingData = new JSONObject(
                        Base64UrlCodec.BASE64URL.decodeToString(jwtPayloadFromDHT.get("data").toString()));


                // TODO also check the validity of THIS jwt and payload!

                // verify that GUIDs are matching
                if (!data.getString("guid").equals(existingData.getString("guid")))
                    throw new IntegrityException("GUIDs are not matching!");
                LOGGER.info("Dataset for [" + GUID + "] updated: \n" + jwt);
                DHTManager.getInstance().put(GUID, jwt);
                response.put("Code", 200);
                response.put("Description", "OK");
                response.put("Value", "");
                return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
            }
            else{
                // writing a new dataset

                // in this case, there is no dataset for this GUID in the DHT
                DHTManager.getInstance().put(GUID, jwt);
                LOGGER.info("Dataset for [" + GUID + "] written to DHT: \n" + jwt);
                response.put("Code", 200);
                response.put("Description", "OK");
                response.put("Value", "");

                return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
            }
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            LOGGER.error("Malformed JWT Exception: " + e.getMessage() + "\n" + e);
            response.put("Code", 400);
            response.put("Description", "Invalid request");
            response.put("Value", "");
            return new ResponseEntity<String>(response.toString(), HttpStatus.NOT_FOUND);
        } catch (IntegrityException | DatasetIntegrityException | ClassNotFoundException | IOException e) {
            LOGGER.error("Integrity Exception: " + e.getMessage() + "\n" + e);
            response.put("Code", 400);
            response.put("Description", "Invalid request");
            response.put("Value", "");
            return new ResponseEntity<String>(response.toString(), HttpStatus.NOT_FOUND);
        } catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("Error while putting data into DHT: " + e.getMessage() + "\n" + e);
            response.put("Code", 500);
            response.put("Description", "Internal server error");
            response.put("Value", "");
            return new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
        }
    }
}

