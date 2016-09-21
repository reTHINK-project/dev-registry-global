package eu.rethink.globalregistry.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class GlobalRegistryAPI
{
	public static String getStatus(String gRegNode)
	{
		HttpURLConnection connection = null;
		
		try
		{
			URL url = new URL("http://" + gRegNode + "/");
			
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			
			connection.setUseCaches(false);
			connection.setDoOutput(false);
			
			//Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			
			String line;
			while((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			
			rd.close();
			
			JSONObject json = new JSONObject(response.toString());
			return json.toString();
		}
		catch(ConnectException e)
		{
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			if(connection != null)
			{
				connection.disconnect(); 
			}
		}
	}
	
	public static String getData(String gRegNode, String guid)
	{
		HttpURLConnection connection = null;
		
		try
		{
			URL url = new URL("http://" + gRegNode + "/guid/" + guid);
			
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			
			connection.setUseCaches(false);
			connection.setDoOutput(false);
			
			//Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			
			String line;
			while((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			
			rd.close();
			
			JSONObject json = new JSONObject(response.toString());
			if(json.has("data"))
				return json.getString("data");
			else
				return json.toString();
		}
		catch(ConnectException e)
		{
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			if(connection != null)
			{
				connection.disconnect(); 
			}
		}
	}
	
	public static String putData(String gRegNode, String guid, String jwt)
	{
		HttpURLConnection connection = null;  
		
		try
		{
			URL url = new URL("http://" + gRegNode + "/guid/" + guid);
			
			connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestMethod("PUT");
			connection.setRequestProperty("Content-Type", "application/text");
			
			connection.setRequestProperty("Content-Length", Integer.toString(jwt.getBytes().length));
			
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
			wr.writeBytes(jwt);
			wr.close();
			
			//Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			
			String line;
			while((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			
			rd.close();
			
			return response.toString();
		}
		catch(ConnectException e)
		{
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			if(connection != null)
			{
				connection.disconnect(); 
			}
		}
	}
}