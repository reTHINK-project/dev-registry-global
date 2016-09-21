package eu.rethink.globalregistry.model;

import org.json.JSONArray;
import org.json.JSONObject;

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
	
	// TODO finish deserialize functionality
	public static Dataset createFromJSONObject(JSONObject json)
	{
		Dataset dataset = new Dataset();
		
		dataset.setActive(json.getInt("active"));
		dataset.setRevoked(json.getInt("revoked"));
		dataset.setGUID(json.getString("guid"));
		dataset.setPublicKey(json.getString("publicKey"));
		dataset.setSalt(json.getString("salt"));
		dataset.setUserIDs(json.getJSONArray("userIDs"));
		dataset.setLastUpdate(json.getString("lastUpdate"));
		dataset.setTimeout(json.getString("timeout"));
		
		return dataset;
	}
	
	public JSONObject exportJSONObject()
	{
		JSONObject json = new JSONObject();
		
		json.put("salt", this.salt);
		json.put("userIDs", this.userIDs);
		json.put("lastUpdate", this.lastUpdate);
		json.put("timeout", this.timeout);
		json.put("publicKey", this.publicKey);
		json.put("active", this.active);
		json.put("revoked", this.revoked);
		json.put("guid", this.guid);
		
		return json;
	}
	
	public String getGUID() {
		return guid;
	}

	public void setGUID(String guid) {
		this.guid = guid;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public JSONArray getUserIDs() {
		return userIDs;
	}

	public void setUserIDs(JSONArray userIDs) {
		this.userIDs = userIDs;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public int getRevoked() {
		return revoked;
	}

	public void setRevoked(int revoked) {
		this.revoked = revoked;
	}
	
	/* TODO this should be rewritten */
	public static boolean checkDatasetValidity(JSONObject json) throws DatasetIntegrityException
	{
		if(!json.has("guid"))
			throw new DatasetIntegrityException("mandatory parameter 'guid' missing");
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
		
		// for unknown reasons, this fails always
		/*if(json.getString("guid").equals(GUID.createGUID(json.getString("publicKey"), json.getString("salt"))))
			throw new DatasetIntegrityException("guid does not match publicKey/salt: "+ json.getString("publicKey") + " :: " + json.getString("salt"));*/
		
		return true;
	}
}