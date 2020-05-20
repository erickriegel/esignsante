package fr.asipsante.api.sign.ws.bean.object;

import fr.asipsante.api.sign.ws.util.Secrets;

import java.lang.reflect.Field;
import java.util.Arrays;

public class SignatureConf {

    private String idSignConf;

    private String secret;

    private String idProofConf;

    private String description;

    private String certificate;

    private String privateKey;

    private String canonicalisationAlgorithm;

    private String digestAlgorithm;

    private String signaturePackaging;

    private String signId;

    private String signValueId;

    private String objectId;

    public SignatureConf() {
    }

    public SignatureConf(String idSignConf, String secret, String idProofConf, String description, String certificate,
                         String privateKey, String canonicalisationAlgorithm, String digestAlgorithm,
                         String signaturePackaging, String signId, String signValueId, String objectId) {
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

    public boolean secretMatch(String secret) {
        return Arrays.stream(getSecret().trim().split(" ")).anyMatch(hash -> Secrets.match(secret, hash));
    }

    public String getIdSignConf() {
        return idSignConf;
    }

    public void setIdSignConf(String idSignConf) {
        this.idSignConf = idSignConf;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIdProofConf() {
        return idProofConf;
    }

    public void setIdProofConf(String idProofConf) {
        this.idProofConf = idProofConf;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCanonicalisationAlgorithm() {
        return canonicalisationAlgorithm;
    }

    public void setCanonicalisationAlgorithm(String canonicalisationAlgorithm) {
        this.canonicalisationAlgorithm = canonicalisationAlgorithm;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getSignaturePackaging() {
        return signaturePackaging;
    }

    public void setSignaturePackaging(String signaturePackaging) {
        this.signaturePackaging = signaturePackaging;
    }

    public String getSignId() {
        return signId;
    }

    public void setSignId(String signId) {
        this.signId = signId;
    }

    public String getSignValueId() {
        return signValueId;
    }

    public void setSignValueId(String signValueId) {
        this.signValueId = signValueId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
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

    public boolean checkValid() throws IllegalAccessException {
        for (Field f : getClass().getDeclaredFields())
            if (f.get(this) == null)
                return false;
        return true;
    }
}
