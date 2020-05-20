/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean.object;

import java.lang.reflect.Field;

public class ProofConf {

    private String idProofConf;

    private String description;

    private String certificate;

    private String privateKey;

    private String canonicalisationAlgorithm;

    private String digestAlgorithm;

    private String signaturePackaging;

    public ProofConf() {
    }

    public ProofConf(final String idProofConf, final String description, final String certificate, final String privateKey, final String canonicalisationAlgorithm, final String digestAlgorithm, final String signaturePackaging) {
        this.idProofConf = idProofConf;
        this.description = description;
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.canonicalisationAlgorithm = canonicalisationAlgorithm;
        this.digestAlgorithm = digestAlgorithm;
        this.signaturePackaging = signaturePackaging;
    }

    public String getIdProofConf() {
        return idProofConf;
    }

    public void setIdProofConf(final String idProofConf) {
        this.idProofConf = idProofConf;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(final String certificate) {
        this.certificate = certificate;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(final String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCanonicalisationAlgorithm() {
        return canonicalisationAlgorithm;
    }

    public void setCanonicalisationAlgorithm(final String canonicalisationAlgorithm) {
        this.canonicalisationAlgorithm = canonicalisationAlgorithm;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(final String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getSignaturePackaging() {
        return signaturePackaging;
    }

    public void setSignaturePackaging(final String signaturePackaging) {
        this.signaturePackaging = signaturePackaging;
    }

    @Override
    public String toString() {
        return "ProofConf{" +
                "idProofConf='" + idProofConf + '\'' +
                ", description='" + description + '\'' +
                ", certificate='" + certificate + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", canonicalisationAlgorithm='" + canonicalisationAlgorithm + '\'' +
                ", digestAlgorithm='" + digestAlgorithm + '\'' +
                ", signaturePackaging='" + signaturePackaging + '\'' +
                '}';
    }

    public boolean checkValid() throws IllegalAccessException {
        for (final Field f : getClass().getDeclaredFields())
            if (f.get(this) == null)
                return false;
        return true;
    }
}
