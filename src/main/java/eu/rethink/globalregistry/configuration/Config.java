package eu.rethink.globalregistry.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Configuration class of the GlobalRegistry. Using singleton pattern.
 * 
 * @author Sebastian Göndör
 * @version 4
 * @date 29.03.2017
 */
public class Config
{
	private static Config		_singleton					= null;
	
	private static final String	versionName					= "0.3.4";
	private static final int	versionNumber				= 1597;
	private static final String	versionCode					= "springified";
	private static final String	versionDate					= "2017-04-06";
	private static final String	productName					= "reTHINK Global Registry";
	private static final String	productNameShort			= "gReg";
	
	private static final int	portDHT						= 5001;
	private static final int	versionDatasetSchema		= 2;
	private static final int	versionRESTAPI				= 1;
	private static final int	versionDHTAPI				= 1;
	
	//private static final int	reconnectIntervalDefault	= 12;
	private static final String	networkInterfaceDefault		= "eth0";
	private static final String	logPathDefault				= "logs";
	private static final String	connectNodeDefault			= "130.149.22.220";
	private static final int	portRESTDefault				= 5002;
	
	//private int					reconnectInterval;
	private String				networkInterface;
	private String				logPath;
	private String				connectNode;
	private int					portREST;
	
	private Config()
	{
		setDefaultValues();
	}
	
	public static Config getInstance()
	{
		if(_singleton == null)
		{
			_singleton = new Config();
		}
		return _singleton;
	}
	
	private void setDefaultValues()
	{
		//this.reconnectInterval = reconnectIntervalDefault;
		this.networkInterface = networkInterfaceDefault;
		this.connectNode = connectNodeDefault;
		this.logPath = logPathDefault; // TODO check if this is working
		this.portREST = portRESTDefault;
	}
	
//	public int getReconnectInterval()
//	{
//		return reconnectInterval;
//	}
//	
//	public void setReconnectInterval(int reconnectInterval)
//	{
//		this.reconnectInterval = reconnectInterval;
//	}
	
	public String getNetworkInterface()
	{
		return networkInterface;
	}
	
	public void setNetworkInterface(String networkInterface)
	{
		this.networkInterface = networkInterface;
	}
	
	public String getLogPath()
	{
		return logPath;
	}
	
	public void setLogPath(String logPath)
	{
		this.logPath = logPath;
	}
	
	public String getConnectNode()
	{
		return connectNode;
	}
	
	public void setConnectNode(String connectNode)
	{
		this.connectNode = connectNode;
	}
	
	/**
	 * Set the connectNode via a URL, e.g., www.example.com/node
	 * 
	 * @param connectNodeURL
	 */
	public void setConnectNodeViaURL(String connectNodeURL)
	{
		InetAddress address;
		
		try
		{
			address = InetAddress.getByName(connectNodeURL);
		}
		catch(UnknownHostException e)
		{
			this.connectNode = connectNodeDefault;
			return;
		}
		
		this.connectNode = address.getHostAddress();
	}
	
	public int getPortREST()
	{
		return portREST;
	}
	
	public void setPortREST(int portREST)
	{
		this.portREST = portREST;
	}
	
	public int getPortDHT()
	{
		return portDHT;
	}
	
	/**
	 * retrieves the product name as a String
	 *
	 * @return String
	 */
	public String getProductName()
	{
		return productName;
	}
	
	/**
	 * retrieves the product name short as a String
	 *
	 * @return String
	 */
	public String getProductNameShort()
	{
		return productNameShort;
	}
	
	/**
	 * retrieves the version number as a String, e.g. "0.1.2"
	 *
	 * @return String
	 */
	public String getVersionName()
	{
		return versionName;
	}
	
	/**
	 * retrieves the date of the build, e.g. 2014-08-22
	 *
	 * @return String
	 */
	public String getVersionDate()
	{
		return versionDate;
	}
	
	public int getVersionNumber()
	{
		return versionNumber;
	}
	
	public String getVersionCode()
	{
		return versionCode;
	}
	
	public int getVersaionDatasetSchema()
	{
		return versionDatasetSchema;
	}
	
	public int getVersionRESTAPI()
	{
		return versionRESTAPI;
	}
	
	public int getVersionDHTAPI()
	{
		return versionDHTAPI;
	}
}