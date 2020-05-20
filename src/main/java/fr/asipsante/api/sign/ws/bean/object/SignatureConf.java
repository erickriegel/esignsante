/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean.object;

import fr.asipsante.api.sign.ws.util.Secrets;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * The type Signature conf.
 */
public class SignatureConf {

    /**
     * idSignConf.
     */
    private String idSignConf;

    /**
     * secret.
     */
    private String secret;

    /**
     * idProofConf.
     */
    private String idProofConf;

    /**
     * description.
     */
    private String description;

    /**
     * certificate.
     */
    private String certificate;

    /**
     * privateKey.
     */
    private String privateKey;

    /**
     * canonicalisationAlgorithm.
     */
    private String canonicalisationAlgorithm;

    /**
     * digestAlgorithm.
     */
    private String digestAlgorithm;

    /**
     * signaturePackaging.
     */
    private String signaturePackaging;

    /**
     * signId.
     */
    private String signId;

    /**
     * signValueId.
     */
    private String signValueId;

    /**
     * objectId.
     */
    private String objectId;

    /**
     * Instantiates a new Signature conf.
     */
    public SignatureConf() {
    }

    /**
     * Instantiates a new Signature conf.
     *
     * @param idSignConf                the id sign conf
     * @param secret                    the secret
     * @param idProofConf               the id proof conf
     * @param description               the description
     * @param certificate               the certificate
     * @param privateKey                the private key
     * @param canonicalisationAlgorithm the canonicalisation algorithm
     * @param digestAlgorithm           the digest algorithm
     * @param signaturePackaging        the signature packaging
     * @param signId                    the sign id
     * @param signValueId               the sign value id
     * @param objectId                  the object id
     */
    public SignatureConf(
            final String idSignConf, final String secret, final String idProofConf, final String description,
            final String certificate, final String privateKey, final String canonicalisationAlgorithm,
            final String digestAlgorithm, final String signaturePackaging, final String signId,
            final String signValueId, final String objectId) {
        this.idSignConf = idSignConf;
        this.secret = secret;
        this.idProofConf = idProofConf;
        this.description = description;
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.canonicalisationAlgorithm = canonicalisationAlgorithm;
        this.digestAlgorithm = digestAlgorithm;
        this.signaturePackaging = signaturePackaging;
        this.signId = signId;
        this.signValueId = signValueId;
        this.objectId = objectId;
    }

    /**
     * No secret match boolean.
     *
     * @param secret the secret
     * @return the boolean
     */
    public boolean noSecretMatch(final String secret) {
        return Arrays.stream(getSecret().trim().split(" ")).noneMatch(hash -> Secrets.match(secret, hash));
    }

    /**
     * Gets id sign conf.
     *
     * @return the id sign conf
     */
    public String getIdSignConf() {
        return idSignConf;
    }

    /**
     * Sets id sign conf.
     *
     * @param idSignConf the id sign conf
     */
    public void setIdSignConf(final String idSignConf) {
        this.idSignConf = idSignConf;
    }

    /**
     * Gets secret.
     *
     * @return the secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets secret.
     *
     * @param secret the secret
     */
    public void setSecret(final String secret) {
        this.secret = secret;
    }

    /**
     * Gets id proof conf.
     *
     * @return the id proof conf
     */
    public String getIdProofConf() {
        return idProofConf;
    }

    /**
     * Sets id proof conf.
     *
     * @param idProofConf the id proof conf
     */
    public void setIdProofConf(final String idProofConf) {
        this.idProofConf = idProofConf;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Gets certificate.
     *
     * @return the certificate
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * Sets certificate.
     *
     * @param certificate the certificate
     */
    public void setCertificate(final String certificate) {
        this.certificate = certificate;
    }

    /**
     * Gets private key.
     *
     * @return the private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets private key.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(final String privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Gets canonicalisation algorithm.
     *
     * @return the canonicalisation algorithm
     */
    public String getCanonicalisationAlgorithm() {
        return canonicalisationAlgorithm;
    }

    /**
     * Sets canonicalisation algorithm.
     *
     * @param canonicalisationAlgorithm the canonicalisation algorithm
     */
    public void setCanonicalisationAlgorithm(final String canonicalisationAlgorithm) {
        this.canonicalisationAlgorithm = canonicalisationAlgorithm;
    }

    /**
     * Gets digest algorithm.
     *
     * @return the digest algorithm
     */
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    /**
     * Sets digest algorithm.
     *
     * @param digestAlgorithm the digest algorithm
     */
    public void setDigestAlgorithm(final String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    /**
     * Gets signature packaging.
     *
     * @return the signature packaging
     */
    public String getSignaturePackaging() {
        return signaturePackaging;
    }

    /**
     * Sets signature packaging.
     *
     * @param signaturePackaging the signature packaging
     */
    public void setSignaturePackaging(final String signaturePackaging) {
        this.signaturePackaging = signaturePackaging;
    }

    /**
     * Gets sign id.
     *
     * @return the sign id
     */
    public String getSignId() {
        return signId;
    }

    /**
     * Sets sign id.
     *
     * @param signId the sign id
     */
    public void setSignId(final String signId) {
        this.signId = signId;
    }

    /**
     * Gets sign value id.
     *
     * @return the sign value id
     */
    public String getSignValueId() {
        return signValueId;
    }

    /**
     * Sets sign value id.
     *
     * @param signValueId the sign value id
     */
    public void setSignValueId(final String signValueId) {
        this.signValueId = signValueId;
    }

    /**
     * Gets object id.
     *
     * @return the object id
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Sets object id.
     *
     * @param objectId the object id
     */
    public void setObjectId(final String objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return "SignatureConf{" +
                "idSignConf='" + idSignConf + '\'' +
                ", secret='" + secret + '\'' +
                ", idProofConf='" + idProofConf + '\'' +
                ", description='" + description + '\'' +
                ", certificate='" + certificate + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", canonicalisationAlgorithm='" + canonicalisationAlgorithm + '\'' +
                ", digestAlgorithm='" + digestAlgorithm + '\'' +
                ", signaturePackaging='" + signaturePackaging + '\'' +
                ", signId='" + signId + '\'' +
                ", signValueId='" + signValueId + '\'' +
                ", objectId='" + objectId + '\'' +
                '}';
    }

    /**
     * Check valid boolean.
     *
     * @return the boolean
     * @throws IllegalAccessException the illegal access exception
     */
    public boolean checkValid() throws IllegalAccessException {
        for (final Field f : getClass().getDeclaredFields())
            if (f.get(this) == null)
                return false;
        return true;
    }
}
