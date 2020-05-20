/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean.object;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class SignVerifConf {

    private String idVerifSign;

    private String description;

    private String metadata;

    private String rules;

    public SignVerifConf() {
    }

    public SignVerifConf(final String idVerifSign, final String description, final String metadata, final String rules) {
        this.idVerifSign = idVerifSign;
        this.description = description;
        this.metadata = metadata;
        this.rules = rules;
    }

    public String getIdVerifSign() {
        return idVerifSign;
    }

    public void setIdVerifSign(final String idVerifSign) {
        this.idVerifSign = idVerifSign;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(final String rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "SignVerifConf{" +
                "idVerifSign='" + idVerifSign + '\'' +
                ", description='" + description + '\'' +
                ", metadata='" + metadata + '\'' +
                ", rules='" + rules + '\'' +
                '}';
    }

    public boolean checkValid() throws IllegalAccessException {
        // remove duplicate rules from list
        final ArrayList<String> rulesList = new ArrayList<>(Arrays.asList(rules.trim().split(",")));
        rulesList.add("DocumentIntact");   // add obligatory rules
        rulesList.add("SignatureIntacte");
        rulesList.add("SignatureCertificatValide");
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
