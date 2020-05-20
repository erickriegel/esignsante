/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.config.provider;

import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;

/**
 * Interface IAsipSignConfigurationsProvider
 */
public interface IAsipSignConfigurationsProvider {

    /**
     * load global conf
     *
     * @return IGlobalConf
     */
    IGlobalConf load();

}
