package fr.asipsante.api.sign.ws.bean;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.asipsante.api.sign.bean.parameters.CertificateValidationParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureValidationParameters;
import fr.asipsante.api.sign.utils.AsipSignServerException;

/**
 * The Class ConfigurationsLoader.
 */
public class ConfigurationsLoader {

    /** The log. */
    Logger log = LoggerFactory.getLogger(ConfigurationsLoader.class);

    /** The ws conf path. */
    private String wsConfPath;

    /**
     * Instantiates a new configurations loader.
     *
     * @param wsConfPath
     *            the ws conf path
     */
    public ConfigurationsLoader(String wsConfPath) {
        super();
        this.wsConfPath = wsConfPath;
    }

    /**
     * Load.
     *
     * @param paramsList
     *            the params list
     */
    public void load(Parameters paramsList) {

        Properties prop = new Properties();
        InputStream targetStream;
        try {
            targetStream = new FileInputStream(wsConfPath);
            prop.load(targetStream);
        } catch (IOException e) {
            log.error(
                    "Chemin du fichier de configuration incorrect, ou erreur sur le fichier renseigné: {}",
                    wsConfPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        String[] signIds = prop.getProperty("config.sign.ids").trim()
                .split(",");
        String[] verifSignIds = prop.getProperty("config.verifsign.ids").trim()
                .split(",");
        String[] verifCertIds = prop.getProperty("config.verifcert.ids").trim()
                .split(",");
        String[] proofIds = prop.getProperty("config.proof.ids").trim()
                .split(",");

        for (String signId : signIds) {
            String signPathValue = prop
                    .getProperty("config.sign." + signId + ".path");
            SignatureParameters params;
            try {
                params = loadConfSign(signPathValue);
                paramsList.getSignatureConfigurations().put(signId, params);
            } catch (AsipSignServerException | IllegalArgumentException e) {
                log.warn(
                        "Fichier de configuration {} invalide, configuration ignorée",
                        signPathValue);
                log.warn(ExceptionUtils.getFullStackTrace(e));
            }

        }

        for (String verifSignId : verifSignIds) {
            String verifPathvalue = prop
                    .getProperty("config.verifsign." + verifSignId + ".path");
            try {
                SignatureValidationParameters params = loadConfSignValidation(
                        verifPathvalue);
                paramsList.getSignatureValidationConfigurations()
                        .put(verifSignId, params);
            } catch (AsipSignServerException e) {
                log.warn(
                        "Fichier de configuration {} invalide, configuration ignorée",
                        verifPathvalue);
                log.warn(ExceptionUtils.getFullStackTrace(e));
            }
        }

        for (String verifCertId : verifCertIds) {
            String certPathValue = prop
                    .getProperty("config.verifcert." + verifCertId + ".path");
            try {
                CertificateValidationParameters params = loadConfCertValidation(
                        certPathValue);
                paramsList.getCertificateValidationConfigurations()
                        .put(verifCertId, params);
            } catch (AsipSignServerException e) {
                log.warn(
                        "Fichier de configuration {} invalide, configuration ignorée",
                        certPathValue);
                log.warn(ExceptionUtils.getFullStackTrace(e));
            }
        }
        // Attention, les conf de la preuve sont des conf de signature de la
        // preuve,
        // les autres paramètres sont récupérés depuis la requête.
        for (String proofId : proofIds) {
            String proofPathValue = prop
                    .getProperty("config.proof." + proofId + ".path");
            try {
                SignatureParameters params = loadConfSign(proofPathValue);
                paramsList.getProofSignatureConfigurations().put(proofId,
                        params);
            } catch (AsipSignServerException | IllegalArgumentException e) {
                log.warn(
                        "Fichier de configuration {} invalide, configuration ignorée",
                        proofPathValue);
                log.warn(ExceptionUtils.getFullStackTrace(e));
            }
        }

    }

    /**
     * Load conf sign.
     *
     * @param confPath
     *            the conf path
     * @return the signature parameters
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    private SignatureParameters loadConfSign(String confPath)
            throws AsipSignServerException {

        Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(confPath);
            prop.load(fis);
            fis.close();
        } catch (IOException e) {
            log.error(
                    "Chemin du fichier de configuration incorrect, ou erreur sur le fichier renseigné: {}",
                    confPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        String uniquepassword = System
                .getProperty("asipsign.ws.unique.password");
        if (uniquepassword != null) {
            prop.setProperty("pkcs12Password", uniquepassword);
        }

        return new SignatureParameters(prop);
    }

    /**
     * Load conf sign validation.
     *
     * @param confPath
     *            the conf path
     * @return the signature validation parameters
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    private SignatureValidationParameters loadConfSignValidation(
            String confPath) throws AsipSignServerException {

        Properties prop = new Properties();
        FileInputStream fis = null;
        SignatureValidationParameters params = new SignatureValidationParameters();
        try {
            fis = new FileInputStream(confPath);
            prop.load(fis);
            params.setDescription(prop.getProperty("description", ""));
            String rules = prop.getProperty("rules");
            if (rules != null) {
                params.loadRules(Arrays.asList(rules.trim().split(",")));
            }
            String metadata = prop.getProperty("metadata");
            if (metadata != null) {
                params.loadMetadata(Arrays.asList(metadata.trim().split(",")));
            }
            fis.close();
        } catch (IOException e) {
            log.error(
                    "Chemin du fichier de configuration incorrect, ou erreur sur le fichier renseigné: {}",
                    confPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return params;
    }

    /**
     * Load conf cert validation.
     *
     * @param confPath
     *            the conf path
     * @return the certificate validation parameters
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    private CertificateValidationParameters loadConfCertValidation(
            String confPath) throws AsipSignServerException {

        Properties prop = new Properties();
        FileInputStream fis = null;
        CertificateValidationParameters params = new CertificateValidationParameters();
        try {
            fis = new FileInputStream(confPath);
            prop.load(fis);
            params.setDescription(prop.getProperty("description", ""));
            String rules = prop.getProperty("rules");
            if (rules != null) {
                params.loadRules(Arrays.asList(rules.trim().split(",")));
            }
            String metadata = prop.getProperty("metadata");
            if (metadata != null) {
                params.loadMetadata(Arrays.asList(metadata.trim().split(",")));
            }
            fis.close();
        } catch (IOException e) {
            log.error(
                    "Chemin du fichier de configuration incorrect, ou erreur sur le fichier renseigné: {}",
                    confPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return params;
    }

}
