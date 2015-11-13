package eu.rethink.globalregistry.server;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * 
 * @author Felix Beierle, Sebastian Gšndšr
 *
 */
public class Services extends Application
{
	private static Set<Object>	services	= new HashSet<Object>();

	public Services()
	{
		services.add(new RestService());
	}

	@Override
	public Set<Object> getSingletons()
	{
		return services;
	}

	public static Set<Object> getServices()
	{
		return services;
	}

}
