/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.asipsante.api.sign.bean.metadata.MetaDatum;
import fr.asipsante.api.sign.bean.parameters.CertificateValidationParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureValidationParameters;
import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.validation.certificat.rules.ICertificatVisitor;
import fr.asipsante.api.sign.validation.signature.rules.IVisitor;
import fr.asipsante.api.sign.ws.model.ConfProof;
import fr.asipsante.api.sign.ws.model.ConfProof.CanonicalisationAlgorithmForProofEnum;
import fr.asipsante.api.sign.ws.model.ConfProof.DigestAlgorithmForProofEnum;
import fr.asipsante.api.sign.ws.model.ConfSign;
import fr.asipsante.api.sign.ws.model.ConfSign.CanonicalisationAlgorithmEnum;
import fr.asipsante.api.sign.ws.model.ConfSign.DigestAlgorithmEnum;
import fr.asipsante.api.sign.ws.model.ConfSign.SignaturePackagingEnum;
import fr.asipsante.api.sign.ws.model.ConfVerifCert;
import fr.asipsante.api.sign.ws.model.ConfVerifSign;
import fr.asipsante.api.sign.ws.model.Rule;

/**
 * The Class ConfigurationsMapper.
 */
public class ConfigurationsMapper {

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(ConfigurationsMapper.class);

    /** The rules definition. */
    private static Properties rulesDefinition = new Properties();

    /**
     * Instantiates a new configurations mapper.
     */
    private ConfigurationsMapper() {
        throw new IllegalStateException("Utility class");
    }

    static {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("rules.properties")) {

            if (is != null) {
                rulesDefinition.load(is);
            } else {
                rulesDefinition.put("defaultKey", "defaultValue");
            }
        } catch (final IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * Map sign config.
     *
     * @param signParametersMap the sign parameters map
     * @return the list
     * @throws AsipSignServerException the asip sign server exception
     */
    public static List<ConfSign> mapSignConfig(Map<String, SignatureParameters> signParametersMap)
            throws AsipSignServerException {
        final List<ConfSign> signConfigs = new ArrayList<>();
        for (final Map.Entry<String, SignatureParameters> entry : signParametersMap.entrySet()) {
            final ConfSign config = new ConfSign();
            config.idSignConf(Long.valueOf(entry.getKey()));
            config.description(entry.getValue().getDescription());
            config.digestAlgorithm(DigestAlgorithmEnum.fromValue(entry.getValue().getDigestAlgo().name()));
            config.canonicalisationAlgorithm(CanonicalisationAlgorithmEnum.fromValue(entry.getValue().getCanonAlgo()));
            config.signaturePackaging(SignaturePackagingEnum.fromValue(entry.getValue().getSignPackaging().name()));
            config.dn(getDn(entry.getValue().getKsFile(), entry.getValue().getPassword()));
            signConfigs.add(config);
        }

        return signConfigs;
    }

    /**
     * Map proof config.
     *
     * @param signProofParametersMap the sign proof parameters map
     * @return the list
     * @throws AsipSignServerException the asip sign server exception
     */
    public static List<ConfProof> mapProofConfig(Map<String, SignatureParameters> signProofParametersMap)
            throws AsipSignServerException {
        final List<ConfProof> proofConfigs = new ArrayList<>();
        for (final Map.Entry<String, SignatureParameters> entry : signProofParametersMap.entrySet()) {
            final ConfProof config = new ConfProof();
            config.idProofConf(Long.valueOf(entry.getKey()));
            config.description(entry.getValue().getDescription());
            config.digestAlgorithmForProof(
                    DigestAlgorithmForProofEnum.fromValue(entry.getValue().getDigestAlgo().name()));
            config.canonicalisationAlgorithmForProof(
                    CanonicalisationAlgorithmForProofEnum.fromValue(entry.getValue().getCanonAlgo()));
            config.dn(getDn(entry.getValue().getKsFile(), entry.getValue().getPassword()));
            proofConfigs.add(config);
        }

        return proofConfigs;
    }

    /**
     * Map verif cert config.
     *
     * @param verifCertParametersMap the verif cert parameters map
     * @return the list
     */
    public static List<ConfVerifCert> mapVerifCertConfig(
            Map<String, CertificateValidationParameters> verifCertParametersMap) {
        final List<ConfVerifCert> verifCertConfigs = new ArrayList<>();
        for (final Map.Entry<String, CertificateValidationParameters> entry : verifCertParametersMap.entrySet()) {
            final ConfVerifCert config = new ConfVerifCert();
            config.idVerifCertConf(Long.valueOf(entry.getKey()));
            config.description(entry.getValue().getDescription());
            final List<MetaDatum> metadata = entry.getValue().getMetaData();
            final List<String> metadataNames = new ArrayList<>();
            for (final MetaDatum metadatum : metadata) {
                metadataNames.add(metadatum.getType().getName());
            }
            config.metaData(metadataNames);

            final List<ICertificatVisitor> rules = entry.getValue().getRules();
            final List<Rule> rulesAsString = new ArrayList<>();
            for (final ICertificatVisitor rule : rules) {
                final Rule ruleAsString = new Rule();
                ruleAsString.id(rule.getClass().getSimpleName());
                ruleAsString.description(
                        rulesDefinition.getProperty(rule.getClass().getSimpleName(), "Pas de description"));
                rulesAsString.add(ruleAsString);
            }
            config.rules(rulesAsString);
            verifCertConfigs.add(config);
        }

        return verifCertConfigs;
    }

    /**
     * Map verif sign config.
     *
     * @param verifSignParametersMap the verif sign parameters map
     * @return the list
     */
    public static List<ConfVerifSign> mapVerifSignConfig(
            Map<String, SignatureValidationParameters> verifSignParametersMap) {
        final List<ConfVerifSign> verifSignConfigs = new ArrayList<>();
        for (final Map.Entry<String, SignatureValidationParameters> entry : verifSignParametersMap.entrySet()) {
            final ConfVerifSign config = new ConfVerifSign();
            config.idVerifSignConf(Long.valueOf(entry.getKey()));
            config.description(entry.getValue().getDescription());
            final List<MetaDatum> metadata = entry.getValue().getMetaData();
            final List<String> metadataNames = new ArrayList<>();
            for (final MetaDatum metadatum : metadata) {
                metadataNames.add(metadatum.getType().getName());
            }
            config.metaData(metadataNames);
            final List<IVisitor> rules = entry.getValue().getRules();
            final List<Rule> rulesAsString = new ArrayList<>();
            for (final IVisitor rule : rules) {
                final Rule ruleAsString = new Rule();
                ruleAsString.id(rule.getClass().getSimpleName());
                ruleAsString.description(
                        rulesDefinition.getProperty(rule.getClass().getSimpleName(), "Pas de description"));
                rulesAsString.add(ruleAsString);
            }
            config.rules(rulesAsString);
            verifSignConfigs.add(config);
        }
        return verifSignConfigs;
    }

    /**
     * Gets the dn.
     *
     * @param keystore the keystore
     * @param password the password
     * @return the dn
     * @throws AsipSignServerException the asip sign server exception
     */
    private static String getDn(File keystore, String password) throws AsipSignServerException {

        String dn = "could not produce subject dn";
        final KeyStore p12;
        try (FileInputStream fis = new FileInputStream(keystore)) {
            p12 = KeyStore.getInstance("pkcs12");
            p12.load(fis, password.toCharArray());
            final Enumeration<String> e = p12.aliases();
            while (e.hasMoreElements()) {
                final String alias = e.nextElement();
                final X509Certificate c = (X509Certificate) p12.getCertificate(alias);
                dn = c.getSubjectDN().getName();
            }
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            throw new AsipSignServerException();
        }

        return dn;
    }
}
