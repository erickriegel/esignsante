package fr.asipsante.api.sign.ws.bean.object;

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

    public ProofConf(String idProofConf, String description, String certificate, String privateKey, String canonicalisationAlgorithm, String digestAlgorithm, String signaturePackaging) {
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
}
