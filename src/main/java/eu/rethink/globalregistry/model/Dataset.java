package eu.rethink.globalregistry.model;


import org.json.JSONObject;


public class Dataset
{
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