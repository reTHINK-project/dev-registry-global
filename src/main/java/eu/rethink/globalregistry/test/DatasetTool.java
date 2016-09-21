package eu.rethink.globalregistry.test;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.Base64UrlCodec;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import eu.rethink.globalregistry.model.Dataset;
import eu.rethink.globalregistry.model.DatasetIntegrityException;
import eu.rethink.globalregistry.model.GUID;
import eu.rethink.globalregistry.util.ECDSAKeyPairManager;
import eu.rethink.globalregistry.util.XSDDateTime;

public class DatasetTool
{
	protected static String[] nodes = {"130.149.22.133:5002", "130.149.22.134:5002", "130.149.22.135:5002"};
	protected static int primarynode = 0;
	
	protected static Dataset dataset;
	protected static String privateKey;
	
	private static void printHelp()
	{
		System.out.print("create, c:      create new dataset\n");
		System.out.print("edit, ed:       edit dataset\n");
		System.out.print("exit, e:        exit\n");
		System.out.print("help, h:        print this help\n");
		System.out.print("print, p:       print current dataset\n");
		System.out.print("quit, q:        exit\n");
		System.out.print("readfile, rf:   read dataset from file\n");
		System.out.print("resolve, r:     resolve guid via global registry\n");
		System.out.print("setnode, sn:    set primary greg node to use\n");
		System.out.print("status, s:      run status check on all global registry nodes\n");
		System.out.print("upload, u:      upload dataset to globalregistry\n");
		System.out.print("verify, v:      verify current dataset\n");
		System.out.print("writefile, wf:  write dataset to file\n");
		System.out.print("quit, q:        exit\n");
	}
	
	public static void main(String args[])
	{
		System.out.print("ReThink GlobalRegistry Dataset Tool\n\n");
		printHelp();
		
//		Console c = System.console();
//		if(c == null)
//		{
//			System.err.println("No console. Aborting.\n\n");
//			System.exit(1);
//		}
//		
//		String command = c.readLine("\n> ");
		
		// workaround for IDE (Eclipse)
		Scanner in = new Scanner(System.in);
		System.out.print("\n> ");
		String command = in.nextLine();
		
		while(!command.equals("exit") && !command.equals("e") && !command.equals("quit") && !command.equals("q"))
		{
			if(command.equals("create") || command.equals("c")) 
			{
				JSONObject json = createNewDataset();
				
				// TODO check validity
				
				dataset = Dataset.createFromJSONObject(json.getJSONObject("dataset"));
				privateKey = json.getString("privateKey");
				
				System.out.print("done\n");
			}
			
			else if(command.equals("edit") || command.equals("ed")) 
			{
				System.out.print("feature nor implemented yet\n");
			}
			
			else if(command.equals("help") || command.equals("h"))
			{
				printHelp();
			}
			
			else if(command.equals("print") || command.equals("p")) 
			{
				if(dataset == null)
					System.out.print("no dataset loaded. load from file (rf) or create one (c)");
				else
					System.out.print("current dataset:\n" + dataset.exportJSONObject().toString());
			}
			
			else if(command.equals("setnode") || command.equals("sn"))
			{
				System.out.print("specify node number (0-" + (nodes.length-1) + "):");
				int innodenumber = in.nextInt();
				
				if(innodenumber < 0 || innodenumber > (nodes.length-1))
				{
					System.out.print("error\n");
				}
				else
				{
					primarynode = innodenumber;
					System.out.print("primary node set to " + nodes[innodenumber] + "\n");
				}
			}
			
			else if(command.equals("resolve") || command.equals("r")) 
			{
				try
				{
					System.out.print("specify guid to resolve:");
					String inGUID = in.nextLine();
					
					JSONObject response = new JSONObject(GlobalRegistryAPI.getData(nodes[primarynode], inGUID));
					String jwt = response.getString("Value");
					
					System.out.println("ok");
					System.out.println("\nread JWT: " + jwt + " ]");
					System.out.print("verifying JWT... ");
					
					JSONObject jwtPayload = new JSONObject(new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
					
					String encodedClaim = jwtPayload.getString("data");
					
					String decodedClaim = Base64UrlCodec.BASE64URL.decodeToString(encodedClaim);
					
					JSONObject jsonDataset = new JSONObject(decodedClaim);
					
					PublicKey publicKey = ECDSAKeyPairManager.decodePublicKey(jsonDataset.getString("publicKey"));
					
					try
					{
						Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt);
						
						System.out.print("JWT successfully verified!\n\n");
					}
					catch (SignatureException e)
					{
						System.out.print("JWT verification failed!\n\n");
						e.printStackTrace();
					}
					finally
					{
						System.out.print(inGUID + " resolved to\n" + jsonDataset);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			else if(command.equals("readfile") || command.equals("rf")) 
			{
				System.out.print("specify guid to read from file:");
				String infilename = in.nextLine();
				
				try
				{
					File file = new File(infilename + ".json");
					JSONObject json = new JSONObject(FileUtils.readFileToString(file, "UTF8"));
					dataset = Dataset.createFromJSONObject(json.getJSONObject("dataset"));
					privateKey = json.getString("privateKey");
					
					System.out.print("\nSuccessfully read file " + file.getName() + "\n");
					System.out.print("contents: " + json.getJSONObject("dataset").toString() + "\n");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			else if(command.equals("status") || command.equals("s")) 
			{
				testNodes();
			}
			
			else if(command.equals("upload") || command.equals("u")) 
			{
				System.out.print("encoding to base64URL... ");
				
				String encodedClaim = new String(Base64UrlCodec.BASE64URL.encode(dataset.exportJSONObject().toString()));
				
				System.out.println("ok");
				System.out.println("\n  [ encoded: " + encodedClaim + " ]\n");
				
				//////////////////////////////////////////////////
				
				System.out.print("creating JWT... ");
				String jwt = "";
				try
				{
					jwt = Jwts.builder().claim("data", encodedClaim).signWith(SignatureAlgorithm.ES256, ECDSAKeyPairManager.decodePrivateKey(privateKey)).compact();
				}
				catch (InvalidKeySpecException e)
				{
					e.printStackTrace();
				}
				catch (NoSuchAlgorithmException e)
				{
					e.printStackTrace();
				}
				
				System.out.println("ok");
				System.out.println("\n  [ jwt: " + jwt + " ]\n");
				
				//////////////////////////////////////////////////
				
				System.out.print("writing JWT to GlobalRegistry ... ");
				
				GlobalRegistryAPI.putData(nodes[primarynode], dataset.getGUID(), jwt);
				
				System.out.println("ok");
			}
			
			else if(command.equals("verify") || command.equals("v")) 
			{
				System.out.print("verifying values of dataset ... ");
				
				try
				{
					dataset.checkDatasetValidity(dataset.exportJSONObject());
					System.out.print("ok!\n");
				}
				catch (DatasetIntegrityException e)
				{
					System.out.print("invalid!\n");
					e.printStackTrace();
				}
			}
			
			else if(command.equals("writefile") || command.equals("wf"))
			{
				System.out.print("writing to file " + dataset.getGUID() + ".json ...");
				
				try
				{
					JSONObject jsonDataset = dataset.exportJSONObject();
					JSONObject jsonDatasetWithPrivateKey = new JSONObject();
					jsonDatasetWithPrivateKey.put("dataset", jsonDataset);
					jsonDatasetWithPrivateKey.put("privateKey", privateKey);
					
					File file = new File(dataset.getGUID() + ".json");
					FileUtils.writeStringToFile(file, jsonDatasetWithPrivateKey.toString(), "UTF-8", false);
					
					System.out.print("export successful!\n");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			else
			{
				System.out.print("unrecognized command. type \"help\" or \"h\" for help\n");
			}
			
//			command = c.readLine("\n> ");
			System.out.print("\n> ");
			command = in.nextLine();
		}
		
		in.close();
		System.out.print("bye...\n\n");
	}
	
	private static JSONObject createNewDataset()
	{
		try
		{
			Scanner in = new Scanner(System.in);
			System.out.print("creating new ECDSA keypair... ");
			
			//////////////////////////////////////////////////
			
			KeyPair keypair = ECDSAKeyPairManager.createKeyPair();
			String publicKeyString = ECDSAKeyPairManager.encodePublicKey(keypair.getPublic());
			String privateKeyString = ECDSAKeyPairManager.encodePrivateKey(keypair.getPrivate());
			
			privateKey = privateKeyString;
			
			System.out.println("ok\n");
			
			//////////////////////////////////////////////////
			
			SecureRandom random = new SecureRandom();
			String salt = new BigInteger(80, random).toString(32);
			
			System.out.print("specify salt (26 char length) [" + salt + "]:");
			String insalt = in.nextLine();
			
			if(!insalt.equals(""))
			{
				// TODO check format
				salt = insalt;
			}
			
			System.out.println("> " + salt);
			
			//////////////////////////////////////////////////
			
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			
			String lastUpdate = XSDDateTime.exportXSDDateTime(c.getTime());
			
			c.add(Calendar.DATE, 90);
			
			String timeout = XSDDateTime.exportXSDDateTime(c.getTime());
			
			System.out.print("specify specify timeout (XSDDateTime) [" + timeout + "]:");
			String inTimeout = in.nextLine();
			
			if(!inTimeout.equals(""))
			{
				try
				{
					XSDDateTime.parseXSDDateTime(inTimeout);
				}
				catch(Exception e)
				{
					System.out.print("invalid value. using 90 days from now (" + timeout + ")\n");
					timeout = inTimeout;
				}
			}
			
			System.out.println("> " + timeout);
			
			//////////////////////////////////////////////////
			
			int active = 1;
			int inActive;
			
			System.out.print("specify specify active (0:1) [" + active + "]:");
			
			try
			{
				inActive = Integer.parseInt(in.nextLine());
			}
			catch(NumberFormatException e)
			{
				inActive = 1;
			}
			
			if(inActive != 0 && inActive != 1)
			{
				System.out.print("invalid value. using default (1)\n");
				active = 1;
			}
			
			System.out.println("> " + active);
			
			//////////////////////////////////////////////////
			
			int revoked = 0;
			int inRevoked;
			
			System.out.print("specify specify revoked (0:1) [" + revoked + "]:");
			
			try
			{
				inRevoked = Integer.parseInt(in.nextLine());
			}
			catch(NumberFormatException e)
			{
				inRevoked = 1;
			}
			
			if(inRevoked != 0 && inRevoked != 1)
			{
				System.out.print("invalid value. using default (0)\n");
				revoked = 0;
			}
			
			System.out.println("> " + revoked);
			
			//////////////////////////////////////////////////
			
			// TODO userIDs
			
			JSONObject jsonDataset = new JSONObject();
			jsonDataset.put("salt", salt);
			jsonDataset.put("userIDs", new JSONArray());
			jsonDataset.put("lastUpdate", lastUpdate);
			jsonDataset.put("timeout", timeout);
			jsonDataset.put("publicKey", publicKeyString);
			jsonDataset.put("active", active);
			jsonDataset.put("revoked", revoked);
			jsonDataset.put("guid", GUID.createGUID(publicKeyString, salt));
			
			JSONObject json = new JSONObject();
			json.put("dataset", jsonDataset);
			json.put("privateKey", privateKeyString);
			
			return json;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	protected static void testNodes()
	{
		for(String node: nodes)
		{
			System.out.print("Testing " + node + " ... ");
			String response = GlobalRegistryAPI.getStatus(node);
			
			if(response == null)
				System.out.print("FAILED!\n");
			else
			{
				JSONObject jresponse = new JSONObject(response);
				
				if(jresponse.getInt("Code") == 200)
					System.out.print("OK!\n");
				else
					System.out.print("FAILED!\n");
			}
		}
	}
}