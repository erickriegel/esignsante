/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.config.provider.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.asipsante.api.sign.config.provider.AsipSignConfigurationsProvider;
import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;
import fr.asipsante.api.sign.ws.bean.config.impl.GlobalConfJson;
import fr.asipsante.api.sign.ws.bean.object.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The Class AsipSignConfigurations.
 */
@Configuration
public class AsipSignConfigurationsJson implements AsipSignConfigurationsProvider {

    /** The log. */
    Logger log = LoggerFactory.getLogger(AsipSignConfigurationsJson.class);

    /**
     * Load configuration file.
     *
     * @return the global configuration
     */
    @Bean
    @Lazy
    public IGlobalConf loadConfiguration() {
        final String jsonConf;
        IGlobalConf conf = null;
        final ObjectMapper mapper = new ObjectMapper();

        try {
            jsonConf = new String(Files.readAllBytes(Paths.get(System.getProperty("ws.conf"))));
            conf = mapper.readValue(jsonConf, GlobalConfJson.class);
            assert conf != null;

            for (SignatureConf signConf : conf.getSignature()){
                assert signConf.checkValid();
            }
            for (ProofConf proofConf : conf.getProof()){
                assert proofConf.checkValid();
            }
            for (SignVerifConf signVerifConf : conf.getSignatureVerification()){
                assert signVerifConf.checkValid();
            }
            for (CertVerifConf certVerifConf : conf.getCertificateVerification()){
                assert certVerifConf.checkValid();
            }
            for (CaConf caConf : conf.getCa()){
                assert caConf.checkValid();
            }
        } catch (IllegalAccessException | IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        log.info("AsipSignConfigurations loaded.");
        return conf;
    }

}
