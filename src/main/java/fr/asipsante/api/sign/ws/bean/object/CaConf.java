/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * The type Ca conf.
 */
public class CaConf {

    /** The log. */
    private static final Logger log = LoggerFactory.getLogger(CaConf.class);

    /**
     * certificate.
     */
    private String certificate;

    /**
     * crl.
     */
    private String crl;

    /**
     * Instantiates a new Ca conf.
     */
    public CaConf() {
    }

    /**
     * Instantiates a new Ca conf.
     *
     * @param certificate the certificate
     * @param crl         the crl
     */
    public CaConf(final String certificate, final String crl) {
        this.certificate = certificate;
        this.crl = crl;
    }

    /**
     * Gets certificate.
     *
     * @return the certificate
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * Sets certificate.
     *
     * @param certificate the certificate
     */
    public void setCertificate(final String certificate) {
        this.certificate = certificate;
    }

    /**
     * Gets crl.
     *
     * @return the crl
     */
    public String getCrl() {
        return crl;
    }

    /**
     * Sets crl.
     *
     * @param crl the crl
     */
    public void setCrl(final String crl) {
        this.crl = crl;
    }

    @Override
    public String toString() {
        return "CaConf{" +
                "certificate='" + certificate + '\'' +
                ", crl='" + crl + '\'' +
                '}';
    }

    /**
     * Check valid boolean.
     *
     * @return the boolean
     * @throws IllegalAccessException the illegal access exception
     */
    public boolean checkValid() throws IllegalAccessException {
        for (final Field f : getClass().getDeclaredFields())
            if (f.get(this) == null) {
                log.error("Missing field in object {}", this.getClass().getSimpleName());
                return false;
            }
        return true;
    }
}
