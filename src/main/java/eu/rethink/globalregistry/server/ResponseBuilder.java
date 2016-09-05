package eu.rethink.globalregistry.server;

import org.json.JSONObject;

import eu.rethink.globalregistry.configuration.Configuration;

/**
 * Builder class for reTHINK compliant responses
 * 
 * @author Sebastian Göndör
 *
 */
public class ResponseBuilder
{
	private int Code;
	private String Value;
	private String Description;
	
	/**
	 * Returns the code.
	 * 
	 * @return code
	 */
	public int getCode()
	{
		return Code;
	}

	/**
	 * Sets the code.
	 * 
	 * @param code
	 */
	public void code(int code)
	{
		this.Code = code;
	}

	/**
	 * Returns the value.
	 * 
	 * @return value
	 */
	public String getValue()
	{
		return this.Value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 */
	public void value(String value)
	{
		this.Value = value;
	}

	/**
	 * Returns the Description.
	 * 
	 * @return Description
	 */
	public String getDescription()
	{
		return this.Description;
	}

	/**
	 * Sets the Description
	 * 
	 * @param Description
	 */
	public void description(String description)
	{
		this.Description = Description;
	}
	
	public JSONObject build()
	{
		Response r = new Response();
		r.setCode(Code);
		r.setDescription(Description);
		r.setValue(Value);
		
		return new JSONObject(r);
	}
	
	private class Response
	{
		private int Code;
		private String Value;
		private String Description;
		
		/**
		 * Returns the code.
		 * 
		 * @return code
		 */
		public int getCode()
		{
			return Code;
		}

		/**
		 * Sets the code.
		 * 
		 * @param code
		 */
		public void setCode(int code)
		{
			this.Code = code;
		}

		/**
		 * Returns the value.
		 * 
		 * @return value
		 */
		public String getValue()
		{
			return this.Value;
		}

		/**
		 * Sets the value.
		 * 
		 * @param value
		 */
		public void setValue(String value)
		{
			this.Value = value;
		}

		/**
		 * Returns the Description.
		 * 
		 * @return Description
		 */
		public String getDescription()
		{
			return this.Description;
		}

		/**
		 * Sets the Description
		 * 
		 * @param Description
		 */
		public void setDescription(String Description)
		{
			this.Description = Description;
		}
	}
	
	private JSONObject response;
}