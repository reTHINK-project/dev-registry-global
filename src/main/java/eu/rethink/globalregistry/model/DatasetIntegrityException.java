package eu.rethink.globalregistry.model;

/**
 * Exception class for handling integrity check failures of the dataset
 * 
 * @date 12.01.2017
 * @version 1
 * @author Sebastian Göndör
 */
public class DatasetIntegrityException extends Exception
{
	private static final long serialVersionUID = 8787254843105440320L;
	
	public DatasetIntegrityException(String message)
	{
		super(message);
	}
	
	public DatasetIntegrityException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public DatasetIntegrityException(Throwable cause)
	{
		super(cause);
	}
}