/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;
import fr.asipsante.api.sign.ws.bean.config.impl.GlobalConfJson;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * The Class ConfigurationLoaderTest.
 */
public class ConfigurationLoaderTest {

    /** The ws conf path good. */
    private static String wsConfPathGood;

    /**
     * Inits the.
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void init() throws Exception {
        wsConfPathGood = String.valueOf(Paths.get(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource("asipsign-conf.json")).toURI()).toString()));
    }

    /**
     * Configs ok test.
     */
    @Test
    public void configsOkTest() throws IOException {
        final String jsonConf = new String(Files.readAllBytes(Paths.get(wsConfPathGood)));
        final ObjectMapper mapper = new ObjectMapper();
        final IGlobalConf conf = mapper.readValue(jsonConf, GlobalConfJson.class);
        assertFalse("La liste des configs de signature est vide", conf.getSignature().isEmpty());
        assertEquals("La liste ne contient pas le bon nombre de configuration de signature", 5,
                conf.getSignature().size());
    }

}
