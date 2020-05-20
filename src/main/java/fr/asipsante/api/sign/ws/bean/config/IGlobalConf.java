/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean.config;

import fr.asipsante.api.sign.config.observer.CaCrlObserver;
import fr.asipsante.api.sign.ws.bean.ConfigurationMapper;
import fr.asipsante.api.sign.ws.bean.object.*;

import java.util.List;
import java.util.Optional;

public interface IGlobalConf {

    void addObserver(CaCrlObserver caCrlService);

    void removeObserver(CaCrlObserver caCrlService);

    ConfigurationMapper mapConfigs();

    Optional<SignatureConf> getSignatureById(String id);

    List<SignatureConf> getSignature();

    void setSignature(List<SignatureConf> signature);

    Optional<ProofConf> getProofById(String id);

    List<ProofConf> getProof();

    void setProof(List<ProofConf> proof);

    Optional<SignVerifConf> getSignatureVerificationById(String id);

    List<SignVerifConf> getSignatureVerification();

    void setSignatureVerification(List<SignVerifConf> signatureVerification);

    Optional<CertVerifConf> getCertificateVerificationById(String id);

    List<CertVerifConf> getCertificateVerification();

    void setCertificateVerification(List<CertVerifConf> certificateVerification);

    List<CaConf> getCa();

    void setCa(List<CaConf> ca);

}
