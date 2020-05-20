/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.asipsante.api.sign.ws.bean.ConfigurationLoader;
import fr.asipsante.api.sign.ws.bean.object.GlobalConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.ws.bean.ConfigurationsLoader;
import fr.asipsante.api.sign.ws.bean.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The Class AsipSignConfigurations.
 */
@Configuration
public class AsipSignConfigurations {

    /** The log. */
    Logger log = LoggerFactory.getLogger(AsipSignConfigurations.class);

    /** Enable/disable secret. */
    @Value("${config.secret}")
    private String secretEnabled;

    public String getSecretEnabled() {
        return secretEnabled;
    }

    /**
     * Load parameters.
     *
     * @return the parameters
     * @throws AsipSignServerException the asip sign server exception
     */
    @Bean
    @Lazy
    public Parameters loadParameters() throws AsipSignServerException {
        final ConfigurationsLoader loader = new ConfigurationsLoader(secretEnabled, System.getProperty("ws.conf"));
        final Parameters params = new Parameters();
        loader.load(params);
        log.info("AsipSignConfigurations loaded.");
        return params;
    }

    /**
     * Load configuration file.
     *
     * @return the global configuration
     * @throws IOException file read exception
     */
    @Bean
    @Lazy
    public GlobalConf loadConfiguration() throws IOException {
        final String jsonConf = new String(Files.readAllBytes(Paths.get(System.getProperty("ws.conf"))));
        final ObjectMapper mapper = new ObjectMapper();
        GlobalConf conf = mapper.readValue(jsonConf, GlobalConf.class);
        log.info("AsipSignConfigurations loaded.");
        return conf;
    }

}
