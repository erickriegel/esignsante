package fr.asipsante.api.sign.config.provider;

import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;

import java.io.IOException;

public interface AsipSignConfigurationsProvider {

    IGlobalConf loadConfiguration();

}
