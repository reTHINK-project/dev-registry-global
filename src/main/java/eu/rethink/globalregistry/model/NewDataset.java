package eu.rethink.globalregistry.model;

import eu.rethink.globalregistry.util.XSDDateTime;
import org.apache.commons.codec.binary.Base64;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Half-Blood on 2/6/2017.
 */
public class NewDataset extends AbstractDataset {

    public NewDataset(){
        super();
    }

    public JSONArray getLegacyIDs() {
        return legacyIDs;
    }


    public void setLegacyIDs(JSONArray legacyIDs) {
        this.legacyIDs = legacyIDs;
    }

    public static NewDataset createFromJSONObject(JSONObject json)
    {
        NewDataset newDataset = new NewDataset();

        newDataset.setSchemaVersion(json.getInt("schemaVersion"));
        newDataset.setActive(json.getInt("active"));
        newDataset.setRevoked(json.getInt("revoked"));
        newDataset.setGUID(json.getString("guid"));
        newDataset.setPublicKey(json.getString("publicKey"));
        newDataset.setSalt(json.getString("salt"));
        newDataset.setUserIDs(json.getJSONArray("userIDs"));
        newDataset.setLastUpdate(json.getString("lastUpdate"));
        newDataset.setTimeout(json.getString("timeout"));
        newDataset.setDefaults(json.getJSONObject("defaults"));
        newDataset.setLegacyIDs(json.getJSONArray("legacyIDs"));

        /*
        try
        {
            newDataset.validateSchema(json);
            newDataset.checkIntegrity(json);
        }
        catch (DatasetIntegrityException e)
        {
            // TODO handle errors like this
            return null;
        }
           */
        return newDataset;
    }

    public JSONObject exportJSONObject()
    {
        JSONObject json = new JSONObject();

        json.put("schemaVersion", this.schemaVersion);
        json.put("salt", this.salt);
        json.put("userIDs", this.userIDs);
        json.put("lastUpdate", this.lastUpdate);
        json.put("timeout", this.timeout);
        json.put("publicKey", this.publicKey);
        json.put("active", this.active);
        json.put("revoked", this.revoked);
        json.put("guid", this.guid);
        json.put("defaults", this.defaults);
        json.put("legacyIDs", this.legacyIDs);

        return json;
    }

    public boolean validateSchema(JSONObject json) throws DatasetIntegrityException {

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema2.json")) {
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
        NewDataset newDataset = createFromJSONObject(json);
        if (newDataset.getGUID().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'guid' missing");
        if (!newDataset.getGUID().equals(GUID.createGUID(newDataset.getPublicKey(), newDataset.getSalt())))
            throw new DatasetIntegrityException("illegal parameter value...");

        if (newDataset.getLastUpdate().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'lastUpdate' missing");
        if (!XSDDateTime.validateXSDDateTime(newDataset.getLastUpdate()))
            throw new DatasetIntegrityException("invalid 'DateTime' format...");

        if(newDataset.getTimeout().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'timeout' missing");
        if (!XSDDateTime.validateXSDDateTime(newDataset.getTimeout()))
            throw new DatasetIntegrityException("invalid 'DateTime' format...");

        if (newDataset.getPublicKey().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'publicKey' missing");
        String stringtobechecked = newDataset.getPublicKey().substring(26, newDataset.getPublicKey().length()-24);
        if (!Base64.isArrayByteBase64(stringtobechecked.getBytes()))
            throw new DatasetIntegrityException("invalid 'PublicKey' character set...");

        if (newDataset.getSalt().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'salt' missing");
        if (!Base64.isArrayByteBase64(newDataset.getSalt().getBytes()))
            throw new DatasetIntegrityException("invalid 'Salt' character set...");

        String isactive = Integer.toString(newDataset.getActive());
        if(isactive.isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'Active' missing");
        if(newDataset.getActive() != 0 && newDataset.getActive() != 1)
            throw new DatasetIntegrityException("invalid 'Active' value...");

        String isrevoked = Integer.toString(newDataset.getRevoked());
        if(isrevoked.isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'revoked' missing");
        if(newDataset.getRevoked() != 0 && newDataset.getRevoked() != 1)
            throw new DatasetIntegrityException("invalid 'Revoked' value...");

		/*if(getUserIDs().length() == 0)
			throw new DatasetIntegrityException("mandatory parameter 'userIDs' missing");*/

        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        for(int n = 0; n < newDataset.getUserIDs().length(); n++)
        {
            JSONObject object = newDataset.getUserIDs().getJSONObject(n);
            String uid = object.getString("uID");
            String domain = object.getString("domain");
            String userID = uid + "@" + domain;
            Matcher matcher = pattern.matcher(userID);
            if(!matcher.matches())
                throw new DatasetIntegrityException("invalid 'UserID' value for " + userID);
        }

        if(newDataset.getLegacyIDs().length() == 0)
            throw new DatasetIntegrityException("mandatory parameter 'userIDs' missing");
        for(int n = 0; n < newDataset.getLegacyIDs().length(); n++)
        {
            JSONObject object = newDataset.getLegacyIDs().getJSONObject(n);
            String type = object.getString("type");
            String category = object.getString("category");
            String description = object.getString("description");
            String id = object.getString("id");
            if ((!Base64.isArrayByteBase64(type.getBytes())) &&
                    (!Base64.isArrayByteBase64(category.getBytes())) &&
                    (!Base64.isArrayByteBase64(description.getBytes())) &&
                    (!Base64.isArrayByteBase64(id.getBytes())))
                throw new DatasetIntegrityException("invalid 'LegacyID' character set...");
        }

        return true;
    }

}
