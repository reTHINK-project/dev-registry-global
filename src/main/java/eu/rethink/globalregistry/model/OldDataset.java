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
public class OldDataset extends AbstractDataset {

    public OldDataset(){
        super();
    }


    public static OldDataset createFromJSONObject(JSONObject json) {
        OldDataset oldDataset =  new OldDataset();

        oldDataset.setSchemaVersion(json.getInt("schemaVersion"));
        oldDataset.setActive(json.getInt("active"));
        oldDataset.setRevoked(json.getInt("revoked"));
        oldDataset.setGUID(json.getString("guid"));
        oldDataset.setPublicKey(json.getString("publicKey"));
        oldDataset.setSalt(json.getString("salt"));
        oldDataset.setUserIDs(json.getJSONArray("userIDs"));
        oldDataset.setLastUpdate(json.getString("lastUpdate"));
        oldDataset.setTimeout(json.getString("timeout"));
        oldDataset.setDefaults(json.getJSONObject("defaults"));

        /*
        try
        {
            oldDataset.validateSchema(json);
            oldDataset.checkIntegrity(json);
        }
        catch (DatasetIntegrityException e)
        {
            // TODO handle errors like this
            return null;
        }
            */
        return oldDataset;
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
        //json.put("legacyIDs", this.legacyIDs);

        return json;
    }

    public boolean validateSchema(JSONObject json) throws DatasetIntegrityException {

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.json")) {
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
        OldDataset oldDataset = OldDataset.createFromJSONObject(json);
        if (oldDataset.getGUID().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'guid' missing");
        if (!oldDataset.getGUID().equals(GUID.createGUID(oldDataset.getPublicKey(), oldDataset.getSalt())))
            throw new DatasetIntegrityException("illegal parameter value...");

        if (oldDataset.getLastUpdate().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'lastUpdate' missing");
        if (!XSDDateTime.validateXSDDateTime(oldDataset.getLastUpdate()))
            throw new DatasetIntegrityException("invalid 'DateTime' format...");

        if(oldDataset.getTimeout().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'timeout' missing");
        if (!XSDDateTime.validateXSDDateTime(oldDataset.getTimeout()))
            throw new DatasetIntegrityException("invalid 'DateTime' format...");

        if (oldDataset.getPublicKey().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'publicKey' missing");
        String stringtobechecked = oldDataset.getPublicKey().substring(26, oldDataset.getPublicKey().length()-24);
        if (!Base64.isArrayByteBase64(stringtobechecked.getBytes()))
            throw new DatasetIntegrityException("invalid 'PublicKey' character set...");

        if (oldDataset.getSalt().isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'salt' missing");
        if (!Base64.isArrayByteBase64(oldDataset.getSalt().getBytes()))
            throw new DatasetIntegrityException("invalid 'Salt' character set...");

        String isactive = Integer.toString(oldDataset.getActive());
        if(isactive.isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'Active' missing");
        if(oldDataset.getActive() != 0 && oldDataset.getActive() != 1)
            throw new DatasetIntegrityException("invalid 'Active' value...");

        String isrevoked = Integer.toString(oldDataset.getRevoked());
        if(isrevoked.isEmpty())
            throw new DatasetIntegrityException("mandatory parameter 'revoked' missing");
        if(oldDataset.getRevoked() != 0 && oldDataset.getRevoked() != 1)
            throw new DatasetIntegrityException("invalid 'Revoked' value...");

		/*if(getUserIDs().length() == 0)
			throw new DatasetIntegrityException("mandatory parameter 'userIDs' missing");*/

        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        for(int n = 0; n < oldDataset.getUserIDs().length(); n++)
        {
            JSONObject object = oldDataset.getUserIDs().getJSONObject(n);
            String uid = object.getString("uID");
            String domain = object.getString("domain");
            String userID = uid + "@" + domain;
            Matcher matcher = pattern.matcher(userID);
            if(!matcher.matches())
                throw new DatasetIntegrityException("invalid 'UserID' value for " + userID);
        }

        return true;
    }
}
