/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.requests;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fr.asipsante.api.sign.config.AsipSignConfigurations;
import fr.asipsante.api.sign.config.CACRLConfig;
import fr.asipsante.api.sign.config.ScheduledConfig;
import fr.asipsante.api.sign.config.WebConfig;

/**
 * The Class SignatureApiIntegrationTestBigFile.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AsipSignConfigurations.class, CACRLConfig.class, ScheduledConfig.class,
        WebConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan("fr.asipsante.api.sign.ws.api")
public class SignatureApiIntegrationTestBigFile {

    /** The mock mvc. */
    @Autowired
    private MockMvc mockMvc;

    /** The doc. */
    private MockMultipartFile doc;

    static {
        final String confPath = Thread.currentThread().getContextClassLoader().getResource("asip-sign-ws.properties")
                .getPath();
        if (confPath != null) {
            System.setProperty("ws.conf", confPath);
        }
    }

    /**
     * Inits the.
     *
     * @throws Exception the exception
     */
    @Before
    public void init() throws Exception {
        doc = new MockMultipartFile("file",
                Thread.currentThread().getContextClassLoader().getResourceAsStream("ASIP_TXT_H0440001_Enteteflux.txt"));
        assertNotNull("Le fichier n'a pas été lu.", doc);
    }

    /**
     * Signature XM ldsig test with proof.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestWithProof() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.fileUpload("/signatures/xmldsigwithproof").file(doc).param("idSignConf", "1")
                        .param("idVerifSignConf", "1").param("requestId", "Request-1").param("proofTag", "MonTAG")
                        .param("applicantId", "RPPS").param("idProofConf", "1").accept("application/json"))
                .andExpect(status().isOk()).andDo(print());
    }

    /**
     * Signature XM ldsig test no proof.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestNoProof() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig").file(doc).param("idSignConf", "1")
                .accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Signature xades test with proof.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXadesTestWithProof() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xadesbaselinebwithproof").file(doc)
                .param("idSignConf", "1").param("idVerifSignConf", "1").param("requestId", "Request-1")
                .param("proofTag", "MonTAG").param("applicantId", "RPPS").param("idProofConf", "1")
                .accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Signature xades test no proof.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXadesTestNoProof() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xadesbaselineb").file(doc)
                .param("idSignConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }
}
