package com.MVC.Controller;

import com.MVC.Service.EntityService;
import com.MVC.configuration.Configuration;
import com.MVC.dht.DHTManager;
import com.MVC.model.Dataset;
import com.MVC.model.DatasetIntegrityException;
import com.MVC.model.GUIDs;
import com.MVC.util.ECDSAKeyPairManager;
import com.MVC.util.IntegrityException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

/**
 * Created by Half-Blood on 1/4/2017.
 */
@RestController
@RequestMapping("/")
public class RestService {

    @Autowired
    private EntityService entityService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> index() throws URISyntaxException {
        // TODO multiple requests in one?
        LOGGER.error("GET Request without GUID received");
        List<PeerAddress> AllNeighbors = DHTManager.getInstance().getAllNeighbors();

        JSONArray connectedNodes = new JSONArray();

        for (PeerAddress neighbor : AllNeighbors) {
            connectedNodes.put(neighbor.inetAddress().getHostAddress());
        }

        JSONObject version = new JSONObject();
        version.put("version", Configuration.getInstance().getVersionName());
        version.put("build", Configuration.getInstance().getVersionNumber());

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
    public ResponseEntity<String> getEntitybyGUID(@PathVariable("GUID") String GUID){
        LOGGER.info("GET Request received for GUID " + GUID);
        JSONObject response = new JSONObject();
        ResponseEntity<String> finalresponse;
        response.put("Code", 404);
        response.put("Description", "GUID not found");
        response.put("Value", "");
        finalresponse = new ResponseEntity<String>(response.toString(), HttpStatus.NOT_FOUND);
        if(GUID != null){
            try{
                //String jwt = DHTManager.getInstance().get(GUID);
                String jwt = entityService.getEntitybyGUID(GUID);
                if(jwt != null) {
                    try{
                        response.put("Code", 200);
                        response.put("Description", "OK");
                        response.put("Value", jwt);
                        finalresponse = new ResponseEntity<String>(response.toString(), HttpStatus.OK);
                    }catch(JSONException e){
                        LOGGER.error("Faulty data in DHT! This should not happen! " + e.getMessage());
                    }
                }

            }catch (IOException | ClassNotFoundException e){
                LOGGER.error("Error while getting data from DHT: " + e.getMessage());
                response.put("Code", 500);
                response.put("Description", "Internal server error");
                response.put("Value", "");
                finalresponse = new ResponseEntity<String>(response.toString(), HttpStatus.BAD_REQUEST);
            }
        }
        return finalresponse ;
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

            //String dhtResult = DHTManager.getInstance().get(guid);
            String dhtResult = entityService.getEntitybyGUID(GUID);

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
                //DHTManager.getInstance().put(GUID, jwt);
                entityService.updateEntity(jwt, GUID);
                response.put("Code", 200);
                response.put("Description", "OK");
                response.put("Value", "");
                return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
            }
            else{
                // writing a new dataset

                // in this case, there is no dataset for this GUID in the DHT
                //DHTManager.getInstance().put(GUID, jwt);
                entityService.insertEntity(jwt, GUID);
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

