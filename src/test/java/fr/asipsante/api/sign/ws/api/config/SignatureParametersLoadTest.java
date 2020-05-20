/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.config;

import static fr.asipsante.api.sign.ws.bean.ConfigurationsMapper.mapVerifSignConfig;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.asipsante.api.sign.bean.metadata.MetaDatum;
import fr.asipsante.api.sign.bean.parameters.SignatureParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureValidationParameters;
import fr.asipsante.api.sign.enums.MetaDataType;
import fr.asipsante.api.sign.validation.signature.rules.IVisitor;
import fr.asipsante.api.sign.validation.signature.rules.impl.TrustedCertificat;
import fr.asipsante.api.sign.ws.bean.ConfigurationsMapper;
import fr.asipsante.api.sign.ws.model.ConfVerifSign;

/**
 * The Class SignatureParametersLoadTest.
 */
public class SignatureParametersLoadTest {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SignatureParametersLoadTest.class);

    /** The filename. */
    private String filename = "sign-1.properties";

    /** The params. */
    private SignatureParameters params;

    /**
     * Gets the sign packaging test.
     *
     * @return the sign packaging test
     * @throws Exception the exception
     */
    @Test
    public void getSignPackagingTest() throws Exception {
        final Properties props = new Properties();
        props.load(ClassLoader.getSystemResourceAsStream(filename));
        params = new SignatureParameters(props);
        assertTrue("Le packaging n'est pas configuré.", params.getSignPackaging() != null);
        assertTrue("Le paramètre signId n'est pas renseigné.", params.getSignId() != null);

    }

    /**
     * Test rules definitions.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRulesDefinitions() throws Exception {
        LOG.info(ConfigurationsMapper.class.getSimpleName());
        final IVisitor rule = new TrustedCertificat();
        final List<MetaDatum> metadata = new ArrayList<>();
        metadata.add(new MetaDatum(MetaDataType.DATE_SIGNATURE, ""));
        final SignatureValidationParameters validationParams = new SignatureValidationParameters();
        final List<IVisitor> rules = new ArrayList<>();
        rules.add(rule);
        validationParams.setRules(rules);
        validationParams.setMetaData(metadata);
        final Map<String, SignatureValidationParameters> map = new HashMap<>();
        map.put("1", validationParams);
        final List<ConfVerifSign> liste = mapVerifSignConfig(map);
        for (final ConfVerifSign confVerifSign : liste) {
            assertTrue("L'autorité de Certification n'est pas reconnue.", confVerifSign.getRules().get(0)
                    .getDescription().equals("L'autorité de Certification est reconnue."));
        }

    }

}
