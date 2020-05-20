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

import fr.asipsante.api.sign.config.provider.impl.AsipSignConfigurationsJson;
import fr.asipsante.api.sign.config.CACRLConfig;
import fr.asipsante.api.sign.config.ScheduledConfig;
import fr.asipsante.api.sign.config.WebConfig;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * The Class SignatureApiIntegrationTest.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AsipSignConfigurationsJson.class, CACRLConfig.class, ScheduledConfig.class,
        WebConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan("fr.asipsante.api.sign.ws.api")
public class SignatureApiIntegrationTest {

    /** The mock mvc. */
    @Autowired
    private MockMvc mockMvc;

    /** The xml. */
    private MockMultipartFile xml;

    /** The texte. */
    private MockMultipartFile texte;

    static {
        final String confPath;
        try {
            confPath = String.valueOf(Paths.get(Paths.get(Objects.requireNonNull(Thread.currentThread().
                    getContextClassLoader().getResource("asipsign-conf.json")).toURI()).toString()));
            System.setProperty("ws.conf", confPath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inits the.
     *
     * @throws Exception the exception
     */
    @Before
    public void init() throws Exception {
        xml = new MockMultipartFile("file", Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("Fichier_TOMWS2_SANS_SIGNATURE.xml"));

        texte = new MockMultipartFile("file",
                Thread.currentThread().getContextClassLoader().getResourceAsStream("outerjoint.txt"));
        assertNotNull("Le fichier n'a pas été lu.", xml);
        assertNotNull("Le fichier n'a pas été lu.", texte);
    }

    /**
     * Cas non passant de signature XMLDSIG avec un certificat expiré.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigCertExpire() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig").file(xml)
                .param("secret", "lalalalalala").param("idSignConf", "5").accept("application/json"))
                .andExpect(status().isServiceUnavailable()).andDo(print());
    }

    /**
     * Cas non passant de signature XMLDSIG avec preuve avec un certificat expiré.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigWithProofCertExpire() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.fileUpload("/signatures/xmldsigwithproof").file(xml).param("secret", "password")
                        .param("idSignConf", "3").param("idVerifSignConf", "1").param("requestId", "Request-1")
                        .param("proofTag", "MonTAG").param("applicantId", "RPPS").accept("application/json"))
                .andExpect(status().isServiceUnavailable()).andDo(print());
    }

    /**
     * Cas non passant de signature XMLDSIG avec un certificat révoqué.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigCertRevoque() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig").file(xml)
                .param("secret", "poopopioppopop").param("idSignConf", "6").accept("application/json"))
                .andExpect(status().isServiceUnavailable()).andDo(print());
    }

    /**
     * Cas non passant de signature XMLDSIG avec preuve avec un certificat révoqué.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigWithProofCertRevoque() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsigwithproof").file(xml)
                .param("secret", "lalalalalala").param("idSignConf", "5").param("idVerifSignConf", "1")
                .param("requestId", "Request-1").param("proofTag", "MonTAG").param("applicantId", "RPPS")
                .accept("application/json")).andExpect(status().isServiceUnavailable()).andDo(print());
    }

    /**
     * Cas non passant de signature XMLDSIG avec preuve avec un certificat avec
     * mauvais usage.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigWithProofCertBadUsage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsigwithproof").file(xml)
                .param("secret", "poopopioppopop").param("idSignConf", "6").param("idVerifSignConf", "1")
                .param("requestId", "Request-1").param("proofTag", "MonTAG").param("applicantId", "RPPS")
                .accept("application/json")).andExpect(status().isServiceUnavailable()).andDo(print());
    }

    /**
     * Cas passant signature XMLDSIG sans preuve.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestNoProof() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig").file(xml).param("secret", "123456")
                .param("idSignConf", "1").accept("application/json")).andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    /**
     * Cas non passant signature XMLDSIG sans preuve, secret incorrect.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestNoProofWrongSecret() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig").file(xml)
                .param("secret", "wrongSecret").param("idSignConf", "1").accept("application/json"))
                .andExpect(status().isUnauthorized()).andDo(print());
    }

    /**
     * Cas non passant signature XMLDSIG sans preuve, sans secret.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestNoProofNoSecret() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig").file(xml).param("idSignConf", "1").accept("application/json"))
                .andExpect(status().isUnauthorized()).andDo(print());
    }

    /**
     * Cas passant signature XMLDSIG avec preuve.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestWithProof() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.fileUpload("/signatures/xmldsigwithproof").file(xml).param("secret", "password")
                        .param("idSignConf", "1").param("idVerifSignConf", "1").param("requestId", "Request-1")
                        .param("proofTag", "MonTAG").param("applicantId", "RPPS").accept("application/json"))
                .andExpect(status().is2xxSuccessful()).andDo(print());
    }

    /**
     * Cas non passant signature XMLDSIG avec preuve, secret incorrect.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestWithProofWrongSecret() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsigwithproof").file(xml)
                .param("secret", "wrongSecret").param("idSignConf", "1").param("idVerifSignConf", "1")
                .param("requestId", "Request-1").param("proofTag", "MonTAG").param("applicantId", "RPPS")
                .accept("application/json")).andExpect(status().isUnauthorized()).andDo(print());
    }

    /**
     * Erreur 404.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestWrongId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig").file(xml).param("secret", "123456")
                .param("idSignConf", "100").accept("application/json")).andExpect(status().isNotFound()).andDo(print());
    }

    /**
     * Erreur 404.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestWrongAllIds() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.fileUpload("/signatures/xmldsigwithproof").file(xml).param("secret", "123456")
                        .param("idSignConf", "100").param("idVerifSignConf", "100").param("requestId", "Request-1")
                        .param("proofTag", "MonTAG").param("applicantId", "RPPS").accept("application/json"))
                .andExpect(status().isNotFound()).andDo(print());
    }

    /**
     * Erreur 501. Cas d'une demande de signature d'un fichier non XML enveloppée
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXMLdsigTestWrongFileFormat() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsigwithproof").file(texte)
                .param("secret", "password").param("idSignConf", "3").param("idVerifSignConf", "1")
                .param("requestId", "Request-1").param("proofTag", "MonTAG").param("applicantId", "RPPS")
                .accept("application/json")).andExpect(status().isNotImplemented()).andDo(print());
    }

    /**
     * Cas passant signature XADES sans preuve.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXadesTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xadesbaselineb").file(xml)
                .param("secret", "123456").param("idSignConf", "1").accept("application/json"))
                .andExpect(status().is2xxSuccessful()).andDo(print());
    }

    /**
     * Cas passant signature XADES avec preuve.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXadesTestWithProof() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xadesbaselinebwithproof").file(xml)
                .param("secret", "123456").param("idSignConf", "1").param("idVerifSignConf", "1")
                .param("requestId", "Request-1").param("proofTag", "MonTAG").param("applicantId", "RPPS")
                .accept("application/json")).andExpect(status().is2xxSuccessful()).andDo(print());
    }

    /**
     * Erreur 404.
     *
     * @throws Exception the exception
     */
    @Test
    public void signatureXadesTestWrongId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xadesbaselineb").file(xml)
                .param("secret", "disabled").param("idSignConf", "100").accept("application/json"))
                .andExpect(status().isNotFound()).andDo(print());
    }

}
