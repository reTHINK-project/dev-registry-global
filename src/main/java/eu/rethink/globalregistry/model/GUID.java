package eu.rethink.globalregistry.model;

import io.jsonwebtoken.impl.Base64UrlCodec;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import eu.rethink.globalregistry.util.ECDSAKeyPairManager;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Manages GUID creation and verification
 * 
 * @date 18.01.2017
 * @version 1
 * @author Sebastian Göndör
 */
public class GUID
{
	private static int		ITERATIONS		= 10000;
	private static String	HASH_ALGORITHM	= "HmacSHA256";
	
	public static String createGUID(String key, String salt)
	{
		byte[] hash = null;
		
		key = ECDSAKeyPairManager.stripKey(key);
		
		try
		{
			// 32 bytes == 256 bit
			hash = PBKDF2.deriveKey(key.getBytes(), salt.getBytes(), ITERATIONS, 32); 
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch(InvalidKeyException e)
		{
			e.printStackTrace();
		}
		// return (new BigInteger(DatatypeConverter.printHexBinary(hash),
		// 16)).toString(36).toUpperCase(); //<-- base36 encoding
		return new String(Base64UrlCodec.BASE64URL.encode(hash));
	}
	
	private static class PBKDF2
	{
		public static byte[] deriveKey(byte[] password, byte[] salt, int iterationCount, int dkLen)
				throws java.security.NoSuchAlgorithmException, java.security.InvalidKeyException
		{
			SecretKeySpec keyspec = new SecretKeySpec(password, HASH_ALGORITHM);
			Mac prf = Mac.getInstance(HASH_ALGORITHM);
			prf.init(keyspec);
			
			// Note: hLen, dkLen, l, r, T, F, etc. are horrible names for
			// variables and functions in this day and age, but they
			// reflect the terse symbols used in RFC 2898 to describe
			// the PBKDF2 algorithm, which improves validation of the
			// code vs. the RFC.
			//
			// dklen is expressed in bytes. (16 for a 128-bit key)
			
			int hLen = prf.getMacLength(); // 20 for SHA1
			int l = Math.max(dkLen, hLen); // 1 for 128bit (16-byte) keys
			int r = dkLen - (l - 1) * hLen; // 16 for 128bit (16-byte) keys
			byte T[] = new byte[l * hLen];
			int ti_offset = 0;
			
			for(int i = 1; i <= l; i++)
			{
				F(T, ti_offset, prf, salt, iterationCount, i);
				ti_offset += hLen;
			}
			
			if(r < hLen)
			{
				// Incomplete last block
				byte DK[] = new byte[dkLen];
				System.arraycopy(T, 0, DK, 0, dkLen);
				return DK;
			}
			
			return T;
		}
		
		private static void F(byte[] dest, int offset, Mac prf, byte[] S, int c, int blockIndex)
		{
			final int hLen = prf.getMacLength();
			byte U_r[] = new byte[hLen];
			// U0 = S || INT (i);
			byte U_i[] = new byte[S.length + 4];
			
			System.arraycopy(S, 0, U_i, 0, S.length);
			INT(U_i, S.length, blockIndex);
			
			for(int i = 0; i < c; i++)
			{
				U_i = prf.doFinal(U_i);
				xor(U_r, U_i);
			}
			
			System.arraycopy(U_r, 0, dest, offset, hLen);
		}
		
		private static void xor(byte[] dest, byte[] src)
		{
			for(int i = 0; i < dest.length; i++)
			{
				dest[i] ^= src[i];
			}
		}
		
		private static void INT(byte[] dest, int offset, int i)
		{
			dest[offset + 0] = (byte) (i / (256 * 256 * 256));
			dest[offset + 1] = (byte) (i / (256 * 256));
			dest[offset + 2] = (byte) (i / (256));
			dest[offset + 3] = (byte) (i);
		}
		
		private PBKDF2()
		{
		}
	}
}