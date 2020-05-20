/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.scheduled;

import fr.asipsante.api.sign.service.ICACRLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * The Class RefreshConfigurations.
 */
@Component
@ComponentScan("fr.asipsante.api.sign")
public class RefreshConfigurations {

    /** The ca path. */
    @Value("${config.ca.path}")
    private String caPath;

    /** The crl path. */
    @Value("${config.crl.path}")
    private String crlPath;

    /** The cacrl service. */
    @Autowired
    private ICACRLService cacrlService;

    /**
     * Refresh ca.
     */
    @Scheduled(cron = "${config.ca.scheduling}")
    public void refreshCa() {
        cacrlService.loadCA(new File(caPath));
    }

    /**
     * Refresh crl.
     */
    @Scheduled(cron = "${config.crl.scheduling}")
    public void refreshCrl() {
        cacrlService.loadCRL(new File(crlPath));
    }

}
