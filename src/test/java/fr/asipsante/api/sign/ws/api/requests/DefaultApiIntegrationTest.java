/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.requests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import fr.asipsante.api.sign.config.CACRLConfig;
import fr.asipsante.api.sign.config.ScheduledConfig;
import fr.asipsante.api.sign.config.WebConfig;

/**
 * The Class DefaultApiIntegrationTest.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CACRLConfig.class, ScheduledConfig.class, WebConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan("fr.asipsante.api.sign.ws.api")
public class DefaultApiIntegrationTest {

    /** The mock mvc. */
    @Autowired
    private MockMvc mockMvc;

    static {
        final String confPath = Thread.currentThread().getContextClassLoader().getResource("asip-sign-ws.properties")
                .getPath();
        if (confPath != null) {
            System.setProperty("ws.conf", confPath);
        }
    }

    /**
     * Test passant qui permet de récuperer la liste des opérations disponibles.
     *
     * @throws Exception the exception
     */
    @Test
    public void rootGetTest() throws Exception {
        mockMvc.perform(get("/").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }
}
