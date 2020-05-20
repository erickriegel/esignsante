/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean.object;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * The type Cert verif conf.
 */
public class CertVerifConf {

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
     * Instantiates a new Cert verif conf.
     *
     * @param idVerifCert the id verif cert
     * @param description the description
     * @param metadata    the metadata
     * @param rules       the rules
     */
    public CertVerifConf(final String idVerifCert, final String description,
                         final String metadata, final String rules) {
        this.idVerifCert = idVerifCert;
        this.description = description;
        this.metadata = metadata;
        this.rules = rules;
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
        this.rules = rules;
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
        // remove duplicate rules from list
        final ArrayList<String> rulesList = new ArrayList<>(Arrays.asList(rules.trim().split(",")));
        // linked hashset to remove duplicates
        final LinkedHashSet<String> lhSetRules = new LinkedHashSet<>(rulesList);
        // create array from the LinkedHashSet and replace rules with correct value
        rules = Arrays.toString(lhSetRules.toArray(new String[0]))
                .replaceAll("\\[", "").replaceAll("]", "");

        for (final Field f : getClass().getDeclaredFields())
            if (f.get(this) == null)
                return false;
        return true;
    }
}
