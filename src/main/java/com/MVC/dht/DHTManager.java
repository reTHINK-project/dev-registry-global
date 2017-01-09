package com.MVC.dht;

import com.MVC.configuration.Configuration;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.storage.Data;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;

/**
 * Created by Half-Blood on 1/4/2017.
 */
@Repository
public class DHTManager {
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
        bind.addInterface(Configuration.getInstance().getNetworkInterface());
        peer = new PeerBuilderDHT(new PeerBuilder(new Number160(rand)).ports(Configuration.getInstance().getPortDHT()).start()).start();

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

