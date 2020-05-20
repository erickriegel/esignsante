/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.util;

/**
 * The enum Vars.
 */
public enum WsVars {

    /** Le type du header. */
    HEADER_TYPE("application/json"),

    /** Le message de log d'une conf invalide. */
    CONF_ERROR("Fichier de configuration {} invalide, configuration ignorée"),

    /** Le message de log d'un chemin de conf incorrect. */
    CONF_PATH_ERROR("Chemin du fichier de configuration incorrect, ou erreur sur le fichier renseigné: {}"),

    /** La partie .path des properties pour les chemins des fichiers de conf. */
    PROPS_PATH(".path");

    /** The var. */
    private final String var;

    /**
     * Instantiates a new vars.
     *
     * @param var the var
     */
    private WsVars(String var) {
        this.var = var;
    }

    /**
     * Gets the var.
     *
     * @return the var
     */
    public String getVar() {
        return var;
    }
}
