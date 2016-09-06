package eu.rethink.globalregistry.certification;

import eu.rethink.globalregistry.certification.exception.InvalidCertificateException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.cert.*;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class X509CertificateVerifier {


    public X509CertificateVerifier() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public PKIXCertPathBuilderResult verifyCertificate(X509Certificate certificate, KeyStore keyStore) throws InvalidCertificateException {
        try {

            if (isSelfSigned(certificate)) {
                throw new InvalidCertificateException("Self-signed certificate.");
            }

            Set<X509Certificate> addCerts = new HashSet<X509Certificate>();

            Enumeration keyEnum = keyStore.aliases();

            while (keyEnum.hasMoreElements()) {
                String alias = (String) keyEnum.nextElement();
                addCerts.add((X509Certificate) keyStore.getCertificate(alias));
            }

            Set<X509Certificate> rootCerts = new HashSet<X509Certificate>();
            Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();

            for (X509Certificate cert : addCerts) {
                if (isSelfSigned(cert)) {
                    rootCerts.add(cert);
                } else {
                    intermediateCerts.add(cert);
                }
            }

            intermediateCerts.add(certificate);

            // Build and verify
            PKIXCertPathBuilderResult verifiedCertChain = verifyCertificate(certificate, rootCerts, intermediateCerts);

            return verifiedCertChain;

        } catch (CertificateException e) {
            throw new InvalidCertificateException("Error verifying the certificate: " + certificate.getSubjectX500Principal());
        } catch (CertPathBuilderException e) {
            throw new InvalidCertificateException("Error building certification path: " + certificate.getSubjectX500Principal());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new InvalidCertificateException("Error verifying the certificate: " + certificate.getSubjectX500Principal());
        } catch (KeyStoreException e) {
            throw new InvalidCertificateException("Error loading keystore");
        }
    }

    private PKIXCertPathBuilderResult verifyCertificate(X509Certificate certificate, Set<X509Certificate> rootCerts, Set<X509Certificate> intermediateCerts) throws InvalidCertificateException, InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, CertPathBuilderException {

        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(certificate);

        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
        for(X509Certificate rootCert : rootCerts) {
            trustAnchors.add(new TrustAnchor(rootCert, null));
        }

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);
        pkixParams.setRevocationEnabled(false);

        // List of intermediate certificates
        CertStore intermediateCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(intermediateCerts), "BC");
        pkixParams.addCertStore(intermediateCertStore);

        // Build cert chain
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");


        //Certificate Revocation
        try {
            PKIXRevocationChecker rc = (PKIXRevocationChecker) builder.getRevocationChecker();
            rc.setOptions(EnumSet.of(PKIXRevocationChecker.Option.PREFER_CRLS));
            pkixParams.addCertPathChecker(rc);
        } catch(UnsupportedOperationException e){
            throw new InvalidCertificateException("Error revocation list");
        }

        PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult)builder.build(pkixParams);

        return result;
    }


    public boolean isSelfSigned(X509Certificate cert) throws NoSuchProviderException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        try {
            PublicKey key = cert.getPublicKey();
            cert.verify(key, "BC");
            return true;
        } catch (InvalidKeyException e) {
            return false;
        } catch (SignatureException e) {
            return false;
        }

    }

}
