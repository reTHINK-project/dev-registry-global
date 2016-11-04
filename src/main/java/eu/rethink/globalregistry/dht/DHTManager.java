package eu.rethink.globalregistry.dht;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.util.List;
import java.util.Random;


import eu.rethink.globalregistry.certification.*;
import eu.rethink.globalregistry.certification.exception.PrivateKeyReadException;
import eu.rethink.globalregistry.certification.exception.X509CertificateReadException;
import net.tomp2p.connection.PeerConnection;
import net.tomp2p.connection.PeerException;
import net.tomp2p.dht.*;
import eu.rethink.globalregistry.configuration.Configuration;
import net.tomp2p.connection.Bindings;
import net.tomp2p.futures.*;
import net.tomp2p.peers.*;
import net.tomp2p.storage.Data;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.replication.IndirectReplication;
import org.apache.commons.codec.binary.Base64;

public class DHTManager
{
	private static DHTManager	instance	= null;

 	private PeerDHT peer;

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
		Number160 peerId = new Number160(Configuration.getInstance().getPeerId());
		X509Reader x509Reader = new X509Reader();
		final CertificateManager certificateManager = new CertificateManager();

		bind.addInterface(Configuration.getInstance().getNetworkInterface());

		try {

			KeyPair keyPair = x509Reader.readKeyPair(peerId.toString());

			X509Certificate ownCertificate = x509Reader.readFromFile(peerId.toString());
			certificateManager.setOwnCertificate(new PeerCertificate(peerId, ownCertificate, true));

			peer = new PeerBuilderDHT(new PeerBuilder(peerId).keyPair(keyPair).ports(Configuration.getInstance().getPortDHT()).start()).start();

			final HandshakeSetup handshake = new HandshakeSetup(peer, certificateManager);

			peer.peerBean().addPeerStatusListener(new PeerStatusListener() {
				@Override
				public boolean peerFailed(PeerAddress remotePeer, PeerException exception) {
					return false;
				}

				@Override
				public boolean peerFound(PeerAddress remotePeer, PeerAddress referrer, PeerConnection peerConnection, RTT roundTripTime) {

					if(!remotePeer.peerId().equals(peer.peerID()) && !certificateManager.exists(remotePeer.peerId())) {
						handshake.askCertificate(remotePeer, peer.peerAddress());
					}

					return false;
				}
			});


			PeerMapConfiguration peerMapConfig = new PeerMapConfiguration(peerId);
			peerMapConfig.addMapPeerFilter(new CertificationPeerMapFilter(certificateManager));
			PeerMap peerMap = new PeerMap(peerMapConfig);

			peer.peer().peerBean().addPeerStatusListener(peerMap);

			//bind new peer map with peer map filter
			peer.peer().peerBean().peerMap(peerMap);

			handshake.init();

			new IndirectReplication(peer).start();

			for(int i=0; i<Configuration.getInstance().getKnownHosts().length; i++)
			{
				InetAddress address = Inet4Address.getByName(Configuration.getInstance().getKnownHosts()[0]);
				FutureDiscover futureDiscover = peer.peer().discover().inetAddress(address).ports(Configuration.getInstance().getPortDHT()).start();
				futureDiscover.awaitUninterruptibly();
				FutureBootstrap futureBootstrap = peer.peer().bootstrap().inetAddress(address).ports(Configuration.getInstance().getPortDHT()).start();

				futureBootstrap.awaitUninterruptibly();
			}

			return this;

		} catch (X509CertificateReadException e) {
			e.printStackTrace();
		} catch (PrivateKeyReadException e) {
			e.printStackTrace();
		}

		return null;
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
		FutureGet futureGet = peer.get(Number160.createHash(key)).start();
		futureGet.awaitUninterruptibly();

		// TODO: use non-blocking?
		if(futureGet.isSuccess() && futureGet.data() != null)
		{
			return futureGet.data().object().toString();
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
		FuturePut futurePut = peer.put(Number160.createHash(key)).data(new Data(value)).start();
		futurePut.awaitUninterruptibly();
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

	public List<PeerAddress> getAllNeighbors() {

		return peer.peerBean().peerMap().all();
	}
}
