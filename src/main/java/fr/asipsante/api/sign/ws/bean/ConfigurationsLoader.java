/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
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
import fr.asipsante.api.sign.ws.util.WsVars;

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
     * @param wsConfPath the ws conf path
     */
    public ConfigurationsLoader(String wsConfPath) {
        super();
        this.wsConfPath = wsConfPath;
    }

    /**
     * Load.
     *
     * @param paramsList the params list
     */
    public void load(Parameters paramsList) {

        final Properties prop = new Properties();
        final InputStream targetStream;
        try {
            targetStream = new FileInputStream(wsConfPath);
            prop.load(targetStream);
        } catch (final IOException e) {
            log.error(WsVars.CONF_PATH_ERROR.getVar(), wsConfPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        final String[] signIds = prop.getProperty("config.sign.ids").trim().split(",");
        final String[] verifSignIds = prop.getProperty("config.verifsign.ids").trim().split(",");
        final String[] verifCertIds = prop.getProperty("config.verifcert.ids").trim().split(",");
        final String[] proofIds = prop.getProperty("config.proof.ids").trim().split(",");

        for (final String signId : signIds) {
            final String signPathValue = prop.getProperty("config.sign." + signId + WsVars.PROPS_PATH.getVar());
            final SignatureParameters params;
            try {
                params = loadConfSign(signPathValue);
                paramsList.getSignatureConfigurations().put(signId, params);
            } catch (AsipSignServerException | IllegalArgumentException e) {
                log.warn(WsVars.CONF_ERROR.getVar(), signPathValue);
                log.warn(ExceptionUtils.getFullStackTrace(e));
            }

        }

        for (final String verifSignId : verifSignIds) {
            final String verifPathvalue = prop
                    .getProperty("config.verifsign." + verifSignId + WsVars.PROPS_PATH.getVar());
            final SignatureValidationParameters params = loadConfSignValidation(verifPathvalue);
            paramsList.getSignatureValidationConfigurations().put(verifSignId, params);
        }

        for (final String verifCertId : verifCertIds) {
            final String certPathValue = prop
                    .getProperty("config.verifcert." + verifCertId + WsVars.PROPS_PATH.getVar());
            final CertificateValidationParameters params = loadConfCertValidation(certPathValue);
            paramsList.getCertificateValidationConfigurations().put(verifCertId, params);
        }
        // Attention, les conf de la preuve sont des conf de signature de la
        // preuve,
        // les autres paramètres sont récupérés depuis la requête.
        for (final String proofId : proofIds) {
            final String proofPathValue = prop.getProperty("config.proof." + proofId + WsVars.PROPS_PATH.getVar());
            try {
                final SignatureParameters params = loadConfSign(proofPathValue);
                paramsList.getProofSignatureConfigurations().put(proofId, params);
            } catch (AsipSignServerException | IllegalArgumentException e) {
                log.warn(WsVars.CONF_ERROR.getVar(), proofPathValue);
                log.warn(ExceptionUtils.getFullStackTrace(e));
            }
        }

    }

    /**
     * Load conf sign.
     *
     * @param confPath the conf path
     * @return the signature parameters
     * @throws AsipSignServerException the asip sign server exception
     */
    private SignatureParameters loadConfSign(String confPath) throws AsipSignServerException {

        final Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(confPath);
            prop.load(fis);
            fis.close();
        } catch (final IOException e) {
            log.error(WsVars.CONF_PATH_ERROR.getVar(), confPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        final String uniquepassword = System.getProperty("asipsign.ws.unique.password");
        if (uniquepassword != null) {
            prop.setProperty("pkcs12Password", uniquepassword);
        }

        return new SignatureParameters(prop);
    }

    /**
     * Load conf sign validation.
     *
     * @param confPath the conf path
     * @return the signature validation parameters
     */
    private SignatureValidationParameters loadConfSignValidation(String confPath) {

        final Properties prop = new Properties();
        FileInputStream fis = null;
        final SignatureValidationParameters signValidationParams = new SignatureValidationParameters();
        try {
            fis = new FileInputStream(confPath);
            prop.load(fis);
            signValidationParams.setDescription(prop.getProperty("description", ""));
            final String rules = prop.getProperty("rules");
            if (rules != null) {
                signValidationParams.loadRules(Arrays.asList(rules.trim().split(",")));
            }
            final String metadata = prop.getProperty("metadata");
            if (metadata != null) {
                signValidationParams.loadMetadata(Arrays.asList(metadata.trim().split(",")));
            }
            fis.close();
        } catch (final IOException e) {
            log.error(WsVars.CONF_PATH_ERROR.getVar(), confPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return signValidationParams;
    }

    /**
     * Load conf cert validation.
     *
     * @param confPath the conf path
     * @return the certificate validation parameters
     */
    private CertificateValidationParameters loadConfCertValidation(String confPath) {

        final Properties prop = new Properties();
        FileInputStream fis = null;
        final CertificateValidationParameters certValidationParams = new CertificateValidationParameters();
        try {
            fis = new FileInputStream(confPath);
            prop.load(fis);
            certValidationParams.setDescription(prop.getProperty("description", ""));
            final String rules = prop.getProperty("rules");
            if (rules != null) {
                certValidationParams.loadRules(Arrays.asList(rules.trim().split(",")));
            }
            final String metadata = prop.getProperty("metadata");
            if (metadata != null) {
                certValidationParams.loadMetadata(Arrays.asList(metadata.trim().split(",")));
            }
            fis.close();
        } catch (final IOException e) {
            log.error(WsVars.CONF_PATH_ERROR.getVar(), confPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return certValidationParams;
    }

}
