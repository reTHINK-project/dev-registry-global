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
public class Dataset_V2 extends AbstractDataset
{
	public Dataset_V2()
	{
		super();
	}
	
	public JSONArray getLegacyIDs()
	{
		return legacyIDs;
	}
	
	public void setLegacyIDs(JSONArray legacyIDs)
	{
		this.legacyIDs = legacyIDs;
	}
	
	public static Dataset_V2 createFromJSONObject(JSONObject json)
	{
		Dataset_V2 datasetV2 = new Dataset_V2();
		
		datasetV2.setSchemaVersion(json.getInt("schemaVersion"));
		datasetV2.setActive(json.getInt("active"));
		datasetV2.setRevoked(json.getInt("revoked"));
		datasetV2.setGUID(json.getString("guid"));
		datasetV2.setPublicKey(json.getString("publicKey"));
		datasetV2.setSalt(json.getString("salt"));
		datasetV2.setUserIDs(json.getJSONArray("userIDs"));
		datasetV2.setLastUpdate(json.getString("lastUpdate"));
		datasetV2.setTimeout(json.getString("timeout"));
		datasetV2.setDefaults(json.getJSONObject("defaults"));
		datasetV2.setLegacyIDs(json.getJSONArray("legacyIDs"));
		
		/*
		 * try { datasetV2.validateSchema(json); datasetV2.checkIntegrity(json);
		 * } catch (DatasetIntegrityException e) { // TODO handle errors like
		 * this return null; }
		 */
		return datasetV2;
	}
	
	public static JSONObject exportJSONObject(Dataset_V2 datasetV2)
	{
		JSONObject json = new JSONObject();
		
		json.put("schemaVersion", datasetV2.schemaVersion);
		json.put("salt", datasetV2.salt);
		json.put("userIDs", datasetV2.userIDs);
		json.put("lastUpdate", datasetV2.lastUpdate);
		json.put("timeout", datasetV2.timeout);
		json.put("publicKey", datasetV2.publicKey);
		json.put("active", datasetV2.active);
		json.put("revoked", datasetV2.revoked);
		json.put("guid", datasetV2.guid);
		json.put("defaults", datasetV2.defaults);
		json.put("legacyIDs", datasetV2.legacyIDs);
		
		return json;
	}
	
	public boolean validateSchema(JSONObject json) throws DatasetIntegrityException
	{
		
		try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema_v2.json"))
		{
			JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
			Schema schema = SchemaLoader.load(rawSchema);
			schema.validate(json);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(ValidationException e)
		{
			throw new DatasetIntegrityException("Dataset does not validate against JSON Schema");
		}
		return true;
	}
	
	// checking integrity is for checking the value of each variable inside
	// dataset, if its correct or not.
	public boolean checkIntegrity(JSONObject json) throws DatasetIntegrityException
	{
		Dataset_V2 datasetV2 = createFromJSONObject(json);
		if(datasetV2.getGUID().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'guid' missing");
		if(!datasetV2.getGUID().equals(GUID.createGUID(datasetV2.getPublicKey(), datasetV2.getSalt())))
			throw new DatasetIntegrityException("illegal parameter value...");
		
		if(datasetV2.getLastUpdate().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'lastUpdate' missing");
		if(!XSDDateTime.validateXSDDateTime(datasetV2.getLastUpdate()))
			throw new DatasetIntegrityException("invalid 'DateTime' format...");
		
		if(datasetV2.getTimeout().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'timeout' missing");
		if(!XSDDateTime.validateXSDDateTime(datasetV2.getTimeout()))
			throw new DatasetIntegrityException("invalid 'DateTime' format...");
		
		if(datasetV2.getPublicKey().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'publicKey' missing");
		String stringtobechecked = datasetV2.getPublicKey().substring(26, datasetV2.getPublicKey().length() - 24);
		if(!Base64.isArrayByteBase64(stringtobechecked.getBytes()))
			throw new DatasetIntegrityException("invalid 'PublicKey' character set...");
		
		if(datasetV2.getSalt().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'salt' missing");
		if(!Base64.isArrayByteBase64(datasetV2.getSalt().getBytes()))
			throw new DatasetIntegrityException("invalid 'Salt' character set...");
		
		String isactive = Integer.toString(datasetV2.getActive());
		if(isactive.isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'Active' missing");
		if(datasetV2.getActive() != 0 && datasetV2.getActive() != 1)
			throw new DatasetIntegrityException("invalid 'Active' value...");
		
		String isrevoked = Integer.toString(datasetV2.getRevoked());
		if(isrevoked.isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'revoked' missing");
		if(datasetV2.getRevoked() != 0 && datasetV2.getRevoked() != 1)
			throw new DatasetIntegrityException("invalid 'Revoked' value...");
		
		/*
		 * if(getUserIDs().length() == 0) throw new
		 * DatasetIntegrityException("mandatory parameter 'userIDs' missing");
		 * 
		 * String regex =
		 * "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
		 * Pattern pattern = Pattern.compile(regex); for(int n = 0; n <
		 * datasetV2.getUserIDs().length(); n++) { JSONObject object =
		 * datasetV2.getUserIDs().getJSONObject(n); String uid =
		 * object.getString("uID"); String domain = object.getString("domain");
		 * String userID = uid + "@" + domain; Matcher matcher =
		 * pattern.matcher(userID); if(!matcher.matches()) throw new
		 * DatasetIntegrityException("invalid 'UserID' value for " + userID); }
		 */
		
		if(datasetV2.getLegacyIDs().length() == 0)
			throw new DatasetIntegrityException("mandatory parameter 'userIDs' missing");
		for(int n = 0; n < datasetV2.getLegacyIDs().length(); n++)
		{
			JSONObject object = datasetV2.getLegacyIDs().getJSONObject(n);
			String type = object.getString("type");
			String category = object.getString("category");
			String description = object.getString("description");
			String id = object.getString("id");
			if((!Base64.isArrayByteBase64(type.getBytes())) && (!Base64.isArrayByteBase64(category.getBytes()))
					&& (!Base64.isArrayByteBase64(description.getBytes()))
					&& (!Base64.isArrayByteBase64(id.getBytes())))
				throw new DatasetIntegrityException("invalid 'LegacyID' character set...");
		}
		
		return true;
	}
}