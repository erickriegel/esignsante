/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.delegate;

import java.util.Optional;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.ws.api.ConfigurationsApiDelegate;
import fr.asipsante.api.sign.ws.bean.Parameters;
import fr.asipsante.api.sign.ws.model.Conf;
import fr.asipsante.api.sign.ws.util.WsVars;

/**
 * The Class ConfigurationsApiDelegateImpl.
 */
@Service
public class ConfigurationsApiDelegateImpl extends ApiDelegate implements ConfigurationsApiDelegate {

    /** The log. */
    Logger log = LoggerFactory.getLogger(ConfigurationsApiDelegateImpl.class);

    /** The parameters. */
    @Autowired
    private Parameters parameters;

    /**
     * Gets the configurations.
     *
     * @return the configurations
     */
    @Override
    public ResponseEntity<Conf> getConfigurations() {
        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<Conf> re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            try {
                re = new ResponseEntity<>(parameters.getConfigs(), HttpStatus.OK);
            } catch (final AsipSignServerException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            log.warn("Le header Accept:application/json est absent.");
        }
        return re;
    }
}
