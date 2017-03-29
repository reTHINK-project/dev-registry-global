package eu.rethink.globalregistry.tests;

import static org.junit.Assert.*;

import java.security.PublicKey;

import eu.rethink.globalregistry.model.DatasetIntegrityException;
import eu.rethink.globalregistry.model.Dataset_V1;
import eu.rethink.globalregistry.model.Dataset_V2;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import io.jsonwebtoken.impl.Base64UrlCodec;

public class DatasetTest
{
	public String	jwt	= "eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKelkyaGxiV0ZXWlhKemFXOXVJam94TENKellXeDBJam9pYVhGd1ptZHpPR2d4WTJSa09IQmpheUlzSW1SbFptRjFiSFJ6SWpwN0luWnZhV05sSWpvaUlpd2lZMmhoZENJNklpSXNJblpwWkdWdklqb2lJbjBzSW5WelpYSkpSSE1pT2x0N0luVkpSQ0k2SW5WelpYSTZMeTloYkdsalpTSXNJbVJ2YldGcGJpSTZJbWR2YjJkc1pTNWpiMjBpZlYwc0lteGhjM1JWY0dSaGRHVWlPaUl5TURFM0xUQXlMVEEzVkRFeU9qVTJPak0zS3pBeE9qQXdJaXdpYkdWbllXTjVTVVJ6SWpwYmV5SmtaWE5qY21sd2RHbHZiaUk2SW5kdmNtc2daVzFoYVd3aUxDSnBaQ0k2SW1Gc2FXTmxRR2R0WVdsc0xtTnZiU0lzSW5SNWNHVWlPaUpsYldGcGJDSXNJbU5oZEdWbmIzSjVJam9pZDI5eWF5SjlYU3dpWVdOMGFYWmxJam94TENKbmRXbGtJam9pWWw4elpVcHhaekl0TmsxMU9HSnJURnBJT1ZoWlIzZ3RVRWxqUnpaR2F5MDBiazh3WHpacmEyaG1TU0lzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZ2YlROSVVrMUlTV3hRT1ZaUk1FczVlSFJ6UVdOaVdWQjFXV0pvV2pacmJIWjBaUzkyV2pGUFVWQkRkakZaVFZOV1EyZFphbTFPTVVGakwzZFlNR0pQVUdSM1JrbERTV1JOU21kdGVtWkdVWEJxYkROclp6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2ljbVYyYjJ0bFpDSTZNQ3dpZEdsdFpXOTFkQ0k2SWpJd01UY3RNRFV0TURoVU1USTZOVFk2TXpjck1ESTZNREFpZlEifQ.kYbwnPUapSCEr7SEQzHVnByX_hN6NHdg1TkZZQVQvm4f9V3Ln-n5BKTqhFfwf_pwB7ehRzWWUfgttB3oYz3uOQ";
	
	JSONObject		data;
	JSONObject		serializedData;
	Dataset_V1		datasetV1;
	Dataset_V2		datasetV2;
	
	@Before
	public void setUp()
	{
		PublicKey newDatasetPublicKey; // the public key of the NEW version
		
		try
		{
			// decode JWT
			JSONObject jwtPayload = new JSONObject(
					new String(Base64UrlCodec.BASE64URL.decodeToString(jwt.split("\\.")[1])));
			data = new JSONObject(Base64UrlCodec.BASE64URL.decodeToString(jwtPayload.get("data").toString()));
		}
		catch(Exception e)
		{
			fail("couldn't decode dataset");
		}
	}
	
	@Test
	public void createDatasetFromJSON()
	{
		// TODO create dataset from JSON
		if(data.getInt("schemaVersion") == 1)
		{
			datasetV1 = Dataset_V1.createFromJSONObject(data);
			assertTrue(datasetV1 != null);
		}
		if(data.getInt("schemaVersion") == 2)
		{
			datasetV2 = Dataset_V2.createFromJSONObject(data);
			assertTrue(datasetV2 != null);
		}
		
	}
	
	@Test
	public void serializeDataset()
	{
		// TODO serialize dataset to JSONObject
		createDatasetFromJSON();
		if(datasetV1 != null)
		{
			serializedData = Dataset_V1.exportJSONObject(datasetV1);
			assertTrue(serializedData != null);
		}
		
		if(datasetV2 != null)
		{
			serializedData = Dataset_V2.exportJSONObject(datasetV2);
			assertTrue(serializedData != null);
		}
	}
	
	@Test
	public void validateDataset() throws DatasetIntegrityException
	{
		// TODO validate dataset
		createDatasetFromJSON();
		if(data.getInt("schemaVersion") == 1)
		{
			assertTrue(datasetV1.validateSchema(data) && datasetV1.checkIntegrity(data));
		}
		
		if(data.getInt("schemaVersion") == 2)
		{
			assertTrue(datasetV2.validateSchema(data) && datasetV2.checkIntegrity(data));
		}
	}
}