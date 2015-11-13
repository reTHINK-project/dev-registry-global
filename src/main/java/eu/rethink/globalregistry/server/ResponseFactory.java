package eu.rethink.globalregistry.server;

/**
 * Class that is used for generating the JSON request object
 * 
 * @author Felix Beierle, Sebastian Gšndšr
 *
 */
public class ResponseFactory
{

	public static final int		CODE_INVALID_REQUEST				= 400;
	public static final int		CODE_DATA_NOT_AVAILABLE				= 404;
	public static final int		CODE_OK								= 200;

	public static final String	MESSAGE_INVALID_REQUEST				= "request invalid";
	public static final String	MESSAGE_MALFORMED_REQUEST			= "malformed request";
	public static final String	MESSAGE_INVALID_SIGNATURE			= "invalid signature for token";
	public static final String	MESSAGE_INVALID_PUT_ALREADY_EXISTS	= "data for this guid already exists.";
	public static final String	MESSAGE_DATA_NOT_AVAILABLE			= "data for guid not available";
	public static final String	MESSAGE_OK							= "request was performed successfully";

	private int					responseCode						= 0;
	private String				message								= "";
	private int					errorCode							= 0;

	/**
	 * empty constructor
	 */
	public ResponseFactory()
	{

	}

	/**
	 * Returns the response code.
	 * 
	 * @return responseCode
	 */
	public int getResponseCode()
	{
		return responseCode;
	}

	/**
	 * Sets the response code.
	 * 
	 * @param responseCode
	 */
	public void setResponseCode(int code)
	{
		this.responseCode = code;
	}

	/**
	 * Returns the message.
	 * 
	 * @return message
	 */
	public String getMessage()
	{
		return this.message;
	}

	/**
	 * Sets the status message.
	 * 
	 * @param status_message
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	/**
	 * Returns the error code.
	 * 
	 * @return errorCode
	 */
	public int getErrorCode()
	{
		return this.errorCode;
	}

	/**
	 * Sets the errorCode
	 * 
	 * @param errorCode
	 */
	public void setErrorCode(int code)
	{
		this.errorCode = code;
	}

	/**
	 * Static Factory method for creating a InvalidRequest Response
	 * 
	 * @return response
	 */
	public static ResponseFactory createInvalidRequestResponse()
	{
		ResponseFactory response = new ResponseFactory();
		response.setResponseCode(CODE_INVALID_REQUEST);
		response.setMessage(MESSAGE_INVALID_REQUEST);
		response.setErrorCode(CODE_INVALID_REQUEST);
		return response;
	}

	/**
	 * Static Factory method for creating a DataNotAvailable Response
	 * 
	 * @return response
	 */
	public static ResponseFactory createDataNotFoundResponse()
	{
		ResponseFactory response = new ResponseFactory();
		response.setResponseCode(CODE_DATA_NOT_AVAILABLE);
		response.setMessage(MESSAGE_DATA_NOT_AVAILABLE);
		response.setErrorCode(CODE_DATA_NOT_AVAILABLE);
		return response;
	}

	/**
	 * Static Factory method for creating a OK Response
	 * 
	 * @return response
	 */
	public static ResponseFactory createOKResponse()
	{
		ResponseFactory response = new ResponseFactory();
		response.setResponseCode(CODE_OK);
		response.setMessage(MESSAGE_OK);
		response.setErrorCode(CODE_OK);
		return response;
	}

	/**
	 * Static Factory method for creating a InvalidSignature Response
	 * 
	 * @return response
	 */
	public static ResponseFactory createInvalidSignatureResponse()
	{
		ResponseFactory response = new ResponseFactory();
		response.setResponseCode(CODE_INVALID_REQUEST);
		response.setMessage(MESSAGE_INVALID_SIGNATURE);
		response.setErrorCode(CODE_INVALID_REQUEST);
		return response;
	}
}
