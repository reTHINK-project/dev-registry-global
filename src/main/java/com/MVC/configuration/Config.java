package com.MVC.configuration;

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
		this.networkInterface = "eth0";
		this.connectNode = "133.149.22.133";
		this.logPath = "logs"; // TODO check if this is working
		this.portREST = 5002;
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
}