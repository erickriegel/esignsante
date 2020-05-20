package fr.asipsante.api.sign.ws.bean.object;

import java.util.List;

public class GlobalConf {

    private List<SignatureConf> signature;

    private List<ProofConf> proof;

    private List<SignVerifConf> signatureVerification;

    private List<CertVerifConf> certificateVerification;

    private List<CaConf> ca;

    public GlobalConf() {
    }

    public GlobalConf(List<SignatureConf> signature, List<ProofConf> proof, List<SignVerifConf> signatureVerification, List<CertVerifConf> certificateVerification, List<CaConf> ca) {
        this.signature = signature;
        this.proof = proof;
        this.signatureVerification = signatureVerification;
        this.certificateVerification = certificateVerification;
        this.ca = ca;
    }

    public List<SignatureConf> getSignature() {
        return signature;
    }

    public void setSignature(List<SignatureConf> signature) {
        this.signature = signature;
    }

    public List<ProofConf> getProof() {
        return proof;
    }

    public void setProof(List<ProofConf> proof) {
        this.proof = proof;
    }

    public List<SignVerifConf> getSignatureVerification() {
        return signatureVerification;
    }

    public void setSignatureVerification(List<SignVerifConf> signatureVerification) {
        this.signatureVerification = signatureVerification;
    }

    public List<CertVerifConf> getCertificateVerification() {
        return certificateVerification;
    }

    public void setCertificateVerification(List<CertVerifConf> certificateVerification) {
        this.certificateVerification = certificateVerification;
    }

    public List<CaConf> getCa() {
        return ca;
    }

    public void setCa(List<CaConf> ca) {
        this.ca = ca;
    }

    @Override
    public String toString() {
        return "GlobalConf{" +
                "signature=" + signature.toString() +
                ", proof=" + proof.toString() +
                ", signatureVerification=" + signatureVerification.toString() +
                ", certificateVerification=" + certificateVerification.toString() +
                ", ca=" + ca.toString() +
                '}';
    }
}
