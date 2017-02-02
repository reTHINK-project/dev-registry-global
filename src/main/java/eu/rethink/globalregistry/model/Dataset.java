package eu.rethink.globalregistry.model;

import java.io.IOException;
import java.io.InputStream;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.rethink.globalregistry.util.XSDDateTime;
import io.jsonwebtoken.impl.Base64UrlCodec;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONTokener;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.commons.codec.binary.Base64;

import javax.validation.constraints.Null;

/**
 * Data object for handling the dataset
 * 
 * @date 12.01.2017
 * @version 1
 * @author Sebastian Göndör, Parth Singh
 */
public class Dataset
{
	protected String guid;
	protected String salt;
	protected JSONArray userIDs;
	protected String lastUpdate;
	protected String timeout;
	protected String publicKey;
	protected int active;
	protected int revoked;
	protected JSONObject defaults;
	protected int schemaVersion;
	protected JSONArray legacyIDs;
	
	private Dataset()
	{}
	
	// TODO finish deserialize functionality
	public static Dataset createFromJSONObject(JSONObject json)
	{
		Dataset dataset = new Dataset();
		
		dataset.setSchemaVersion(json.getInt("schemaVersion"));
		dataset.setActive(json.getInt("active"));
		dataset.setRevoked(json.getInt("revoked"));
		dataset.setGUID(json.getString("guid"));
		dataset.setPublicKey(json.getString("publicKey"));
		dataset.setSalt(json.getString("salt"));
		dataset.setUserIDs(json.getJSONArray("userIDs"));
		dataset.setLastUpdate(json.getString("lastUpdate"));
		dataset.setTimeout(json.getString("timeout"));
		dataset.setDefaults(json.getJSONObject("defaults"));
		dataset.setLegacyIDs(json.getJSONArray("legacyIDs"));
		
		try
		{
			dataset.validateSchema();
			dataset.checkIntegrity();
		}
		catch (DatasetIntegrityException e)
		{
			// TODO handle errors like this
			return null;
		}

		return dataset;
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
	
	public String getGUID()
	{
		return guid;
	}
	
	public void setGUID(String guid)
	{
		this.guid = guid;
	}
	
	public int getSchemaVersion()
	{
		return schemaVersion;
	}
	
	public void setSchemaVersion(int schemaVersion)
	{
		this.schemaVersion = schemaVersion;
	}
	
	public String getSalt()
	{
		return salt;
	}
	
	public void setSalt(String salt)
	{
		this.salt = salt;
	}
	
	public JSONArray getUserIDs()
	{
		return userIDs;
	}
	
	public JSONObject getUserIDs(int index)
	{
		return userIDs.getJSONObject(index);
	}
	
	public void setUserIDs(JSONArray userIDs)
	{
		this.userIDs = userIDs;
	}
	
	public String getLastUpdate()
	{
		return lastUpdate;
	}
	
	public void setLastUpdate(String lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}
	
	public String getTimeout()
	{
		return timeout;
	}
	
	public void setTimeout(String timeout)
	{
		this.timeout = timeout;
	}
	
	public String getPublicKey()
	{
		return publicKey;
	}
	
	public void setPublicKey(String publicKey)
	{
		this.publicKey = publicKey;
	}
	
	public int getActive()
	{
		return active;
	}
	
	public void setActive(int active)
	{
		this.active = active;
	}
	
	public int getRevoked()
	{
		return revoked;
	}
	
	public void setRevoked(int revoked)
	{
		this.revoked = revoked;
	}
	
	public JSONObject getDefaults()
	{
		return defaults;
	}
	
	public void setDefaults(JSONObject defaults)
	{
		this.defaults = defaults;
	}
	
	public JSONArray getLegacyIDs()
	{
		return legacyIDs;
	}
	
	public void setLegacyIDs(JSONArray legacyIDs)
	{
		this.legacyIDs = legacyIDs;
	}


	//validate schema is for checking the given datset against its schema format and structure.
	public boolean validateSchema() throws DatasetIntegrityException {
		int version = this.schemaVersion;
		switch (version) {
			case 1:
				try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.json")) {
					JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
					Schema schema = SchemaLoader.load(rawSchema);
					schema.validate(exportJSONObject());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ValidationException e) {
					throw new DatasetIntegrityException("Dataset does not validate against JSON Schema");
				}
				break;
			case 2:
				try (InputStream inputStream = getClass().getResourceAsStream("schema2.json")) {
					JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
					Schema schema = SchemaLoader.load(rawSchema);
					schema.validate(exportJSONObject());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ValidationException e) {
					throw new DatasetIntegrityException("Dataset does not validate against JSON Schema");
				}
				break;
			default:
				throw new IllegalArgumentException("No such schema version exist");
		}
		return true;
	}

    //checking integrity is for checking the value of each variable inside dataset, if its correct or not.
	public boolean checkIntegrity() throws DatasetIntegrityException
	{

		if (getGUID().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'guid' missing");
		if (!getGUID().equals(GUID.createGUID(this.getPublicKey(), this.getSalt())))
			throw new DatasetIntegrityException("illegal parameter value...");

		if (getLastUpdate().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'lastUpdate' missing");
		if (!XSDDateTime.validateXSDDateTime(getLastUpdate()))
			throw new DatasetIntegrityException("invalid 'DateTime' format...");

		if(getTimeout().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'timeout' missing");
		if (!XSDDateTime.validateXSDDateTime(getTimeout()))
			throw new DatasetIntegrityException("invalid 'DateTime' format...");

		if (getPublicKey().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'publicKey' missing");
		String stringtobechecked = getPublicKey().substring(26, getPublicKey().length()-24);
		if (!Base64.isArrayByteBase64(stringtobechecked.getBytes()))
			throw new DatasetIntegrityException("invalid 'PublicKey' character set...");

		if (getSalt().isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'salt' missing");
		if (!Base64.isArrayByteBase64(getSalt().getBytes()))
			throw new DatasetIntegrityException("invalid 'Salt' character set...");

		String isactive = Integer.toString(getActive());
		if(isactive.isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'Active' missing");
		if(getActive() != 0 && getActive() != 1)
			throw new DatasetIntegrityException("invalid 'Active' value...");

		String isrevoked = Integer.toString(getRevoked());
		if(isrevoked.isEmpty())
			throw new DatasetIntegrityException("mandatory parameter 'revoked' missing");
		if(getRevoked() != 0 && getRevoked() != 1)
			throw new DatasetIntegrityException("invalid 'Revoked' value...");

		/*if(getUserIDs().length() == 0)
			throw new DatasetIntegrityException("mandatory parameter 'userIDs' missing");*/

		String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
		Pattern pattern = Pattern.compile(regex);
		for(int n = 0; n < getUserIDs().length(); n++)
		{
			JSONObject object = getUserIDs().getJSONObject(n);
			String uid = object.getString("uID");
			String domain = object.getString("domain");
			String userID = uid + "@" + domain;
			Matcher matcher = pattern.matcher(userID);
			if(!matcher.matches())
				throw new DatasetIntegrityException("invalid 'UserID' value for " + userID);
		}

		if(getLegacyIDs().length() == 0)
			throw new DatasetIntegrityException("mandatory parameter 'userIDs' missing");
		for(int n = 0; n < getLegacyIDs().length(); n++)
		{
			JSONObject object = getLegacyIDs().getJSONObject(n);
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
	
	/**
	 * @deprecated
	 * @param json
	 * @return
	 * @throws DatasetIntegrityException
	 */
	@Deprecated
	public static boolean checkDatasetValidity(JSONObject json) throws DatasetIntegrityException
	{
		if(!json.has("guid"))
			throw new DatasetIntegrityException("mandatory parameter 'guid' missing");
		if(!json.has("schemaVersion"))
			throw new DatasetIntegrityException("mandatory parameter 'schemaVersion' missing");
		
		// TODO: userIDs are now objects. Rewrite check
		if(!json.has("userIDs"))
			throw new DatasetIntegrityException("mandatory parameter 'userIDs' missing");
		if(!json.has("lastUpdate"))
			throw new DatasetIntegrityException("mandatory parameter 'lastUpdate' missing");
		if(!json.has("timeout"))
			throw new DatasetIntegrityException("mandatory parameter 'timeout' missing");
		if(!json.has("publicKey"))
			throw new DatasetIntegrityException("mandatory parameter 'publicKey' missing");
		if(!json.has("salt"))
			throw new DatasetIntegrityException("mandatory parameter 'salt' missing");
		if(!json.has("revoked"))
			throw new DatasetIntegrityException("mandatory parameter 'revoked' missing");
		if(!json.has("defaults"))
			throw new DatasetIntegrityException("mandatory parameter 'defaults' missing");
		if(!json.getJSONObject("defaults").has("voice"))
			throw new DatasetIntegrityException("mandatory parameter 'defaults : voice' missing");
		if(!json.getJSONObject("defaults").has("chat"))
			throw new DatasetIntegrityException("mandatory parameter 'defaults : chat' missing");
		if(!json.getJSONObject("defaults").has("video"))
			throw new DatasetIntegrityException("mandatory parameter 'defaults : video' missing");
		// for unknown reasons, this fails always
		/*if(json.getString("guid").equals(GUID.createGUID(json.getString("publicKey"), json.getString("salt"))))
			throw new DatasetIntegrityException("guid does not match publicKey/salt: "+ json.getString("publicKey") + " :: " + json.getString("salt"));*/
		
		return true;
	}
}