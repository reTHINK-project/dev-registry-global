package eu.rethink.globalregistry.configuration;

/**
 * Configuration class of the GlobalRegistry. Using singleton pattern.
 * 
 * @author Sebastian Göndör
 * @version 1
 * @date 10.01.2017
 */
public class Config
{
	private static Config _singleton = null;

	private static final String		versionName				= "0.3.0";
	private static final int		versionNumber			= 1400;
	private static final String		versionCode				= "springified";
	private static final String		versionDate				= "2017-01-10";
	private static final String		productName				= "reTHINK Global Registry";
	private static final String		productNameShort		= "gReg";

	private static final int		portDHT					= 5001;
	private static final int		versionDatasetSchema	= 1;
	private static final int		versionRESTAPI			= 1;
	private static final int		versionDHTAPI			= 1;

	private static final String		networkInterfaceDefault	= "eth0";
	private static final String		logPathDefault			= "logs";
	private static final String		connectNodeDefault		= "130.149.22.133";
	private static final int		portRESTDefault			= 5002;

	private String networkInterface;
	private String logPath;
	private String connectNode;
	private int portREST;

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
		this.networkInterface = networkInterfaceDefault;
		this.connectNode = connectNodeDefault;
		this.logPath = logPathDefault; // TODO check if this is working
		this.portREST = portRESTDefault;
	}

	public String getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(String networkInterface) {
		this.networkInterface = networkInterface;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	public String getConnectNode() {
		return connectNode;
	}

	public void setConnectNode(String connectNode) {
		this.connectNode = connectNode;
	}

	public int getPortREST() {
		return portREST;
	}

	public void setPortREST(int portREST) {
		this.portREST = portREST;
	}

	public int getPortDHT() {
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