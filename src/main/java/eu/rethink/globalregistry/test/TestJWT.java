package eu.rethink.globalregistry.test;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.json.JSONArray;
import org.json.JSONObject;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.Base64UrlCodec;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
			System.out.println("\n  [ PrivateKey: " + privateKeyString + " ] \n expected: " + ECDSAKeyPairManager.encodePrivateKey(privateKey));
			System.out.println("  [ PublicKey: " + publicKeyString + " ] \n expected: " + ECDSAKeyPairManager.encodePublicKey(publicKey) + "\n");
			
			//////////////////////////////////////////////////
			
//			System.out.print("creating JSONObject from String... ");
//			
//			JSONObject json = new JSONObject(datasetString);
//			System.out.println("ok");
//			System.out.println("\n  [ JSON: " + json.toString() + " ]\n");
			
			//////////////////////////////////////////////////
			
//			System.out.print("encoding to base64URL... ");
//			
//			String encodedClaim = new String(Base64UrlCodec.BASE64URL.encode(json.toString()));
//			
//			System.out.println("ok");
//			System.out.println("\n  [ encoded: " + encodedClaim + " ]\n");
//			
//			encodedClaim = "eyJkYXRhIjoiZXlKbmRXbGtJam9pTnpaaVptNXlkREZ1WDFKVFN6WTRRM0kzVkdKNmVtVmZjMHd3VDNobWNsRnVNSFp0V1hONFkzbzVPQ0lzSW5OaGJIUWlPaUozY1M5a1QwWmFZa0ZtU2pSMlNYQldSVGd5UkZaSlVWQTNSbEJsU21ReFJuSXdNR294Y0U5aGRtOXZQU0lzSW5WelpYSkpSSE1pT2x0ZExDSnNZWE4wVlhCa1lYUmxJam9pTWpBeE5pMHdOQzB4TVZRd09Ub3lOam94TUM0eE5UTmFJaXdpZEdsdFpXOTFkQ0k2SWpJd01qWXRNRFF0TVRGVU1EazZNalk2TVRBdU1UVXpXaUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZYVms4eU1XZFplU3R5UjFwME0zaG1hVkp2VVZWTVIwcHNSR2xhY2tGdkszaDRiWE12UzFsbE5rOXNiRGxVVDBaclQxQkxPRVZuVFU1bU1FcG1kbGROU1cxc1VFVndjVkpHTXpsWWJqUjNUMW8yZEd4UmR6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2lZV04wYVhabElqb3hMQ0p5WlhadmEyVmtJam93ZlEifQ";

			//////////////////////////////////////////////////
						
//			System.out.print("creating JWT... ");
//			String jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, privateKey).compact();
//			
//			System.out.println("ok");
//			System.out.println("\n  [ jwt: " + jwt + " ]\n");
//			
//			System.out.println("expected JWT: eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKbmRXbGtJam9pTnpaaVptNXlkREZ1WDFKVFN6WTRRM0kzVkdKNmVtVmZjMHd3VDNobWNsRnVNSFp0V1hONFkzbzVPQ0lzSW5OaGJIUWlPaUozY1M5a1QwWmFZa0ZtU2pSMlNYQldSVGd5UkZaSlVWQTNSbEJsU21ReFJuSXdNR294Y0U5aGRtOXZQU0lzSW5WelpYSkpSSE1pT2x0ZExDSnNZWE4wVlhCa1lYUmxJam9pTWpBeE5pMHdOQzB4TVZRd09Ub3lOam94TUM0eE5UTmFJaXdpZEdsdFpXOTFkQ0k2SWpJd01qWXRNRFF0TVRGVU1EazZNalk2TVRBdU1UVXpXaUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZYVms4eU1XZFplU3R5UjFwME0zaG1hVkp2VVZWTVIwcHNSR2xhY2tGdkszaDRiWE12UzFsbE5rOXNiRGxVVDBaclQxQkxPRVZuVFU1bU1FcG1kbGROU1cxc1VFVndjVkpHTXpsWWJqUjNUMW8yZEd4UmR6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2lZV04wYVhabElqb3hMQ0p5WlhadmEyVmtJam93ZlEifQ.RECC-4Ruun18vvTx-5gfiIXFjvZkHWRcBtE66rFhJ4KRcz_bx1TaD5uSwdFtvTNtFZTT1SOqLEK12ZNw-b1pRg");
			
			
//			jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, privateKey).compact();
//			System.out.println(jwt);
//			jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, privateKey).compact();
//			System.out.println(jwt);
//			jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, privateKey).compact();
//			System.out.println(jwt);
//			jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, privateKey).compact();
//			System.out.println(jwt);
			
			//////////////////////////////////////////////////
			
//			System.out.print("Writing new JWT to GlobalRegistry [Node1] (PUT new dataset)... ");
//			
//			GlobalRegistryAPI.putData(gRegNode1, json.getString("guid"), jwt);
//			
//			System.out.println("ok");
			
			//////////////////////////////////////////////////
			
//			System.out.print("Fetching JWT from GlobalRegistry [Node1] (GET existing dataset)... ");
//			
//			jwt = GlobalRegistryAPI.getData(gRegNode1, json.getString("guid"));
//			
//			System.out.println("ok");
//			System.out.println("\n  [ jwt: " + jwt + " ]\n");
			
			
			
			// jose4j
			
			// The content that will be signed
//		    String examplePayload = encodedClaim;
			
			// Create a new JsonWebSignature
//		    JsonWebSignature jws = new JsonWebSignature();

		    // Set the payload, or signed content, on the JWS object
//		    jws.setPayload(examplePayload);
//
//		    // Set the signature algorithm on the JWS that will integrity protect the payload
//		    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
//
//		    // Set the signing key on the JWS
//		    // Note that your application will need to determine where/how to get the key
//		    // and here we just use an example from the JWS spec
//		    System.out.println(privateKey);
//		    jws.setKey(privateKey);
//			
//		    // Sign the JWS and produce the compact serialization or complete JWS representation, which
//		    // is a string consisting of three dot ('.') separated base64url-encoded
//		    // parts in the form Header.Payload.Signature
//		    String jwsCompactSerialization = jws.getCompactSerialization();
//
//		    // Do something useful with your JWS
//		    System.out.println(jwsCompactSerialization);
//			
			// verification with jose4j:
		    
//		    String fromJS = "eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKMWMyVnlTVVJ6SWpwYlhTd2ljSFZpYkdsalMyVjVJam9pTFMwdExTMUNSVWRKVGlCUVZVSk1TVU1nUzBWWkxTMHRMUzFOUmxsM1JVRlpTRXR2V2tsNmFqQkRRVkZaUmtzMFJVVkJRVzlFVVdkQlJWZFdUekl4WjFsNUszSkhXblF6ZUdacFVtOVJWVXhIU214RWFWcHlRVzhyZUhodGN5OUxXV1UyVDJ4c09WUlBSbXRQVUVzNFJXZE5UbVl3U21aMlYwMUpiV3hRUlhCeFVrWXpPVmh1TkhkUFdqWjBiRkYzUFQwdExTMHRMVVZPUkNCUVZVSk1TVU1nUzBWWkxTMHRMUzBpTENKellXeDBJam9pZDNFdlpFOUdXbUpCWmtvMGRrbHdWa1U0TWtSV1NWRlFOMFpRWlVwa01VWnlNREJxTVhCUFlYWnZiejBpTENKbmRXbGtJam9pTnpaaVptNXlkREZ1WDFKVFN6WTRRM0kzVkdKNmVtVmZjMHd3VDNobWNsRnVNSFp0V1hONFkzbzVPQ0lzSW14aGMzUlZjR1JoZEdVaU9pSXlNREUyTFRBMExURXhWREE1T2pJMk9qRXdMakUxTTFvaUxDSjBhVzFsYjNWMElqb2lNakF5Tmkwd05DMHhNVlF3T1RveU5qb3hNQzR4TlROYUlpd2lZV04wYVhabElqb3hMQ0p5WlhadmEyVmtJam93ZlE9PSJ9.tItp-fbS4gk7AGNJeGtuJRk_31r7k0yyq0DxfHWFJ4RcFOvtLH2JQ1ibm9AEM3oQ9-BqqX7IuyWnFXPZF74Pow";
//		    String fromJ  = "eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKMWMyVnlTVVJ6SWpwYlhTd2ljSFZpYkdsalMyVjVJam9pTFMwdExTMUNSVWRKVGlCUVZVSk1TVU1nUzBWWkxTMHRMUzFOUmxsM1JVRlpTRXR2V2tsNmFqQkRRVkZaUmtzMFJVVkJRVzlFVVdkQlJWZFdUekl4WjFsNUszSkhXblF6ZUdacFVtOVJWVXhIU214RWFWcHlRVzhyZUhodGN5OUxXV1UyVDJ4c09WUlBSbXRQVUVzNFJXZE5UbVl3U21aMlYwMUpiV3hRUlhCeFVrWXpPVmh1TkhkUFdqWjBiRkYzUFQwdExTMHRMVVZPUkNCUVZVSk1TVU1nUzBWWkxTMHRMUzBpTENKellXeDBJam9pZDNFdlpFOUdXbUpCWmtvMGRrbHdWa1U0TWtSV1NWRlFOMFpRWlVwa01VWnlNREJxTVhCUFlYWnZiejBpTENKbmRXbGtJam9pTnpaaVptNXlkREZ1WDFKVFN6WTRRM0kzVkdKNmVtVmZjMHd3VDNobWNsRnVNSFp0V1hONFkzbzVPQ0lzSW14aGMzUlZjR1JoZEdVaU9pSXlNREUyTFRBMExURXhWREE1T2pJMk9qRXdMakUxTTFvaUxDSjBhVzFsYjNWMElqb2lNakF5Tmkwd05DMHhNVlF3T1RveU5qb3hNQzR4TlROYUlpd2lZV04wYVhabElqb3hMQ0p5WlhadmEyVmtJam93ZlE9PSJ9.MEUCIFTuTdDuMy3G4HrJ8-tJ31_vlR6nZkJSojCrTF8BUMtlAiEAhCwEUP436YCxkDn-qiwwW5a1AU_njxSrnr5ehB5xHek";

//		    // Create a new JsonWebSignature
//		    jws = new JsonWebSignature();
//
//		    // Set the compact serialization on the JWS
//		    jws.setCompactSerialization(fromJS);
//
//		    // Set the verification key
//		    // Note that your application will need to determine where/how to get the key
//		    // Here we use an example from the JWS spec
//		    jws.setKey(publicKey);
//
//		    // Check the signature
//		    boolean signatureVerified = jws.verifySignature();
//
//		    // Do something useful with the result of signature verification
//		    System.out.println("JWS Signature is valid: " + signatureVerified);
//
//		    // Get the payload, or signed content, from the JWS
//		    String payload = jws.getPayload();
//
//		    // Do something useful with the content
//		    System.out.println("JWS payload: " + payload);
//		    
//		    
//		    // Create a new JsonWebSignature
//		    jws = new JsonWebSignature();
//
//		    // Set the compact serialization on the JWS
//		    jws.setCompactSerialization(fromJ);
//
//		    // Set the verification key
//		    // Note that your application will need to determine where/how to get the key
//		    // Here we use an example from the JWS spec
//		    jws.setKey(publicKey);
//
//		    // Check the signature
//		    signatureVerified = jws.verifySignature();
//
//		    // Do something useful with the result of signature verification
//		    System.out.println("JWS Signature is valid: " + signatureVerified);
//
//		    // Get the payload, or signed content, from the JWS
//		    payload = jws.getPayload();
//
//		    // Do something useful with the content
//		    System.out.println("JWS payload: " + payload);
		    
			
			
			// nimbus jose jwt:
			
			// Create the public and private EC keys
//			ECPrivateKey ecPrivateKey = (ECPrivateKey) privateKey;
//			
//			// Create the EC signer
//			JWSSigner signer = new ECDSASigner(ecPrivateKey);
//		    
//			// Prepare JWT with claims set
//			JWTClaimsSet claimsSet = new JWTClaimsSet();
////			claimsSet.setSubject("alice");
////			claimsSet.setIssuer("https://c2id.com");
////			claimsSet.setExpirationTime(new Date(new Date().getTime() + 60 * 1000));
//
//			SignedJWT signedJWT = new SignedJWT(
//			    new JWSHeader(JWSAlgorithm.ES256), 
//			    claimsSet);
//
//			// Compute the EC signature
//			signedJWT.sign(signer);

			


		    
		    
		
		    
			System.out.println("####Example from Bounty Castle####");
//	        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
//	        KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
//	        g.initialize(ecSpec, new SecureRandom());
//	        KeyPair pairBC = g.generateKeyPair();
//	        PrivateKey privKeyBC = privateKey; //pairBC.getPrivate();
//	        PublicKey pubKeyBC = publicKey; //pairBC.getPublic();
	        

			
			String header = "eyJhbGciOiJFUzI1NiJ9";
			String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKbmRXbGtJam9pTnpaaVptNXlkREZ1WDFKVFN6WTRRM0kzVkdKNmVtVmZjMHd3VDNobWNsRnVNSFp0V1hONFkzbzVPQ0lzSW5OaGJIUWlPaUozY1M5a1QwWmFZa0ZtU2pSMlNYQldSVGd5UkZaSlVWQTNSbEJsU21ReFJuSXdNR294Y0U5aGRtOXZQU0lzSW5WelpYSkpSSE1pT2x0ZExDSnNZWE4wVlhCa1lYUmxJam9pTWpBeE5pMHdOQzB4TVZRd09Ub3lOam94TUM0eE5UTmFJaXdpZEdsdFpXOTFkQ0k2SWpJd01qWXRNRFF0TVRGVU1EazZNalk2TVRBdU1UVXpXaUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZYVms4eU1XZFplU3R5UjFwME0zaG1hVkp2VVZWTVIwcHNSR2xhY2tGdkszaDRiWE12UzFsbE5rOXNiRGxVVDBaclQxQkxPRVZuVFU1bU1FcG1kbGROU1cxc1VFVndjVkpHTXpsWWJqUjNUMW8yZEd4UmR6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2lZV04wYVhabElqb3hMQ0p5WlhadmEyVmtJam93ZlEifQ.RECC-4Ruun18vvTx-5gfiIXFjvZkHWRcBtE66rFhJ4KRcz_bx1TaD5uSwdFtvTNtFZTT1SOqLEK12ZNw-b1pRg";
			String body = "eyJkYXRhIjoiZXlKbmRXbGtJam9pTnpaaVptNXlkREZ1WDFKVFN6WTRRM0kzVkdKNmVtVmZjMHd3VDNobWNsRnVNSFp0V1hONFkzbzVPQ0lzSW5OaGJIUWlPaUozY1M5a1QwWmFZa0ZtU2pSMlNYQldSVGd5UkZaSlVWQTNSbEJsU21ReFJuSXdNR294Y0U5aGRtOXZQU0lzSW5WelpYSkpSSE1pT2x0ZExDSnNZWE4wVlhCa1lYUmxJam9pTWpBeE5pMHdOQzB4TVZRd09Ub3lOam94TUM0eE5UTmFJaXdpZEdsdFpXOTFkQ0k2SWpJd01qWXRNRFF0TVRGVU1EazZNalk2TVRBdU1UVXpXaUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZYVms4eU1XZFplU3R5UjFwME0zaG1hVkp2VVZWTVIwcHNSR2xhY2tGdkszaDRiWE12UzFsbE5rOXNiRGxVVDBaclQxQkxPRVZuVFU1bU1FcG1kbGROU1cxc1VFVndjVkpHTXpsWWJqUjNUMW8yZEd4UmR6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2lZV04wYVhabElqb3hMQ0p5WlhadmEyVmtJam93ZlEifQ";
			String signatureString = "RECC-4Ruun18vvTx-5gfiIXFjvZkHWRcBtE66rFhJ4KRcz_bx1TaD5uSwdFtvTNtFZTT1SOqLEK12ZNw-b1pRg";
//			signature = "19d9404046c38cae4b262beb8c0557d81f423fe1aba4bb598500f98f89e5319f";
//			signature = "649cdab6869deee972c3829022698bca3a9080d6bbd9758607b116673cb4cf03";
			
//			header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
//			jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbGxlamEiLCJhZG1pbiI6dHJ1ZX0.03f329983b86f7d9a9f5fef85305880101d5e302afafa20154d094b229f75773";
//			body = "eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbGxlamEiLCJhZG1pbiI6dHJ1ZX0";
//			signature = "03f329983b86f7d9a9f5fef85305880101d5e302afafa20154d094b229f75773";
			
			String encodedString = header + "." + body;
			
			byte[] signatureByteArray = signatureString.getBytes(StandardCharsets.UTF_8);
			System.out.println(signatureByteArray);
			
			
			
			
			
            System.out.println("####Example from Bounty Castle####");
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
            g.initialize(ecSpec, new SecureRandom());
            KeyPair pairBC = g.generateKeyPair();
            PrivateKey privKeyBC = privateKey; //pairBC.getPrivate();
            PublicKey pubKeyBC = publicKey; //pairBC.getPublic();
            
            //Signing
            String plaintext = encodedString;
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", "BC");
            ecdsaSign.initSign(privKeyBC);
            ecdsaSign.update(plaintext.getBytes("UTF-8"));
            byte[] signature = ecdsaSign.sign();
            System.out.println("Signatur is "+signature);
            System.out.println("sig is: " + new String(signature));
            
            //Verifying
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", "BC");
            ecdsaVerify.initVerify(pubKeyBC);
            ecdsaVerify.update(plaintext.getBytes("UTF-8"));
            boolean result = ecdsaVerify.verify(signature);
            System.out.println(">>>>>>>Signature verifiction is "+result);
			
			
			
			
			
			
			
	        //Signing
//	        String plaintext = encodedString;
//	        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", "BC");
//	        ecdsaSign.initSign(privateKey);
//	        ecdsaSign.update(plaintext.getBytes("UTF-8"));
//	        byte[] signature2 = ecdsaSign.sign();
//	        System.out.println("Signatur is "+signature2);
//			String test = new String(signature2);
//			System.out.println(test);
//	        
//	        //Verifying
//	        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", "BC");
//	        ecdsaVerify.initVerify(publicKey);
//	        ecdsaVerify.update(encodedString.getBytes(StandardCharsets.UTF_8));
//	        boolean result = ecdsaVerify.verify(signatureByteArray);
//	        System.out.println(">>>>>>>Signature verifiction is "+result);
	        
			
		    
			//////////////////////////////////////////////////
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage() + "\n");
			e.printStackTrace();
		}
	}
}