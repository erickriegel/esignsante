package fr.asipsante.api.sign.config;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.ws.bean.ConfigurationsLoader;
import fr.asipsante.api.sign.ws.bean.Parameters;

/**
 * The Class AsipSignConfigurations.
 */
@Configuration
public class AsipSignConfigurations {

    /** The log. */
    Logger log = LoggerFactory.getLogger(AsipSignConfigurations.class);

    /**
     * Load parameters.
     *
     * @return the parameters
     * @throws AsipSignServerException
     *             the asip sign server exception
     * @throws UnsupportedEncodingException
     *             the unsupported encoding exception
     */
    @Bean
    @Lazy
    public Parameters loadParameters()
            throws AsipSignServerException, UnsupportedEncodingException {
        ConfigurationsLoader loader = new ConfigurationsLoader(
                System.getProperty("ws.conf"));
        Parameters params = new Parameters();
        loader.load(params);
        log.info("AsipSignConfigurations loaded.");
        return params;
    }

}
