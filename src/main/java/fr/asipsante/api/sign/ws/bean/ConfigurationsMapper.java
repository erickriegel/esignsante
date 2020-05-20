package fr.asipsante.api.sign.ws.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
    private static Logger log = LoggerFactory
            .getLogger(ConfigurationsMapper.class);

    /** The rules definition. */
    private static Properties rulesDefinition = new Properties();

    static {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("rules.properties")){

            if (is != null) {
                rulesDefinition.load(is);
            } else {
                rulesDefinition.put("defaultKey", "defaultValue");
            }
        } catch (IOException e) {
            log.warn("Erreur de lecture du fichier de définition des règles");
        }
    }

    /**
     * Map sign config.
     *
     * @param signParametersMap
     *            the sign parameters map
     * @return the list
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    public static List<ConfSign> mapSignConfig(
            Map<String, SignatureParameters> signParametersMap)
            throws AsipSignServerException {
        List<ConfSign> signConfigs = new ArrayList<ConfSign>();
        for (Map.Entry<String, SignatureParameters> entry : signParametersMap
                .entrySet()) {
            ConfSign config = new ConfSign();
            config.idSignConf(Long.valueOf(entry.getKey()));
            config.description(entry.getValue().getDescription());
            config.digestAlgorithm(DigestAlgorithmEnum
                    .fromValue(entry.getValue().getDigestAlgo().name()));
            config.canonicalisationAlgorithm(CanonicalisationAlgorithmEnum
                    .fromValue(entry.getValue().getCanonAlgo()));
            config.signaturePackaging(SignaturePackagingEnum
                    .fromValue(entry.getValue().getSignPackaging().name()));
            config.dn(getDn(entry.getValue().getKsFile(),
                    entry.getValue().getPassword()));
            signConfigs.add(config);
        }

        return signConfigs;
    }

    /**
     * Map proof config.
     *
     * @param signProofParametersMap
     *            the sign proof parameters map
     * @return the list
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    public static List<ConfProof> mapProofConfig(
            Map<String, SignatureParameters> signProofParametersMap)
            throws AsipSignServerException {
        List<ConfProof> proofConfigs = new ArrayList<ConfProof>();
        for (Map.Entry<String, SignatureParameters> entry : signProofParametersMap
                .entrySet()) {
            ConfProof config = new ConfProof();
            config.idProofConf(Long.valueOf(entry.getKey()));
            config.description(entry.getValue().getDescription());
            config.digestAlgorithmForProof(DigestAlgorithmForProofEnum
                    .fromValue(entry.getValue().getDigestAlgo().name()));
            config.canonicalisationAlgorithmForProof(
                    CanonicalisationAlgorithmForProofEnum
                            .fromValue(entry.getValue().getCanonAlgo()));
            config.dn(getDn(entry.getValue().getKsFile(),
                    entry.getValue().getPassword()));
            proofConfigs.add(config);
        }

        return proofConfigs;
    }

    /**
     * Map verif cert config.
     *
     * @param verifCertParametersMap
     *            the verif cert parameters map
     * @return the list
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    public static List<ConfVerifCert> mapVerifCertConfig(
            Map<String, CertificateValidationParameters> verifCertParametersMap)
            throws AsipSignServerException {
        List<ConfVerifCert> verifCertConfigs = new ArrayList<ConfVerifCert>();
        for (Map.Entry<String, CertificateValidationParameters> entry : verifCertParametersMap
                .entrySet()) {
            ConfVerifCert config = new ConfVerifCert();
            config.idVerifCertConf(Long.valueOf(entry.getKey()));
            config.description(entry.getValue().getDescription());
            List<MetaDatum> metadata = entry.getValue().getMetaData();
            List<String> metadataNames = new ArrayList<String>();
            for (MetaDatum metadatum : metadata) {
                metadataNames.add(metadatum.getType().getName());
            }
            config.metaData(metadataNames);

            List<ICertificatVisitor> rules = entry.getValue().getRules();
            List<Rule> rulesAsString = new ArrayList<Rule>();
            for (ICertificatVisitor rule : rules) {
                Rule ruleAsString = new Rule();
                ruleAsString.id(rule.getClass().getSimpleName());
                ruleAsString.description(rulesDefinition.getProperty(
                        rule.getClass().getSimpleName(), "Pas de description"));
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
     * @param verifSignParametersMap
     *            the verif sign parameters map
     * @return the list
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    public static List<ConfVerifSign> mapVerifSignConfig(
            Map<String, SignatureValidationParameters> verifSignParametersMap)
            throws AsipSignServerException {
        List<ConfVerifSign> verifSignConfigs = new ArrayList<ConfVerifSign>();
        for (Map.Entry<String, SignatureValidationParameters> entry : verifSignParametersMap
                .entrySet()) {
            ConfVerifSign config = new ConfVerifSign();
            config.idVerifSignConf(Long.valueOf(entry.getKey()));
            config.description(entry.getValue().getDescription());
            List<MetaDatum> metadata = entry.getValue().getMetaData();
            List<String> metadataNames = new ArrayList<String>();
            for (MetaDatum metadatum : metadata) {
                metadataNames.add(metadatum.getType().getName());
            }
            config.metaData(metadataNames);
            List<IVisitor> rules = entry.getValue().getRules();
            List<Rule> rulesAsString = new ArrayList<Rule>();
            for (IVisitor rule : rules) {
                Rule ruleAsString = new Rule();
                ruleAsString.id(rule.getClass().getSimpleName());
                ruleAsString.description(rulesDefinition.getProperty(
                        rule.getClass().getSimpleName(), "Pas de description"));
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
     * @param keystore
     *            the keystore
     * @param password
     *            the password
     * @return the dn
     * @throws AsipSignServerException
     *             the asip sign server exception
     */
    private static String getDn(File keystore, String password)
            throws AsipSignServerException {

        KeyStore p12;

        try {
            p12 = KeyStore.getInstance("pkcs12");
            p12.load(new FileInputStream(keystore), password.toCharArray());

            Enumeration<String> e = p12.aliases();
            while (e.hasMoreElements()) {
                String alias = e.nextElement();
                X509Certificate c = (X509Certificate) p12.getCertificate(alias);
                return c.getSubjectDN().getName();
            }
        } catch (GeneralSecurityException | IOException e) {
            log.error("erreur lors de la récupération du DN", e);
            throw new AsipSignServerException(e);
        }

        return "could not produce subject dn";
    }

}
