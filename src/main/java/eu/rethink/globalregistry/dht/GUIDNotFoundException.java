package eu.rethink.globalregistry.dht;

/**
 * Exception class for handling integrity check failures of the dataset
 * 
 * @date 12.01.2017
 * @version 1
 * @author Sebastian Göndör
 */
public class GUIDNotFoundException extends Exception
{
	private static final long serialVersionUID = 8787254843105440320L;
	
	public GUIDNotFoundException(String message)
	{
		super(message);
	}
	
	public GUIDNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public GUIDNotFoundException(Throwable cause)
	{
		super(cause);
	}
}