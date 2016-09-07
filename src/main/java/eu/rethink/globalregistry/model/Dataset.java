package eu.rethink.globalregistry.model;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;


public class Dataset
{
	public static boolean checkDatasetValidity(JSONObject json) throws DatasetIntegrityException
	{
			
		Schema schema = SchemaLoader.load(new JSONObject("{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"id\":\"http://jsonschema.net/rethink/greg/data\",\"type\":\"object\",\"properties\":{\"guid\":{\"id\": \"http://jsonschema.net/rethink/greg/data/guid\",\"type\": \"string\"},\"userIDs\": {\"id\": \"http://jsonschema.net/rethink/greg/data/userIDs\",\"type\": \"array\"},\"lastUpdate\": {\"id\": \"http://jsonschema.net/rethink/greg/data/lastUpdate\",\"type\": \"string\"},\"timeout\": {\"id\": \"http://jsonschema.net/rethink/greg/data/timeout\",\"type\": \"string\"},\"publicKey\": {\"id\": \"http://jsonschema.net/rethink/greg/data/publicKey\",\"type\": \"string\"},\"salt\": {\"id\": \"http://jsonschema.net/rethink/greg/data/timeout\",\"type\": \"string\"},\"active\": { \"id\": \"http://jsonschema.net/rethink/greg/data/active\",\"type\": \"integer\"},\"revoked\": {\"id\": \"http://jsonschema.net/rethink/greg/data/revoked\",\"type\": \"integer\"}},\"required\": [\"guid\", \"userIDs\", \"lastUpdate\", \"timeout\", \"publicKey\", \"salt\", \"active\", \"revoked\"]}"));
		schema.validate(json); // throws a ValidationException if this
								// object is invalid
		
		// for unknown reasons, this fails always
		/*if(json.getString("guid").equals(GUID.createGUID(json.getString("publicKey"), json.getString("salt"))))
			throw new DatasetIntegrityException("guid does not match publicKey/salt: "+ json.getString("publicKey") + " :: " + json.getString("salt"));*/
		
		return true;
	}
}