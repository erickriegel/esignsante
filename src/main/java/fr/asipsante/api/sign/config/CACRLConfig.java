package fr.asipsante.api.sign.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import fr.asipsante.api.sign.service.ICACRLService;
import fr.asipsante.api.sign.service.ICertificateValidationService;
import fr.asipsante.api.sign.service.IProofGenerationService;
import fr.asipsante.api.sign.service.ISignatureService;
import fr.asipsante.api.sign.service.ISignatureValidationService;
import fr.asipsante.api.sign.service.impl.CACRLServiceImpl;
import fr.asipsante.api.sign.service.impl.CertificateValidationServiceImpl;
import fr.asipsante.api.sign.service.impl.ProofGenerationServiceImpl;
import fr.asipsante.api.sign.service.impl.SignatureServiceImpl;
import fr.asipsante.api.sign.service.impl.SignatureValidationServiceImpl;

/**
 * The Class CACRLConfig.
 */
@Configuration()
public class CACRLConfig {

    /** The log. */
    Logger log = LoggerFactory.getLogger(CACRLConfig.class);

    /** The ca path. */
    @Value("${config.ca.path}")
    private String caPath;

    /** The crl path. */
    @Value("${config.crl.path}")
    private String crlPath;

    /**
     * Signature service.
     *
     * @return the i signature service
     */
    @Bean
    @Lazy
    public ISignatureService signatureService() {
        return new SignatureServiceImpl();
    }

    /**
     * Signature validation service.
     *
     * @return the i signature validation service
     */
    @Bean
    @Lazy
    public ISignatureValidationService signatureValidationService() {
        return new SignatureValidationServiceImpl();
    }

    /**
     * Certificate validation service.
     *
     * @return the i certificate validation service
     */
    @Bean
    @Lazy
    public ICertificateValidationService certificateValidationService() {
        return new CertificateValidationServiceImpl();
    }

    /**
     * Proof generation service.
     *
     * @return the i proof generation service
     */
    @Bean
    @Lazy
    public IProofGenerationService proofGenerationService() {
        return new ProofGenerationServiceImpl();
    }

    /**
     * Service ca crl.
     *
     * @return the ICACRL service
     */
    @Bean
    @Lazy
    public ICACRLService serviceCaCrl() {

        ICACRLService serviceCaCrl = new CACRLServiceImpl();
        log.info("Chargement du bundle des AC, chemin : {}", caPath);
        serviceCaCrl.loadCA(new File(caPath));
        log.info("Chargement du bundle des CRL, chemin : {}", crlPath);
        serviceCaCrl.loadCRL(new File(crlPath));
        return serviceCaCrl;
    }
}
