/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean;

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

    private static final int ASCII_COUNT = 32;

    /**
     * Load conf sign.
     *
     * @param signConf the signature configuration
     * @return the signature parameters
     */
    public static SignatureParameters loadSignConf(final SignatureConf signConf) {
        final SignatureParameters params = new SignatureParameters();
        params.setDescription(signConf.getDescription());
        params.setCanonAlgo(signConf.getCanonicalisationAlgorithm());
        params.setDigestAlgo(DigestAlgorithm.valueOf(signConf.getDigestAlgorithm()));
        params.setSignPackaging(RestrictedSignaturePackaging.valueOf(signConf.getSignaturePackaging()));
        params.setObjectId(signConf.getObjectId());
        params.setSignId(signConf.getSignId());
        params.setSignValueId(signConf.getSignValueId());

        final String ksPass = RandomStringUtils.randomAscii(ASCII_COUNT);

        try {
            final File ksFile = generateKsFile(signConf.getCertificate(), signConf.getPrivateKey(), ksPass);
            params.setKsFile(ksFile);
            params.setPassword(ksPass);
        } catch (final GeneralSecurityException | IOException e) {
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
    public static SignatureParameters loadProofSignConf(final ProofConf proofConf) {
        final SignatureParameters params = new SignatureParameters();
        params.setDescription(proofConf.getDescription());
        params.setCanonAlgo(proofConf.getCanonicalisationAlgorithm());
        params.setDigestAlgo(DigestAlgorithm.valueOf(proofConf.getDigestAlgorithm()));
        params.setSignPackaging(RestrictedSignaturePackaging.valueOf(proofConf.getSignaturePackaging()));

        final String ksPass = RandomStringUtils.randomAscii(ASCII_COUNT);

        try {
            final File ksFile = generateKsFile(proofConf.getCertificate(), proofConf.getPrivateKey(), ksPass);
            params.setKsFile(ksFile);
            params.setPassword(ksPass);
        } catch (final GeneralSecurityException | IOException e) {
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
    public static SignatureValidationParameters loadSignVerifConf(final SignVerifConf signVerifConf) {
        final SignatureValidationParameters signValidationParams = new SignatureValidationParameters();
        signValidationParams.setDescription(signVerifConf.getDescription());
        signValidationParams.loadRules(Arrays.asList(signVerifConf.getRules().replaceAll("\\s+","").split(",")));
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
    public static CertificateValidationParameters loadCertVerifConf(final CertVerifConf certVerifConf) {
        final CertificateValidationParameters certValidationParams = new CertificateValidationParameters();
        certValidationParams.setDescription(certVerifConf.getDescription());
        certValidationParams.loadRules(Arrays.asList(certVerifConf.getRules().replaceAll("\\s","").split(",")));
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
    private static File generateKsFile(final String certificate, final String privateKey, final String ksPass) throws GeneralSecurityException, IOException {

        final KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(null, ksPass.toCharArray());

        final CertificateFactory fac = CertificateFactory.getInstance("X509");
        final ByteArrayInputStream in = new ByteArrayInputStream(certificate.getBytes());
        final X509Certificate cert = (X509Certificate)fac.generateCertificate(in);
        in.close();
        final X509Certificate[] certChain = new X509Certificate[] {cert};

        final String alias = cert.getSubjectX500Principal().getName();

        final byte[] key = convertToDER(privateKey);
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        final PrivateKey pk = kf.generatePrivate(keySpec);

        keystore.setCertificateEntry(alias , cert);
        keystore.setKeyEntry("importKey", pk, ksPass.toCharArray(), certChain);

        final File tempFile = File.createTempFile("keyStore",".p12");
        final FileOutputStream fos = new FileOutputStream(tempFile);
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
    private static byte[] convertToDER(final String pemContent) {
        try (final Reader reader = new StringReader(pemContent); final PemReader pemReader = new PemReader(reader)) {
            final PemObject readPemObject = pemReader.readPemObject();
            return readPemObject.getContent();
        } catch (final IOException e) {
            throw new RuntimeException("Unable to convert PEM to DER", e);
        }
    }

}
