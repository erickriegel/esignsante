package fr.asipsante.api.sign.ws.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.asipsante.api.sign.bean.parameters.CertificateValidationParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureValidationParameters;
import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.ws.model.Conf;
import fr.asipsante.api.sign.ws.model.ConfProof;
import fr.asipsante.api.sign.ws.model.ConfSign;
import fr.asipsante.api.sign.ws.model.ConfVerifCert;
import fr.asipsante.api.sign.ws.model.ConfVerifSign;

/**
 * The Class Parameters.
 */
public class Parameters {

    /** Liste des configurations de signature identifiées par leur id. */
    private Map<String, SignatureParameters> signatureConfigurations = new HashMap<String, SignatureParameters>();

    /** The signature validation configurations. */
    private Map<String, SignatureValidationParameters> signatureValidationConfigurations = new HashMap<String, SignatureValidationParameters>();;

    /**
     * Liste des configurations de signature de la preuve identifiées par leur
     * id.
     */
    private Map<String, SignatureParameters> proofSignatureConfigurations = new HashMap<String, SignatureParameters>();;

    /** The certificate validation configurations. */
    private Map<String, CertificateValidationParameters> certificateValidationConfigurations = new HashMap<String, CertificateValidationParameters>();;

    /**
     * Gets the configs.
     *
     * @return La liste des configurations
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    public Conf getConfigs() throws AsipSignServerException {

        Conf configs = new Conf();
        List<ConfSign> confsSign = ConfigurationsMapper
                .mapSignConfig(signatureConfigurations);
        configs.setSignature(confsSign);
        List<ConfVerifCert> confsVerifCert = ConfigurationsMapper
                .mapVerifCertConfig(certificateValidationConfigurations);
        configs.setCertificatVerification(confsVerifCert);
        List<ConfProof> confsProof = ConfigurationsMapper
                .mapProofConfig(proofSignatureConfigurations);
        configs.setProof(confsProof);
        List<ConfVerifSign> confsVerifSign = ConfigurationsMapper
                .mapVerifSignConfig(signatureValidationConfigurations);
        configs.setSignatureVerification(confsVerifSign);
        return configs;
    }

    /**
     * Gets the signature configurations.
     *
     * @return the signature configurations
     */
    public Map<String, SignatureParameters> getSignatureConfigurations() {
        return signatureConfigurations;
    }

    /**
     * Gets the signature validation configurations.
     *
     * @return the signature validation configurations
     */
    public Map<String, SignatureValidationParameters> getSignatureValidationConfigurations() {
        return signatureValidationConfigurations;
    }

    /**
     * Sets the signature validation configurations.
     *
     * @param signatureValidationConfigurations
     *            the signature validation configurations
     */
    public void setSignatureValidationConfigurations(
            Map<String, SignatureValidationParameters> signatureValidationConfigurations) {
        this.signatureValidationConfigurations = signatureValidationConfigurations;
    }

    /**
     * Gets the proof signature configurations.
     *
     * @return the proof signature configurations
     */
    public Map<String, SignatureParameters> getProofSignatureConfigurations() {
        return proofSignatureConfigurations;
    }

    /**
     * Sets the proof signature configurations.
     *
     * @param proofSignatureConfigurations
     *            the proof signature configurations
     */
    public void setProofSignatureConfigurations(
            Map<String, SignatureParameters> proofSignatureConfigurations) {
        this.proofSignatureConfigurations = proofSignatureConfigurations;
    }

    /**
     * Gets the certificate validation configurations.
     *
     * @return the certificate validation configurations
     */
    public Map<String, CertificateValidationParameters> getCertificateValidationConfigurations() {
        return certificateValidationConfigurations;
    }

    /**
     * Sets the certificate validation configurations.
     *
     * @param certificateValidationConfigurations
     *            the certificate validation configurations
     */
    public void setCertificateValidationConfigurations(
            Map<String, CertificateValidationParameters> certificateValidationConfigurations) {
        this.certificateValidationConfigurations = certificateValidationConfigurations;
    }

    /**
     * Sets the signature configurations.
     *
     * @param signatureConfigurations
     *            the signature configurations
     */
    public void setSignatureConfigurations(
            Map<String, SignatureParameters> signatureConfigurations) {
        this.signatureConfigurations = signatureConfigurations;
    }

}
