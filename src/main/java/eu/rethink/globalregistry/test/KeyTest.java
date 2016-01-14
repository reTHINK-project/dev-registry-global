package eu.rethink.globalregistry.test;

import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.KeyPair;

import eu.rethink.globalregistry.util.ECDSAKeyPairManager;

public class KeyTest
{
	public static void main(String args[])
	{
		try
		{
			System.out.println("running...");
			KeyPair keypair = ECDSAKeyPairManager.createKeyPair();
			System.out.println(ECDSAKeyPairManager.encodePublicKey(keypair.getPublic()));
			System.out.println(ECDSAKeyPairManager.encodePrivateKey(keypair.getPrivate()));
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}