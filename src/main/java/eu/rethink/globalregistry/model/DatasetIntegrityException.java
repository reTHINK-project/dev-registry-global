package eu.rethink.globalregistry.model;

public class DatasetIntegrityException extends Exception
{
	private static final long serialVersionUID = 8787254843105440320L;

	public DatasetIntegrityException(String msg)
	{
		super(msg);
	}
}