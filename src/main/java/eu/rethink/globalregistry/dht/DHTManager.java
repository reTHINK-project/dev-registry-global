package eu.rethink.globalregistry.dht;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Random;

import eu.rethink.globalregistry.configuration.Configuration;
import net.tomp2p.connection.Bindings;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

public class DHTManager
{
	private static DHTManager	instance	= null;

	private Peer				peer;

	private DHTManager()
	{

	}

	public static DHTManager getInstance()
	{
		if (instance == null)
			instance = new DHTManager();
		return instance;
	}

	public DHTManager initDHT() throws IOException
	{
		Random rand = new Random();
		
		Bindings bind = new Bindings();
		bind.addInterface(Configuration.getInstance().getNetworkInterface());
		
		// To do this with tomP2P5: https://github.com/tomp2p/TomP2P/blob/master/examples/src/main/java/net/tomp2p/examples/ExampleIndirectReplication.java
		/*
		 * peer = new PeerBuilderDHT(new PeerBuilder(new Number160(rand))
		 * 			.ports(Configuration.getInstance().getPortDHT())
		 * 			.start())
		 * 			.start();
		 * new IndirectReplication(peer).start();
		 */
		
		peer = new PeerMaker(new Number160(rand))
				.setPorts(Configuration.getInstance().getPortDHT())
				.setEnableIndirectReplication(true)
				.makeAndListen();

		peer.getConfiguration().setBehindFirewall(true);


		if (Configuration.getInstance().getNewDHTSystem() == 1)
		{}
		else
		{
			for(int i=0; i<Configuration.getInstance().getKnownHosts().length; i++)
			{
				InetAddress address = Inet4Address.getByName(Configuration.getInstance().getKnownHosts()[i]);
				
				FutureDiscover futureDiscover = peer.discover().setInetAddress(address).setPorts(Configuration.getInstance().getPortDHT()).start();
				futureDiscover.awaitUninterruptibly();
				FutureBootstrap futureBootstrap = peer.bootstrap().setInetAddress(address).setPorts(Configuration.getInstance().getPortDHT()).start();
				futureBootstrap.awaitUninterruptibly();
			}
		}

		return this;
	}

	/**
	 * Retrieves the social record from the DHT.
	 * 
	 * @param key
	 * @return the social record
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public String get(String key) throws ClassNotFoundException, IOException
	{
		FutureDHT futureGet = peer.get(Number160.createHash(key)).start();
		futureGet.awaitUninterruptibly();
		// TODO: use non-blocking?
		if (futureGet.isSuccess())
		{
			return futureGet.getData().getObject().toString();
		}
		return null; // TODO: decide on sentinel value
	}

	/**
	 * Stores the social record in the DHT.
	 * 
	 * @param key
	 *            : the GID
	 * @param value
	 *            : the social record
	 * @throws IOException
	 */
	public void put(String key, String value) throws IOException
	{
		peer.put(Number160.createHash(key)).setData(new Data(value)).start().awaitUninterruptibly();
		// TODO: use non-blocking?
	}
	
	/**
	 * removes a key from the DHT. Should ONLY be used for the tests
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void delete(String key) throws IOException
	{
		peer.remove(Number160.createHash(key)).start();
	}

}