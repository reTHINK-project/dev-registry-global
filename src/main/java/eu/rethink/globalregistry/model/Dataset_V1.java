package eu.rethink.globalregistry.model;

import eu.rethink.globalregistry.util.XSDDateTime;
import org.apache.commons.codec.binary.Base64;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;


public class Dataset_V1 extends AbstractDataset {

    public Dataset_V1(){
        super();
    }


    public static Dataset_V1 createFromJSONObject(JSONObject json) {
        Dataset_V1 datasetV1 =  new Dataset_V1();

        datasetV1.setSchemaVersion(json.getInt("schemaVersion"));
        datasetV1.setActive(json.getInt("active"));
        datasetV1.setRevoked(json.getInt("revoked"));
        datasetV1.setGUID(json.getString("guid"));
        datasetV1.setPublicKey(json.getString("publicKey"));
        datasetV1.setSalt(json.getString("salt"));
        datasetV1.setUserIDs(json.getJSONArray("userIDs"));
        datasetV1.setLastUpdate(json.getString("lastUpdate"));
        datasetV1.setTimeout(json.getString("timeout"));
        datasetV1.setDefaults(json.getJSONObject("defaults"));

        /*
        try
        {
            datasetV1.validateSchema(json);
            datasetV1.checkIntegrity(json);
        }
        catch (DatasetIntegrityException e)
        {
            // TODO handle errors like this
            return null;
        }
            */
        return datasetV1;
    }

    public static JSONObject exportJSONObject(Dataset_V1 datasetV1)
    {
        JSONObject json = new JSONObject();

        json.put("schemaVersion", datasetV1.schemaVersion);
        json.put("salt", datasetV1.salt);
        json.put("userIDs", datasetV1.userIDs);
        json.put("lastUpdate", datasetV1.lastUpdate);
        json.put("timeout", datasetV1.timeout);
        json.put("publicKey", datasetV1.publicKey);
        json.put("active", datasetV1.active);
        json.put("revoked", datasetV1.revoked);
        json.put("guid", datasetV1.guid);
        json.put("defaults", datasetV1.defaults);
        //json.put("legacyIDs", datasetV1.legacyIDs);

        return json;
    }

    public boolean validateSchema(JSONObject json) throws DatasetIntegrityException {

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema_v1.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ValidationException e) {
            throw new DatasetIntegrityException("Dataset does not validate against JSON Schema");
        }
        return true;
    }

    //checking integrity is for checking the value of each variable inside dataset, if its correct or not.
    public boolean checkIntegrity(JSONObject json) throws DatasetIntegrityException
    {
        Dataset_V1 datasetV1 = Dataset_V1.createFromJSONObject(json);
        if (datasetV1.getGUID().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'guid' missing");
        if (!datasetV1.getGUID().equals(GUID.createGUID(datasetV1.getPublicKey(), datasetV1.getSalt())))
            throw new DatasetIntegrityException("illegal parameter value...");

        if (datasetV1.getLastUpdate().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'lastUpdate' missing");
        if (!XSDDateTime.validateXSDDateTime(datasetV1.getLastUpdate()))
            throw new DatasetIntegrityException("invalid 'DateTime' format...");

        if(datasetV1.getTimeout().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'timeout' missing");
        if (!XSDDateTime.validateXSDDateTime(datasetV1.getTimeout()))
            throw new DatasetIntegrityException("invalid 'DateTime' format...");

        if (datasetV1.getPublicKey().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'publicKey' missing");
        String stringtobechecked = datasetV1.getPublicKey().substring(26, datasetV1.getPublicKey().length()-24);
        if (!Base64.isArrayByteBase64(stringtobechecked.getBytes()))
            throw new DatasetIntegrityException("invalid 'PublicKey' character set...");

        if (datasetV1.getSalt().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'salt' missing");
        if (!Base64.isArrayByteBase64(datasetV1.getSalt().getBytes()))
            throw new DatasetIntegrityException("invalid 'Salt' character set...");

        String isactive = Integer.toString(datasetV1.getActive());
        if(isactive.isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'Active' missing");
        if(datasetV1.getActive() != 0 && datasetV1.getActive() != 1)
            throw new DatasetIntegrityException("invalid 'Active' value...");

        String isrevoked = Integer.toString(datasetV1.getRevoked());
        if(isrevoked.isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'revoked' missing");
        if(datasetV1.getRevoked() != 0 && datasetV1.getRevoked() != 1)
            throw new DatasetIntegrityException("invalid 'Revoked' value...");

		/*if(getUserIDs().length() == 0)
			throw new DatasetIntegrityException("mandatory parameter 'userIDs' missing");

        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        for(int n = 0; n < datasetV1.getUserIDs().length(); n++)
        {
            JSONObject object = datasetV1.getUserIDs().getJSONObject(n);
            String uid = object.getString("uID");
            String domain = object.getString("domain");
            String userID = uid + "@" + domain;
            Matcher matcher = pattern.matcher(userID);
            if(!matcher.matches())
                throw new DatasetIntegrityException("invalid 'UserID' value for " + userID);
        }
        */
        return true;
    }
}
