package eu.rethink.globalregistry.test;

import org.json.JSONArray;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.Base64UrlCodec;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import eu.rethink.globalregistry.model.GUID;
import eu.rethink.globalregistry.util.ECDSAKeyPairManager;

public class TestDataset
{
	protected static String gRegNode1 = "130.149.22.133:5002";
	protected static String gRegNode2 = "130.149.22.134:5002";
	protected static String gRegNode3 = "130.149.22.135:5002";
	
	public static void main(String args[])
	{
		try
		{
			//////////////////////////////////////////////////
			
			System.out.print("creating new ECDSA keypair... ");
			
			KeyPair keypair = ECDSAKeyPairManager.createKeyPair();
			String publicKeyString = ECDSAKeyPairManager.encodePublicKey(keypair.getPublic());
			String privateKeyString = ECDSAKeyPairManager.encodePrivateKey(keypair.getPrivate());
			
			String salt = "SpHuXwEGwrNcEcFoNS8Kv79PyGFlxi1v";
			
			System.out.println("ok");
			System.out.println("\n  [ PrivateKey: " + privateKeyString + " ]");
			System.out.println("  [ PublicKey: " + publicKeyString + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("creating new JSONObject... ");
			
			JSONObject json = new JSONObject();
			json.put("salt", salt);
			json.put("userIDs", new JSONArray("[\"reTHINK://sebastian.goendoer.net/\",\"reTHINK://facebook.com/fluffy123\"]"));
			json.put("lastUpdate", "2015-09-24T08:24:27+00:00");
			json.put("timeout", "2026-09-24T08:24:27+00:00");
			json.put("publicKey", publicKeyString);
			json.put("active", 1);
			json.put("revoked", 0);
			json.put("guid", GUID.createGUID(publicKeyString, salt));
			
			System.out.println("ok");
			System.out.println("\n  [ JSON: " + json.toString() + "]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("encoding to base64URL... ");
			
			String encodedClaim = new String(Base64UrlCodec.BASE64URL.encode(json.toString()));
			
			System.out.println("ok");
			System.out.println("\n  [ encoded: " + encodedClaim + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("creating JWT... ");
			String jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, keypair.getPrivate()).compact();
			
			System.out.println("ok");
			System.out.println("\n  [ jwt: " + jwt + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("Writing new JWT to GlobalRegistry [Node1] (PUT new dataset)... ");
			
			GlobalRegistryAPI.putData(gRegNode1, json.getString("guid"), jwt);
			
			System.out.println("ok");
			
			//////////////////////////////////////////////////
			
			System.out.print("Fetching JWT from GlobalRegistry [Node2] (GET existing dataset)... ");
			
			jwt = GlobalRegistryAPI.getData(gRegNode2, json.getString("guid"));
			
			System.out.println("ok");
			System.out.println("\n  [ jwt: " + jwt + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.println("verifying JWT... ");
			
			JSONObject jwtPayload = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
			
			encodedClaim = jwtPayload.getString("data");
			
			String decodedClaim = Base64UrlCodec.BASE64URL.decodeToString(encodedClaim);
			
			json = new JSONObject(decodedClaim);
			
			PublicKey publicKey = ECDSAKeyPairManager.decodePublicKey(json.getString("publicKey"));
			
			try
			{
				Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
				
				System.out.println("successfully verified!");
			}
			catch (SignatureException e)
			{
				System.out.println("verification failed!");
			}
			
			//////////////////////////////////////////////////
						
			System.out.print("editing Dataset... ");
			
			json = new JSONObject();
			json.put("salt", salt);
			json.put("userIDs", new JSONArray("[\"reTHINK://sebastian.goendoer.net/\",\"reTHINK://facebook.com/fluffy123\"]"));
			json.put("lastUpdate", "2015-09-24T08:24:27+00:00");
			json.put("timeout", "2027-09-24T08:24:27+00:00");
			json.put("publicKey", publicKeyString);
			json.put("active", 1);
			json.put("revoked", 0);
			json.put("guid", GUID.createGUID(publicKeyString, salt));
			
			System.out.println("ok");
			System.out.println("\n  [ JSON: " + json.toString() + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("encoding to base64URL... ");
			
			encodedClaim = new String(Base64UrlCodec.BASE64URL.encode(json.toString()));
			
			System.out.println("ok");
			System.out.println("\n  [ encoded: " + encodedClaim + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("creating JWT... ");
			jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, keypair.getPrivate()).compact();
			
			System.out.println("ok");
			System.out.println("\n  [ jwt: " + jwt + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("Writing edited JWT to GlobalRegistry [Node3] (PUT existing dataset)... ");
			
			GlobalRegistryAPI.putData(gRegNode3, json.getString("guid"), jwt);
			
			System.out.println("ok");
			
			//////////////////////////////////////////////////
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage() + "\n");
			e.printStackTrace();
		}
	}
}