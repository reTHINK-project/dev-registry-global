package eu.rethink.globalregistry.test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.rethink.globalregistry.model.Dataset;
import eu.rethink.globalregistry.model.DatasetIntegrityException;
import eu.rethink.globalregistry.model.GUIDs;
import eu.rethink.globalregistry.util.ECDSAKeyPairManager;
import eu.rethink.globalregistry.util.XSDDateTime;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by Half-Blood on 1/4/2017.
 */
public class DatasetTool {
    public static int GREG_PORT = 5002;

    protected static JSONArray nodes = new JSONArray();
    protected static final String defaultNode = "130.149.22.133";
    protected static String activeNode = "130.149.22.133";

    protected static Dataset dataset;
    protected static String privateKey;

    private static void printHelp()
    {
        System.out.print("------------------------------\nv 0.0.7\n------------------------------\n");
        System.out.print("create, c:      create new dataset\n");
        System.out.print("edit, e:        edit dataset\n");
        System.out.print("exit, x:        exit\n");
        System.out.print("help, h:        print this help\n");
        System.out.print("print, p:       print current dataset\n");
        System.out.print("jwt, j:         print signed jwt for current dataset\n");
        System.out.print("quit, q:        exit\n");
        System.out.print("readfile, rf:   read dataset from file\n");
        System.out.print("resolve, r:     resolve guid via global registry\n");
        System.out.print("setnode, sn:    set active greg node to use\n");
        System.out.print("status, s:      run status check on all global registry nodes\n");
        System.out.print("upload, u:      upload dataset to globalregistry\n");
        System.out.print("verify, v:      verify current dataset\n");
        System.out.print("writefile, wf:  write dataset to file\n");
        System.out.print("quit, q:        exit\n");
    }

    public static void main(String args[])
    {
        //SpringApplication.run(DatasetTool.class, args);
        System.out.print("ReThink GlobalRegistry Dataset Tool\n\n");

        // parse dht from default node
        System.out.println("parsing dht network... ");
        nodes = parseNetwork(defaultNode);

        System.out.println("running status check... ");
        testNodes();

        // TODO handle exception of default node not reachable

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

        while(!command.equals("exit") && !command.equals("x") && !command.equals("quit") && !command.equals("q"))
        {
            if(command.equals("create") || command.equals("c"))
            {
                String inOverwriteDataset = "y";

                if(dataset != null)
                {
                    System.out.print("overwrite current dataset in memory? (y|n) [n]: \n");
                    inOverwriteDataset = in.nextLine();
                }

                if(!inOverwriteDataset.equals("n") && !inOverwriteDataset.equals("y"))
                {
                    System.out.print("illegal parameter!\n");
                }
                else if(inOverwriteDataset.equals("n"))
                {
                    System.out.print("aborting...\n");
                }
                else
                {
                    System.out.print("creating new dataset ...\n");

                    JSONObject json = createNewDataset();

                    dataset = Dataset.createFromJSONObject(json.getJSONObject("dataset"));
                    privateKey = json.getString("privateKey");

                    verifyDataset();

                    System.out.print("dataset successfully created. GUID: " + dataset.getGUID() + "\n");

                    printDataset();
                }
            }

            else if(command.equals("edit") || command.equals("e"))
            {
                System.out.print("feature disabled due to some bug...\n");
				/*if(dataset == null)
					System.out.print("no dataset loaded. load from file (rf) or create one (c)");
				else
					dataset = Dataset.createFromJSONObject(editDataset());

				System.out.print("dataset successfully edited. GUID: " + dataset.getGUID() + "\n");

				printDataset();*/
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
                    printDataset();
            }

            else if(command.equals("setnode") || command.equals("sn"))
            {
                System.out.print("specify node IP address [" + activeNode + "]: ");
                String innode = in.nextLine();

                String response = GlobalRegistryAPI.getStatus(innode + ":" + GREG_PORT);

                if(response == null)
                {
                    System.out.print("error! node not reachable! active node set to " + activeNode + "\n");
                    activeNode = defaultNode;
                }
                else
                {
                    JSONObject jresponse = new JSONObject(response);

                    if(jresponse.getInt("Code") == 200)
                    {
                        activeNode = innode;
                        System.out.print("active node set to " + activeNode + "\n");
                    }
                    else
                    {
                        System.out.print("error! node not reachable! active node set to " + activeNode + "\n");
                        activeNode = defaultNode;
                    }
                }
            }

            else if(command.equals("resolve") || command.equals("r"))
            {
                try
                {
                    System.out.print("specify guid to resolve:");
                    String inGUID = in.nextLine();

                    JSONObject response = new JSONObject(GlobalRegistryAPI.getData(activeNode + ":" + GREG_PORT, inGUID));
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
                try
                {
                    System.out.print("specify guid to read from file:");
                    String inFilename = in.nextLine();

                    JSONObject json = readDatasetFile(inFilename);

                    dataset = Dataset.createFromJSONObject(json.getJSONObject("dataset"));
                    privateKey = json.getString("privateKey");

                    System.out.print("contents: " + json.getJSONObject("dataset").toString() + "\n");
                }
                catch (JSONException e)
                {
                    System.out.print("error parsing file\n");
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    System.out.print("error parsing file\n");
                    e.printStackTrace();
                }
            }

            else if(command.equals("status") || command.equals("s"))
            {
                testNodes();
            }

            else if(command.equals("setnode") || command.equals("sn"))
            {
                setActiveNode();
            }

            else if(command.equals("upload") || command.equals("u"))
            {
                if(dataset == null)
                    System.out.print("no dataset loaded. load from file (rf) or create one (c)");
                else
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

                    GlobalRegistryAPI.putData(activeNode + ":" + GREG_PORT, dataset.getGUID(), jwt);

                    System.out.println("ok");
                }
            }

            else if(command.equals("jwt") || command.equals("j"))
            {
                if(dataset == null)
                    System.out.print("no dataset loaded. load from file (rf) or create one (c)");
                else
                {
                    String encodedClaim = new String(Base64UrlCodec.BASE64URL.encode(dataset.exportJSONObject().toString()));

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
                }
            }

            else if(command.equals("verify") || command.equals("v"))
            {
                if(dataset == null)
                    System.out.print("no dataset loaded. load from file (rf) or create one (c)");
                else
                    verifyDataset();
            }

            else if(command.equals("writefile") || command.equals("wf"))
            {
                if(dataset == null)
                    System.out.print("no dataset loaded. load from file (rf) or create one (c)");
                else
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

    private static JSONObject editDataset()
    {
        // TODO use simple cloning maybe?
        Dataset oldDataset = Dataset.createFromJSONObject(dataset.exportJSONObject());

        Scanner in = new Scanner(System.in);

        try
        {
            // TODO recreate keypair
            KeyPair keypair = ECDSAKeyPairManager.createKeyPair();
            //String publicKeyString = ECDSAKeyPairManager.encodePublicKey(keypair.getPublic());
            String privateKeyString = ECDSAKeyPairManager.encodePrivateKey(keypair.getPrivate());

            privateKey = privateKeyString;

            System.out.println("ok\n");

            //////////////////////////////////////////////////

            //////////////////////////////////////////////////

            Calendar c = Calendar.getInstance();
            c.setTime(new Date());

            String lastUpdate = XSDDateTime.exportXSDDateTime(c.getTime());
            String timeout = oldDataset.getTimeout();

            System.out.print("specify specify timeout (XSDDateTime) [" + oldDataset.getTimeout() + "]:");
            String inTimeout = in.nextLine();

            if(!inTimeout.equals(""))
            {
                try
                {
                    XSDDateTime.parseXSDDateTime(inTimeout);
                }
                catch(Exception e)
                {
                    System.out.print("invalid value. using 90 days from now (" + oldDataset.getTimeout() + ")\n");
                    timeout = inTimeout;
                }
            }

            System.out.println("> " + timeout);

            //////////////////////////////////////////////////

            int active = oldDataset.getActive();
            int inActive;

            System.out.print("specify specify active (0|1) [" + oldDataset.getActive() + "]:");

            try
            {
                inActive = Integer.parseInt(in.nextLine());
            }
            catch(NumberFormatException e)
            {
                inActive = oldDataset.getActive();
            }

            if(inActive != 0 && inActive != 1)
            {
                System.out.print("invalid value. leaving value unchanged (" + oldDataset.getRevoked() + "\n");
                active = oldDataset.getActive();
            }

            System.out.println("> " + active);

            //////////////////////////////////////////////////

            int revoked = oldDataset.getRevoked();
            int inRevoked;

            System.out.print("specify specify revoked (0:1) [" + oldDataset.getRevoked() + "]:");

            try
            {
                inRevoked = Integer.parseInt(in.nextLine());
            }
            catch(NumberFormatException e)
            {
                inRevoked = oldDataset.getRevoked();
            }

            if(inRevoked != 0 && inRevoked != 1)
            {
                System.out.print("invalid value. leaving value unchanged (" + oldDataset.getRevoked() + "\n");
                revoked = oldDataset.getRevoked();
            }

            System.out.println("> " + revoked);

            //////////////////////////////////////////////////

            JSONArray userIDs = new JSONArray();
            JSONArray oldUserIDs = oldDataset.getUserIDs();

            String inEditUserID;

            for(int i=0; i<oldUserIDs.length(); i++)
            {
                System.out.print("type new value for userID. enter for leaving it unchanged. [" + oldUserIDs.getString(i) + "]: \n");
                inEditUserID = in.nextLine();

                try
                {
                    System.out.println("> " + oldUserIDs.getString(i));

                    if(inEditUserID.equals(""))
                        userIDs.put(oldUserIDs.getString(i));
                        //else if(false) // TODO if format invalid
                        //	userIDs.put(oldUserIDs.getString(i));
                    else
                        userIDs.put(inEditUserID);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    continue;
                }
                finally
                {
                    //in.close();
                }
            }

            //////////////////////////////////////////////////

            //JSONArray userIDs = new JSONArray();

            System.out.print("add UserID? (y|n) [n]: \n");
            String inAddUserID = in.nextLine();

            while(inAddUserID.equals("y"))
            {
                System.out.print("specify userID: \n");
                String inUserID = in.nextLine();

                // TODO implement format check

                userIDs.put(inUserID);

                System.out.print("add another UserID? (y|n) [n]: \n");
                inAddUserID = in.nextLine();
            }

            if(!inAddUserID.equals("n"))
                System.out.print("illegal parameter!\n");

            //////////////////////////////////////////////////

            System.out.print("finishing up... \n");

            JSONObject jsonDataset = new JSONObject();
            jsonDataset.put("salt", oldDataset.getSalt());
            jsonDataset.put("userIDs", userIDs);
            jsonDataset.put("lastUpdate", lastUpdate);
            jsonDataset.put("timeout", timeout);
            jsonDataset.put("publicKey", oldDataset.getPublicKey());
            jsonDataset.put("active", active);
            jsonDataset.put("revoked", revoked);
            jsonDataset.put("guid", oldDataset.getGUID());

            return jsonDataset;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private static JSONObject createNewDataset()
    {
        Scanner in = new Scanner(System.in);

        try
        {
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

            System.out.print("specify salt (26 char length) [" + salt + "]: ");
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

            System.out.print("specify specify timeout (XSDDateTime) [" + timeout + "]: ");
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

            System.out.print("specify specify active (0|1) [" + active + "]: ");

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

            System.out.print("specify specify revoked (0|1) [" + revoked + "]: ");

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

            JSONArray userIDs = new JSONArray();

            System.out.print("add UserID? (y|n) [n]: \n");
            String inAddUserID = in.nextLine();

            while(inAddUserID.equals("y"))
            {
                System.out.print("specify uID: \n");
                String inUID = in.nextLine();

                System.out.print("specify domain: \n");
                String inDomain = in.nextLine();

                // TODO implement format check

                userIDs.put(new JSONObject().put("uID", inUID).put("domain", inDomain));

                System.out.print("add another UserID? (y|n) [n]: \n");
                inAddUserID = in.nextLine();
            }

            if(!inAddUserID.equals("n"))
                System.out.print("illegal parameter!\n");

            //////////////////////////////////////////////////

            JSONObject defaults = new JSONObject();

            System.out.print("default for voice: \n");
            String inVoiceDefault = in.nextLine();
            defaults.put("voice", inVoiceDefault);

            System.out.print("default for chat: \n");
            String inChatDefault = in.nextLine();
            defaults.put("chat", inChatDefault);

            System.out.print("default for video: \n");
            String inVideoDefault = in.nextLine();
            defaults.put("video", inVideoDefault);

            System.out.print("add another default? (y|n) [n]: \n");
            String inAddDefault = in.nextLine();

            while(inAddDefault.equals("y"))
            {
                System.out.print("specify default name: \n");
                String inDefaultName = in.nextLine();

                System.out.print("specify defautl value for [" + inDefaultName + "]: \n");
                String inDefaultValue = in.nextLine();

                // TODO implement format check

                defaults.put(inDefaultName, inDefaultValue);

                System.out.print("add another default? (y|n) [n]: \n");
                inAddDefault = in.nextLine();
            }

            if(!inAddDefault.equals("n"))
                System.out.print("illegal parameter!\n");

            System.out.print("finishing up... \n");

            JSONObject jsonDataset = new JSONObject();
            jsonDataset.put("salt", salt);
            jsonDataset.put("userIDs", userIDs);
            jsonDataset.put("lastUpdate", lastUpdate);
            jsonDataset.put("timeout", timeout);
            jsonDataset.put("publicKey", publicKeyString);
            jsonDataset.put("active", active);
            jsonDataset.put("revoked", revoked);
            jsonDataset.put("guid", GUIDs.createGUID(publicKeyString, salt));
            jsonDataset.put("defaults", defaults);
            jsonDataset.put("schemaVersion", 1);

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
        finally
        {
            //in.close();
        }
    }

    private static void verifyDataset()
    {
        System.out.print("verifying values of dataset ... ");

        try
        {
            Dataset.checkDatasetValidity(dataset.exportJSONObject());
            System.out.print("ok!\n");
        }
        catch (DatasetIntegrityException e)
        {
            System.out.print("invalid!\n");
            e.printStackTrace();
        }
    }

    private static void printDataset()
    {
        System.out.print("current dataset:\n" + dataset.exportJSONObject().toString(2));
    }

    private static JSONObject readDatasetFile(String filename)
    {
        JSONObject json = null;

        try
        {
            File file = new File(filename + ".json");
            json = new JSONObject(FileUtils.readFileToString(file, "UTF8"));

            System.out.print("\nSuccessfully read file " + file.getName() + "\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return json;
    }

    private static JSONArray parseNetwork(String initialNode)
    {
        JSONArray toCheck = new JSONArray();
        JSONArray foundNodes = new JSONArray();

        toCheck.put(initialNode);

        while(toCheck.length() > 0)
        {
            //System.out.println("in array toCheck: " + toCheck.toString());
            try
            {
                //System.out.println("checking: " + toCheck.getString(0));
                //System.out.print(GlobalRegistryAPI.getStatus(toCheck.getString(0)));

                // trying the first node in toCheck
                JSONArray connectedNodes = new JSONObject(GlobalRegistryAPI.getStatus(toCheck.getString(0) + ":" + GREG_PORT)).getJSONArray("connectedNodes");

                // as no exception was thrown, the currently checked node is online. hence adding it to foundNodes
                foundNodes.put(toCheck.getString(0));
                //System.out.println("adding " + toCheck.getString(0) + " to foundNodes");

                // removing currently checked node from toCheck
                //System.out.println("removing " + toCheck.getString(0) + " from toCheck");
                toCheck.remove(0);

                // adding all connectedNodes in response to toCheck if they are not yet in the array
                for(int i=0; i<connectedNodes.length(); i++)
                {
                    if(!inArray(toCheck, connectedNodes.getString(i)) && !inArray(foundNodes, connectedNodes.getString(i)))
                    {
                        //System.out.println("adding " + connectedNodes.get(i) + " to toCheck");
                        toCheck.put(connectedNodes.get(i));
                    }
                }
            }
            catch(Exception e)
            {
                // removing currently checked node from toCheck
                toCheck.remove(0);

                e.printStackTrace();
            }

            //System.out.println("in array foundNodes: " + foundNodes.toString() + "\n\n");
        }

        return foundNodes;
    }

    private static boolean inArray(JSONArray json, String token)
    {
        for(int i=0; i<json.length(); i++)
        {
            if(json.getString(i).equals(token))
            {
                return true;
            }
        }

        return false;
    }

    private static String setActiveNode()
    {
        try
        {
            Scanner in = new Scanner(System.in);
            System.out.print("specify node ip address [" + defaultNode + "]:");
            String inNodeAddress = in.nextLine();
            in.close();

            // TODO verify format
            return inNodeAddress;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return defaultNode;
        }
    }

    private static void testNodes()
    {
        for(int i=0; i<nodes.length(); i++)
        {
            System.out.print("Testing " + nodes.getString(i) + " ... ");
            String response = GlobalRegistryAPI.getStatus(nodes.getString(i) + ":" + GREG_PORT);

            if(response == null)
                System.out.print("FAILED!\n");
            else
            {
                JSONObject jresponse = new JSONObject(response);

                if(jresponse.getInt("Code") == 200)
                    System.out.print("OK! [running version " + jresponse.getJSONObject("version").getString("version") + "]\n");
                else
                    System.out.print("FAILED!\n");
            }
        }
        System.out.print("\n");
    }
}

