package eu.rethink.globalregistry.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rethink.globalregistry.configuration.Configuration;


/**
 * Starts a simple embedded Jetty Server to server HTTP-access to TomP2P.
 * 
 * @author Felix Beierle, Sebastian G�nd�r
 *
 */
public class JettyServer
{
	private static final Logger	LOGGER	= LoggerFactory.getLogger(JettyServer.class);

	public static void start() throws Exception
	{
		Server server = new Server(Configuration.getInstance().getPortServer());

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		ServletHolder h = new ServletHolder(new HttpServletDispatcher());
		h.setInitParameter("javax.ws.rs.Application", "eu.rethink.globalregistry.server.Services");
		context.addServlet(h, "/*");

		server.setHandler(context);

		server.start();
		server.join();

		LOGGER.debug("JettyServer started");
	}

}