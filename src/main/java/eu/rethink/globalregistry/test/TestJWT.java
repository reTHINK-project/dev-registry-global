package eu.rethink.globalregistry.test;

import org.json.JSONArray;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.Base64UrlCodec;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import eu.rethink.globalregistry.model.GUID;
import eu.rethink.globalregistry.util.ECDSAKeyPairManager;

public class TestJWT
{
	protected static String gRegNode1 = "130.149.22.133:5002";
	
	public static void main(String args[])
	{
		try
		{
			String privateKeyString = "-----BEGIN PRIVATE KEY-----MEUCAQAwEAYHKoZIzj0CAQYFK4EEAAoELjAsAgEBBCDZRNY2W/5YH181gGXRnuzID3Y8oOpo+76yShhH3sdOiKEFAwMAAAA=-----END PRIVATE KEY-----";
			String publicKeyString = "-----BEGIN PUBLIC KEY-----MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEWVO21gYy+rGZt3xfiRoQULGJlDiZrAo+xxms/KYe6Oll9TOFkOPK8EgMNf0JfvWMImlPEpqRF39Xn4wOZ6tlQw==-----END PUBLIC KEY-----";
			String datasetString = "{\"userIDs\":[],\"publicKey\":\"-----BEGIN PUBLIC KEY-----MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEWVO21gYy+rGZt3xfiRoQULGJlDiZrAo+xxms/KYe6Oll9TOFkOPK8EgMNf0JfvWMImlPEpqRF39Xn4wOZ6tlQw==-----END PUBLIC KEY-----\",\"salt\":\"wq/dOFZbAfJ4vIpVE82DVIQP7FPeJd1Fr00j1pOavoo=\",\"guid\":\"76bfnrt1n_RSK68Cr7Tbzze_sL0OxfrQn0vmYsxcz98\",\"lastUpdate\":\"2016-04-11T09:26:10.153Z\",\"timeout\":\"2026-04-11T09:26:10.153Z\",\"active\":1,\"revoked\":0}";
			
			//////////////////////////////////////////////////
			
			System.out.print("creating keys from String... ");
			
			PrivateKey privateKey = ECDSAKeyPairManager.decodePrivateKey(privateKeyString);
			PublicKey publicKey = ECDSAKeyPairManager.decodePublicKey(publicKeyString);
			
			//String publicKeyString = ECDSAKeyPairManager.encodePublicKey(keypair.getPublic());
			//String privateKeyString = ECDSAKeyPairManager.encodePrivateKey(keypair.getPrivate());
			
			System.out.println("ok");
			System.out.println("\n  [ PrivateKey: " + privateKeyString + " ] \n expected: " + ECDSAKeyPairManager.encodePrivateKey(privateKey));
			System.out.println("  [ PublicKey: " + publicKeyString + " ] \n expected: " + ECDSAKeyPairManager.encodePublicKey(publicKey) + "\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("creating JSONObject from String... ");
			
			JSONObject json = new JSONObject(datasetString);
			System.out.println("ok");
			System.out.println("\n  [ JSON: " + json.toString() + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("encoding to base64URL... ");
			
			String encodedClaim = new String(Base64UrlCodec.BASE64URL.encode(json.toString()));
			
			System.out.println("ok");
			System.out.println("\n  [ encoded: " + encodedClaim + " ]\n");
			
			//////////////////////////////////////////////////
						
			System.out.print("creating JWT... ");
			String jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, privateKey).compact();
			
			System.out.println("ok");
			System.out.println("\n  [ jwt: " + jwt + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("Writing new JWT to GlobalRegistry [Node1] (PUT new dataset)... ");
			
			GlobalRegistryAPI.putData(gRegNode1, json.getString("guid"), jwt);
			
			System.out.println("ok");
			
			//////////////////////////////////////////////////
			
			System.out.print("Fetching JWT from GlobalRegistry [Node1] (GET existing dataset)... ");
			
			jwt = GlobalRegistryAPI.getData(gRegNode1, json.getString("guid"));
			
			System.out.println("ok");
			System.out.println("\n  [ jwt: " + jwt + " ]\n");
			
			//////////////////////////////////////////////////
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage() + "\n");
			e.printStackTrace();
		}
	}
}