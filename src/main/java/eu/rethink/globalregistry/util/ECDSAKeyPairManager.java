package eu.rethink.globalregistry.util;

import io.jsonwebtoken.impl.Base64UrlCodec;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ECDSAKeyPairManager
{
	public static final String	ALGORITHM			= "ECDSA";
	public static final String	CURVE				= "secp256k1";
	//public static final int		KEYSIZE				= 160;
	
	public static final String	PUBLICKEY_PREFIX	= "-----BEGIN PUBLIC KEY-----";
	public static final String	PUBLICKEY_POSTFIX	= "-----END PUBLIC KEY-----";
	public static final String	PRIVATEKEY_PREFIX	= "-----BEGIN PRIVATE KEY-----";
	public static final String	PRIVATEKEY_POSTFIX	= "-----END PRIVATE KEY-----";
	
	public static KeyPair createKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException
	{
		Security.addProvider(new BouncyCastleProvider());
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM, "BC");
		ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE);
		keyGen.initialize(ecSpec, new SecureRandom());
		
		return keyGen.generateKeyPair();
	}
	
	/**
	 * returns a PKCS#8 String beginning with -----BEGIN PUBLIC KEY-----
	 * 
	 * @param key
	 * @return
	 */
	public static String encodePublicKey(PublicKey key)
	{
		Security.addProvider(new BouncyCastleProvider());
		return PUBLICKEY_PREFIX + Base64UrlCodec.BASE64URL.encode(key.getEncoded()) + PUBLICKEY_POSTFIX;
	}
	
	/**
	 * returns a PKCS#8 String beginning with -----BEGIN PRIVATE KEY-----
	 * 
	 * @param key
	 * @return
	 */
	public static String encodePrivateKey(PrivateKey key)
	{
		Security.addProvider(new BouncyCastleProvider());
		return PRIVATEKEY_PREFIX + Base64UrlCodec.BASE64URL.encode(key.getEncoded()) + PRIVATEKEY_POSTFIX;
	}
	
	// TODO check if this even works
	public static PublicKey decodePublicKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException 
	{
		Security.addProvider(new BouncyCastleProvider());
		key = key
			.replace(PUBLICKEY_PREFIX, "")
			.replace(PUBLICKEY_POSTFIX, "")
			.replace("\r", "")
			.replace("\n", "")
			.trim();
		
		byte[] keyBytes = Base64UrlCodec.BASE64URL.decode(key);
		
		return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(keyBytes));
	}
	
	public static PrivateKey decodePrivateKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException 
	{
		Security.addProvider(new BouncyCastleProvider());
		key = key
			.replace(PRIVATEKEY_PREFIX, "")
			.replace(PRIVATEKEY_POSTFIX, "")
			.replace("\r", "")
			.replace("\n", "")
			.trim();
		
		byte[] keyBytes = Base64UrlCodec.BASE64URL.decode(key);
		
		return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
	}
	
	public static String stripKey(String key)
	{
		return key
			.replace("\r", "")
			.replace("\n", "")
			.trim();
	}
}