/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */

package fr.asipsante.api.sign.scheduled;

import fr.asipsante.api.sign.config.utils.CaCrlServiceLoader;
import fr.asipsante.api.sign.service.ICACRLService;
import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * The Class RefreshConfigurations.
 * On télécharge les CRL par défaut une fois toutes les 24 heures du moment du lancement de l'appli
 * Si config.crl.scheduling est renseigné, le rechargement suit l'expression cron.
 */
@Component
@ComponentScan("fr.asipsante.api.sign")
public class RefreshConfigurations {

    /** The log. */
    Logger log = LoggerFactory.getLogger(RefreshConfigurations.class);

    /** The global conf. */
    @Autowired
    private IGlobalConf globalConf;

    /** The cacrl service. */
    @Autowired
    private ICACRLService cacrlService;

    @Value("${config.crl.scheduling ?:}")
    private String isCron;

    /**
     * Refresh crl.
     */
    @Scheduled(cron = "${config.crl.scheduling ?: 0 0 0 29 2 ?}")
    public void refreshCrl() {
        try {
            CaCrlServiceLoader.loadCaCrl(cacrlService, globalConf.getCa());
        } catch (final IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * Refresh crl fixed rate.
     * 86400000 ms = 1 day
     */
    @Scheduled(fixedRate = 86400000)
    public void refreshCrlDefault() {
        if (isCron == null || "".equals(isCron)) {
            refreshCrl();
        }
    }
}
