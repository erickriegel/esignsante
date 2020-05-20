package fr.asipsante.api.sign.config;

import fr.asipsante.api.sign.config.provider.IAsipSignConfigurationsProvider;
import fr.asipsante.api.sign.config.provider.impl.AsipSignConfigurationsJson;
import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * The type App global config.
 */
@Configuration
public class AppGlobalConfig {

    /**
     * Load configuration file.
     *
     * @return the global configuration
     */
    @Bean
    @Lazy
    public IGlobalConf loadConfiguration() {
        final IAsipSignConfigurationsProvider confProvider = new AsipSignConfigurationsJson();
        return confProvider.load();
    }

}
