package fr.asipsante.api.sign.ws.bean.config.impl;

import fr.asipsante.api.sign.config.observer.CaCrlObserver;
import fr.asipsante.api.sign.ws.bean.ConfigurationMapper;
import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;
import fr.asipsante.api.sign.ws.bean.object.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GlobalConfJson implements IGlobalConf {

    private List<SignatureConf> signature;

    private List<ProofConf> proof;

    private List<SignVerifConf> signatureVerification;

    private List<CertVerifConf> certificateVerification;

    private List<CaConf> ca;

    private List<CaCrlObserver> caCrlObservers = new ArrayList<>();

    public GlobalConfJson() {
    }

    public GlobalConfJson(List<SignatureConf> signature, List<ProofConf> proof, List<SignVerifConf> signatureVerification, List<CertVerifConf> certificateVerification, List<CaConf> ca) {
        this.signature = signature;
        this.proof = proof;
        this.signatureVerification = signatureVerification;
        this.certificateVerification = certificateVerification;
        this.ca = ca;
    }

    @Override
    public void addObserver(CaCrlObserver caCrlObserver) {
        this.caCrlObservers.add(caCrlObserver);
    }

    @Override
    public void removeObserver(CaCrlObserver caCrlObserver) {
        this.caCrlObservers.remove(caCrlObserver);
    }

    public ConfigurationMapper mapConfigs() {
        return new ConfigurationMapper(signature, proof, signatureVerification, certificateVerification);
    }

    public Optional<SignatureConf> getSignatureById(String id) {
        return getSignature().stream().filter(signConf -> id.equals(signConf.getIdSignConf())).findAny();
    }

    public List<SignatureConf> getSignature() {
        return signature;
    }

    public void setSignature(List<SignatureConf> signature) {
        this.signature = signature;
    }

    public Optional<ProofConf> getProofById(String id) {
        return getProof().stream().filter(proofConf -> id.equals(proofConf.getIdProofConf())).findAny();
    }

    public List<ProofConf> getProof() {
        return proof;
    }

    public void setProof(List<ProofConf> proof) {
        this.proof = proof;
    }

    public Optional<SignVerifConf> getSignatureVerificationById(String id) {
        return getSignatureVerification().stream().filter(signVerif -> id.equals(signVerif.getIdVerifSign())).findAny();
    }

    public List<SignVerifConf> getSignatureVerification() {
        return signatureVerification;
    }

    public void setSignatureVerification(List<SignVerifConf> signatureVerification) {
        this.signatureVerification = signatureVerification;
    }

    public Optional<CertVerifConf> getCertificateVerificationById(String id) {
        return getCertificateVerification().stream().filter(confVerif -> id.equals(confVerif.getIdVerifCert())).findAny();
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
        for (CaCrlObserver caCrlObserver : caCrlObservers) {
            caCrlObserver.update();
        }
    }

    @Override
    public String toString() {
        return "GlobalConfJson{" +
                "signature=" + signature.toString() +
                ", proof=" + proof.toString() +
                ", signatureVerification=" + signatureVerification.toString() +
                ", certificateVerification=" + certificateVerification.toString() +
                ", ca=" + ca.toString() +
                '}';
    }

}
