package fr.asipsante.api.sign.ws.bean;

import eu.europa.esig.dss.DSSException;
import fr.asipsante.api.sign.bean.parameters.CertificateValidationParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureValidationParameters;
import fr.asipsante.api.sign.enums.DigestAlgorithm;
import fr.asipsante.api.sign.enums.RestrictedSignaturePackaging;
import fr.asipsante.api.sign.ws.bean.object.CertVerifConf;
import fr.asipsante.api.sign.ws.bean.object.ProofConf;
import fr.asipsante.api.sign.ws.bean.object.SignVerifConf;
import fr.asipsante.api.sign.ws.bean.object.SignatureConf;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

/**
 * ConfigurationLoader Class.
 */
public class ConfigurationLoader {

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(ConfigurationLoader.class);

    /**
     * Load conf sign.
     *
     * @param signConf the signature configuration
     * @return the signature parameters
     */
    public SignatureParameters loadSignConf(SignatureConf signConf) {
        final SignatureParameters params = new SignatureParameters();
        params.setDescription(signConf.getDescription());
        params.setCanonAlgo(signConf.getCanonicalisationAlgorithm());
        params.setDigestAlgo(DigestAlgorithm.valueOf(signConf.getDigestAlgorithm()));
        params.setSignPackaging(RestrictedSignaturePackaging.valueOf(signConf.getSignaturePackaging()));
        params.setObjectId(signConf.getObjectId());
        params.setSignId(signConf.getSignId());
        params.setSignValueId(signConf.getSignValueId());

        String ksPass = RandomStringUtils.randomAscii(32);

        try {
            File ksFile = generateKsFile(signConf.getCertificate(), signConf.getPrivateKey(), ksPass);
            params.setKsFile(ksFile);
        } catch (GeneralSecurityException | IOException e) {
            log.error(signConf.getIdSignConf(), "Erreur lors de la génération du Key Store " +
                    "pour la configuration de signature : {}");
        }
        return params;
    }

    /**
     * Load conf proof sign.
     *
     * @param proofConf the proof signature configuration
     * @return the signature parameters
     */
    public SignatureParameters loadProofSignConf(ProofConf proofConf) {
        final SignatureParameters params = new SignatureParameters();
        params.setDescription(proofConf.getDescription());
        params.setCanonAlgo(proofConf.getCanonicalisationAlgorithm());
        params.setDigestAlgo(DigestAlgorithm.valueOf(proofConf.getDigestAlgorithm()));
        params.setSignPackaging(RestrictedSignaturePackaging.valueOf(proofConf.getSignaturePackaging()));

        String ksPass = RandomStringUtils.randomAscii(32);

        try {
            File ksFile = generateKsFile(proofConf.getCertificate(), proofConf.getPrivateKey(), ksPass);
            params.setKsFile(ksFile);
        } catch (GeneralSecurityException | IOException e) {
            log.error(proofConf.getIdProofConf(), "Erreur lors de la génération du Key Store " +
                    "pour la configuration de signature de preuve : {}");
        }
        return params;
    }

    /**
     * Load conf sign validation.
     *
     * @param signVerifConf the sign validation conf
     * @return the signature validation parameters
     */
    public SignatureValidationParameters loadSignVerifConf(SignVerifConf signVerifConf) {
        final SignatureValidationParameters signValidationParams = new SignatureValidationParameters();
        signValidationParams.setDescription(signVerifConf.getDescription());
        signValidationParams.loadRules(Arrays.asList(signVerifConf.getRules().trim().split(",")));
        try {
            signValidationParams.loadMetadata(Arrays.asList(signVerifConf.getMetadata().trim().split(",")));
        } catch (final IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return signValidationParams;
    }

    /**
     * Load conf sign validation.
     *
     * @param certVerifConf the cert validation conf
     * @return the certificate validation parameters
     */
    public CertificateValidationParameters loadCertVerifConf(CertVerifConf certVerifConf) {
        final CertificateValidationParameters certValidationParams = new CertificateValidationParameters();
        certValidationParams.setDescription(certVerifConf.getDescription());
        certValidationParams.loadRules(Arrays.asList(certVerifConf.getRules().trim().split(",")));
        try {
            certValidationParams.loadMetadata(Arrays.asList(certVerifConf.getMetadata().trim().split(",")));
        } catch (final IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return certValidationParams;
    }

    /**
     * Generate Key Store File.
     *
     * @param certificate the certificate
     * @param privateKey the private key
     * @param ksPass password
     * @return ksFile
     * @throws GeneralSecurityException java security related exceptions
     * @throws IOException file writing exception
     */
    private File generateKsFile(String certificate, String privateKey, String ksPass) throws GeneralSecurityException, IOException {

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(null, ksPass.toCharArray());

        CertificateFactory fac = CertificateFactory.getInstance("X509");
        ByteArrayInputStream in = new ByteArrayInputStream(certificate.getBytes());
        X509Certificate cert = (X509Certificate)fac.generateCertificate(in);
        in.close();
        X509Certificate[] certChain = new X509Certificate[] {cert};

        String alias = cert.getSubjectX500Principal().getName();

        byte[] key = convertToDER(privateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        PrivateKey pk = kf.generatePrivate(keySpec);

        keystore.setCertificateEntry(alias , cert);
        keystore.setKeyEntry("importKey", pk, ksPass.toCharArray(), certChain);

        File tempFile = File.createTempFile("ks",".tmp");
        FileOutputStream fos = new FileOutputStream(tempFile);
        keystore.store(fos, ksPass.toCharArray());

        fos.close();
        return tempFile;
    }

    /**
     * This method converts a PEM encoded certificate/crl/... to DER encoded.
     *
     * @param pemContent
     *            the String which contains the PEM encoded object
     * @return the binaries of the DER encoded object
     */
    private byte[] convertToDER(String pemContent) {
        try (Reader reader = new StringReader(pemContent); PemReader pemReader = new PemReader(reader)) {
            PemObject readPemObject = pemReader.readPemObject();
            return readPemObject.getContent();
        } catch (IOException e) {
            throw new DSSException("Unable to convert PEM to DER", e);
        }
    }

}
