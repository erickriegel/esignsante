/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.requests;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.asipsante.api.sign.config.AsipSignConfigurations;
import fr.asipsante.api.sign.config.CACRLConfig;
import fr.asipsante.api.sign.config.ScheduledConfig;
import fr.asipsante.api.sign.config.WebConfig;
import fr.asipsante.api.sign.ws.model.Secret;

/**
 * The Class SignatureApiIntegrationTest.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AsipSignConfigurations.class, CACRLConfig.class, ScheduledConfig.class,
        WebConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan("fr.asipsante.api.sign.ws.api")
public class SecretApiIntegrationTest {

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

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    /**
     * Secret generation test.
     *
     * @throws Exception the exception
     */
    @Test
    public void secretGenTest() throws Exception {
        final Secret secret = new Secret();
        secret.setPlainSecret("password");

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        final ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        final String requestJson = ow.writeValueAsString(secret);

        mockMvc.perform(MockMvcRequestBuilders.post("/secrets").contentType(APPLICATION_JSON_UTF8).content(requestJson))
                .andExpect(status().isOk()).andDo(print());
    }

}