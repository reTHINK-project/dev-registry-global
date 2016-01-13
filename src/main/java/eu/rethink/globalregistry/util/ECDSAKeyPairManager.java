package eu.rethink.globalregistry.util;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

public class ECDSAKeyPairManager
{
	public static final String	ALGORITHM			= "RSA";
	public static final int		KEYSIZE				= 4096;
	
	public static final String	PUBLICKEY_PREFIX	= "-----BEGIN PUBLIC KEY-----";
	public static final String	PUBLICKEY_POSTFIX	= "-----END PUBLIC KEY-----";
	public static final String	PRIVATEKEY_PREFIX	= "-----BEGIN PRIVATE KEY-----";
	public static final String	PRIVATEKEY_POSTFIX	= "-----END PRIVATE KEY-----";
	
	public static KeyPair createKeyPair() throws NoSuchAlgorithmException
	{
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGenerator.initialize(KEYSIZE);
		
		return keyPairGenerator.genKeyPair();
	}
	
	/**
	 * returns a String beginning with -----BEGIN PUBLIC KEY-----
	 * 
	 * @param key
	 * @return
	 */
	public static String encodePublicKey(PublicKey key)
	{
		return PUBLICKEY_PREFIX + DatatypeConverter.printBase64Binary(key.getEncoded()) + PUBLICKEY_POSTFIX;
	}
	
	/**
	 * returns a String beginning with -----BEGIN PRIVATE KEY-----
	 * 
	 * @param key
	 * @return
	 */
	public static String encodePrivateKey(PrivateKey key)
	{
		return PRIVATEKEY_PREFIX + DatatypeConverter.printBase64Binary(key.getEncoded()) + PRIVATEKEY_POSTFIX;
	}
	
	public static PublicKey decodePublicKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException 
	{
		key = key
			.replace(PUBLICKEY_PREFIX, "")
			.replace(PUBLICKEY_POSTFIX, "")
			.replace("\r", "")
			.replace("\n", "")
			.trim();
		
		byte[] keyBytes = DatatypeConverter.parseBase64Binary(key);
		
		return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(keyBytes));
	}
	
	public static PrivateKey decodePrivateKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException 
	{
		key = key
			.replace(PRIVATEKEY_PREFIX, "")
			.replace(PRIVATEKEY_POSTFIX, "")
			.replace("\r", "")
			.replace("\n", "")
			.trim();
		
		byte[] keyBytes = DatatypeConverter.parseBase64Binary(key);
		
		return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(keyBytes));//X509EncodedKeySpec(keyBytes));
	}
	
	public static String stripKey(String key)
	{
		return key
			.replace("\r", "")
			.replace("\n", "")
			.trim();
	}
}