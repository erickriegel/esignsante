/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.config.observer;

import fr.asipsante.api.sign.config.utils.CaCrlServiceLoader;
import fr.asipsante.api.sign.service.ICACRLService;
import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * The Class CaCrlObserver.
 */
@Component
public class CaCrlObserver {

    /** The log. */
    Logger log = LoggerFactory.getLogger(CaCrlObserver.class);

    /**
     * The GlobalConf object.
     */
    @Autowired
    private IGlobalConf globalConf;

    /**
     * The service ca crl.
     */
    @Autowired
    private ICACRLService serviceCaCrl;

    /**
     * Add observer.
     */
    @PostConstruct
    public void addObserver() {
        globalConf.addObserver(this);
    }

    /**
     * reload CAs and CRLs on detected change in GlobalConf.
     */
    public void update() {
        try {
            CaCrlServiceLoader.loadCaCrl(serviceCaCrl, globalConf.getCa());
        } catch (IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

}
