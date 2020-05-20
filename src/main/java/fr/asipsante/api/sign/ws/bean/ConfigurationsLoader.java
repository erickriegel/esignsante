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

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(ConfigurationsLoader.class);

    /**
     * The Constant CONF_PREFIX.
     */
    private static final String CONF_PREFIX = "config.sign.";

    /**
     * The ws conf path.
     */
    private String wsConfPath;

    /**
     * Enable/disable secret.
     */
    private String secretEnabled;

    /**
     * Instantiates a new configurations loader.
     *
     * @param secretEnabled the conf for accepting secrets
     * @param wsConfPath    the ws conf path
     */
    public ConfigurationsLoader(String secretEnabled, String wsConfPath) {
        super();
        this.wsConfPath = wsConfPath;
        this.secretEnabled = secretEnabled;
    }

    /**
     * Load.
     *
     * @param paramsList the params list
     */
    public void load(Parameters paramsList) {

        final Properties props = new Properties();
        final InputStream targetStream;
        String[] signIds = new String[0];
        String[] verifSignIds = new String[0];
        String[] verifCertIds = new String[0];
        String[] proofIds = new String[0];
        try {
            targetStream = new FileInputStream(wsConfPath);
            props.load(targetStream);
            signIds = getProperty(props, "config.sign.ids").trim().split(",");
            verifSignIds = getProperty(props, "config.verifsign.ids").trim().split(",");
            verifCertIds = getProperty(props, "config.verifcert.ids").trim().split(",");
            proofIds = getProperty(props, "config.proof.ids").trim().split(",");
        } catch (final IOException e) {
            log.error(WsVars.CONF_PATH_ERROR.getVar(), wsConfPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        for (final String signId : signIds) {
            final String signPathValue = getProperty(props, CONF_PREFIX + signId + WsVars.PROPS_PATH.getVar());
            final String signProofId = getProperty(props, CONF_PREFIX + signId + WsVars.PROPS_PROOF.getVar());
            final SignatureParameters params;
            try {
                params = loadConfSign(signPathValue);
                paramsList.getSignatureConfigurations().put(signId, params);
                paramsList.getSignatureProofId().put(signId, signProofId);

                if ("enable".equalsIgnoreCase(secretEnabled)) {
                    final String[] signSecrets = getProperty(props,
                            CONF_PREFIX + signId + WsVars.PROPS_SECRET.getVar()).trim().split(" ");
                    paramsList.getSignatureSecrets().put(signId, signSecrets);
                }
            } catch (AsipSignServerException | IllegalArgumentException e) {
                log.warn(WsVars.CONF_ERROR.getVar(), signPathValue);
                log.warn(ExceptionUtils.getFullStackTrace(e));
            }

        }

        for (final String verifSignId : verifSignIds) {
            final String verifPathvalue = getProperty(props,
                    "config.verifsign." + verifSignId + WsVars.PROPS_PATH.getVar());
            final SignatureValidationParameters params = loadConfSignValidation(verifPathvalue);
            paramsList.getSignatureValidationConfigurations().put(verifSignId, params);
        }

        for (final String verifCertId : verifCertIds) {
            final String certPathValue = getProperty(props,
                    "config.verifcert." + verifCertId + WsVars.PROPS_PATH.getVar());
            final CertificateValidationParameters params = loadConfCertValidation(certPathValue);
            paramsList.getCertificateValidationConfigurations().put(verifCertId, params);
        }
        // Attention, les conf de la preuve sont des conf de signature de la preuve,
        // les autres paramètres sont récupérés depuis la requête.
        for (final String proofId : proofIds) {
            final String proofPathValue = getProperty(props,
                    "config.proof." + proofId + WsVars.PROPS_PATH.getVar());
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
     * gets property.
     *
     * @param props the properties
     * @param key   the key
     * @return the value
     * @throws IllegalArgumentException missing parameter in property file
     */
    private String getProperty(Properties props, String key) {
        final String value = props.getProperty(key);
        if (value == null) {
            log.error(WsVars.CONF_MISSING.getVar(), key, wsConfPath);
            throw new IllegalArgumentException();
        }
        return value;
    }

    /**
     * Load conf sign.
     *
     * @param confPath the conf path
     * @return the signature parameters
     * @throws AsipSignServerException the asip sign server exception
     */
    private SignatureParameters loadConfSign(String confPath) throws AsipSignServerException {

        final Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(confPath);
            props.load(fis);
            fis.close();
        } catch (final IOException e) {
            log.error(WsVars.CONF_PATH_ERROR.getVar(), confPath);
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        final String uniquepassword = System.getProperty("asipsign.ws.unique.password");
        if (uniquepassword != null) {
            props.setProperty("pkcs12Password", uniquepassword);
        }

        return new SignatureParameters(props);
    }

    /**
     * Load conf sign validation.
     *
     * @param confPath the conf path
     * @return the signature validation parameters
     */
    private SignatureValidationParameters loadConfSignValidation(String confPath) {

        final Properties props = new Properties();
        FileInputStream fis = null;
        final SignatureValidationParameters signValidationParams = new SignatureValidationParameters();
        try {
            fis = new FileInputStream(confPath);
            props.load(fis);
            signValidationParams.setDescription(props.getProperty("description", ""));
            final String rules = props.getProperty("rules");
            if (rules != null) {
                signValidationParams.loadRules(Arrays.asList(rules.trim().split(",")));
            }
            final String metadata = props.getProperty("metadata");
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

        final Properties props = new Properties();
        FileInputStream fis = null;
        final CertificateValidationParameters certValidationParams = new CertificateValidationParameters();
        try {
            fis = new FileInputStream(confPath);
            props.load(fis);
            certValidationParams.setDescription(props.getProperty("description", ""));
            final String rules = props.getProperty("rules");
            if (rules != null) {
                certValidationParams.loadRules(Arrays.asList(rules.trim().split(",")));
            }
            final String metadata = props.getProperty("metadata");
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
