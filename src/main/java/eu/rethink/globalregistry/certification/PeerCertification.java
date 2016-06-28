package main.java.eu.rethink.globalregistry.certification;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.java.eu.rethink.globalregistry.exception.CertificateNotFound;
import net.tomp2p.peers.PeerAddress;
import eu.rethink.globalregistry.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PeerCertification {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final GsonFactory GSON_FACTORY = new GsonFactory();
    private static final Gson GSON = new Gson();
    private final HttpRequestFactory requestFactory;

    public PeerCertification() {

        requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                httpRequest.setParser(GSON_FACTORY.createJsonObjectParser());
            }
        });
    }

    public boolean verifyCertificate(PeerAddress peer) throws CertificateNotFound {

        Configuration config = Configuration.getInstance();
        GenericUrl url = new GenericUrl("http://" + config.getBlockchainClient() + "/cert/" + peer.peerId());

        System.out.println("PeerID: " + peer.peerId());

        try {
            HttpRequest httpRequest = requestFactory.buildGetRequest(url);
            HttpResponse response = httpRequest.execute();

            System.out.println(response.parseAsString());

            if(response.getStatusCode() == 200) {
                //TODO: better certificate validation
                Certificate certificate = response.parseAs(Certificate.class);
                return certificate.isValid();
            } else {
                throw new CertificateNotFound("Cannot find certificate for peer with ID: " + peer.peerId() +
                        " and IP: " + peer.inetAddress());
            }

        } catch (IOException e) {
            throw new CertificateNotFound("Cannot find certificate for peer with ID: " + peer.peerId() +
                    " and IP: " + peer.inetAddress());
        }

    }

}
