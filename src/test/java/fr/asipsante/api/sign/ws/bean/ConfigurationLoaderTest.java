/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The Class ConfigurationLoaderTest.
 */
public class ConfigurationLoaderTest {

    /** The loader. */
    private ConfigurationsLoader loader;

    /** The ws conf path good. */
    private static String wsConfPathGood;

    /** The ws conf path bad. */
    private static String wsConfPathBad;

    /**
     * Inits the.
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void init() throws Exception {
        wsConfPathGood = Thread.currentThread().getContextClassLoader().getResource("asip-sign-ws.properties").toURI()
                .getPath();
        wsConfPathBad = Thread.currentThread().getContextClassLoader()
                .getResource("asip-sign-ws-test-bad-configs.properties").toURI().getPath();
    }

    /**
     * Configs ok test.
     */
    @Test
    public void configsOkTest() {
        loader = new ConfigurationsLoader(wsConfPathGood);
        final Parameters params = new Parameters();
        loader.load(params);
        assertFalse("La liste des configs de signature est vide", params.getSignatureConfigurations().isEmpty());
        assertEquals("La liste ne contient pas le bon nombre de configuration de signature", 5,
                params.getSignatureConfigurations().size());
    }

    /**
     * Configs bad test.
     */
    @Test
    public void configsBadTest() {
        loader = new ConfigurationsLoader(wsConfPathBad);
        final Parameters params = new Parameters();
        loader.load(params);
        assertTrue("La liste des configs de signature devrait être vide",
                params.getSignatureConfigurations().isEmpty());
    }

}
