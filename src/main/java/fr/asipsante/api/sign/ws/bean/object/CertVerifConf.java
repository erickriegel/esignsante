/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean.object;

import fr.asipsante.api.sign.ws.bean.object.utils.RulesFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * The type Cert verif conf.
 */
public class CertVerifConf {

    /** The log. */
    private static final Logger log = LoggerFactory.getLogger(CertVerifConf.class);

    /**
     * idVerifCert.
     */
    private String idVerifCert;

    /**
     * description.
     */
    private String description;

    /**
     * metadata.
     */
    private String metadata;

    /**
     * rules.
     */
    private String rules;

    /**
     * Instantiates a new Cert verif conf.
     */
    public CertVerifConf() {
    }

    /**
     * Gets id verif cert.
     *
     * @return the id verif cert
     */
    public String getIdVerifCert() {
        return idVerifCert;
    }

    /**
     * Sets id verif cert.
     *
     * @param idVerifCert the id verif cert
     */
    public void setIdVerifCert(final String idVerifCert) {
        this.idVerifCert = idVerifCert;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Gets metadata.
     *
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Sets metadata.
     *
     * @param metadata the metadata
     */
    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets rules.
     *
     * @return the rules
     */
    public String getRules() {
        return rules;
    }

    /**
     * Sets rules.
     *
     * @param rules the rules
     */
    public void setRules(final String rules) {
        this.rules = RulesFormatting.formatCertRules(rules);
    }

    @Override
    public String toString() {
        return "CertVerifConf{" +
                "idVerifCert='" + idVerifCert + '\'' +
                ", description='" + description + '\'' +
                ", metadata='" + metadata + '\'' +
                ", rules='" + rules + '\'' +
                '}';
    }

    /**
     * Check valid boolean.
     *
     * @return the boolean
     * @throws IllegalAccessException the illegal access exception
     */
    public boolean checkValid() throws IllegalAccessException {
        for (final Field f : getClass().getDeclaredFields()) {
            if (f.get(this) == null) {
                log.error("Missing field in object {}", this.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }

}
