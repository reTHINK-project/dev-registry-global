package eu.rethink.globalregistry.test;

import org.json.JSONArray;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.Base64UrlCodec;

import java.security.KeyPair;
import eu.rethink.globalregistry.model.GUID;
import eu.rethink.globalregistry.util.ECDSAKeyPairManager;

public class TestJWT
{
	protected static String gRegNode1 = "130.149.22.133:5002";
	
	public static void main(String args[])
	{
		try
		{
			//////////////////////////////////////////////////
			
			System.out.print("creating JSONObject from String... ");
			
			JSONObject json = new JSONObject("{\"userIDs\":[],\"publicKey\":\"-----BEGIN PUBLIC KEY-----MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEkIf9IjfhT6whPtZJayz8huQAR5C3A0HMFT5K0yR9JMdfZHDDSg/PGSpFPng0GzKzQmbQIJkjUvG8HERSnePC2A==-----END PUBLIC KEY-----\",\"salt\":\"on+YI40sHALyoTHYlNKUEPCRXZ+TC/+AOJ5f4LVeJDw=\",\"guid\":\"fmb98kDf5LNFyw4YTthvyWdCThxfXKPW4P7cXqIHdVc\",\"lastUpdate\":\"2016-04-08T17:23:41.948Z\",\"timeout\":\"2026-04-08T17:23:41.948Z\",\"active\":1,\"revoked\":0}");
			System.out.println("ok");
			System.out.println("\n  [ JSON: " + json.toString() + " ]\n");
			
			//////////////////////////////////////////////////
			
			System.out.print("encoding to base64URL... ");
			
			String encodedClaim = new String(Base64UrlCodec.BASE64URL.encode(json.toString()));
			
			System.out.println("ok");
			System.out.println("\n  [ encoded: " + encodedClaim + " ]\n");
			
			//////////////////////////////////////////////////
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage() + "\n");
			e.printStackTrace();
		}
	}
}