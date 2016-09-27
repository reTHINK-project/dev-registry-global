package eu.rethink.globalregistry.certification;

import eu.rethink.globalregistry.certification.exception.X509CertificateReadException;
import eu.rethink.globalregistry.certification.model.Message;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class X509Reader {

    public X509Certificate readFromFile(String id) throws X509CertificateReadException {

        PemObject pem;

        try {
            PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(id + ".cert.pem")));
            pem = pemReader.readPemObject();

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(pem.getContent());

            X509Certificate cert = (X509Certificate)certificateFactory.generateCertificate(in);

            pemReader.close();

            return cert;

        } catch (FileNotFoundException e) {
            throw new X509CertificateReadException("Certificate PEM file not found.");
        } catch (IOException | CertificateException e) {
            throw new X509CertificateReadException("Error reading certificate file.");
        }

    }

    public X509Certificate readFromMessage(Message message) throws X509CertificateReadException {

        try {

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(message.getData());

            X509Certificate cert = (X509Certificate)certificateFactory.generateCertificate(in);

            return cert;

        } catch (CertificateException e) {
            throw new X509CertificateReadException("Error reading certificate from data.");
        }

    }
}
