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
 * Classe de test d'un fichier XML corrompu en signature enveloppée. Tous les
 * tests doivent retourner une erreur 501
 * 
 * @author pam
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AsipSignConfigurations.class,
        CACRLConfig.class, ScheduledConfig.class, WebConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan("fr.asipsante.api.sign.ws.api")
public class SignatureApiIntegrationTest501 {

    /** The mock mvc. */
    @Autowired
    private MockMvc mockMvc;

    /** The doc. */
    private MockMultipartFile doc;

    static {
        String confPath = Thread.currentThread().getContextClassLoader()
                .getResource("asip-sign-ws.properties").getPath();
        if (confPath != null) {
            System.setProperty("ws.conf", confPath);
        }
    }

    /**
     * Inits the.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void init() throws Exception {
        doc = new MockMultipartFile("file", Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(
                        "FichierSigne_TOMWS2_SANS_SIGNATURE_nonConforme.xml"));
        assertNotNull(doc);
    }

    /**
     * Signature XM ldsig test.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void signatureXMLdsigTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig")
                .file(doc).param("idSignConf", "1")
                .param("idVerifSignConf", "1").param("requestId", "Request-1")
                .param("proofTag", "MonTAG").param("applicantId", "RPPS")
                .param("idProofConf", "1").accept("application/json"))
                .andExpect(status().isNotImplemented()).andDo(print());
    }

    /**
     * Signature XM ldsig test no proof.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void signatureXMLdsigTestNoProof() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/signatures/xmldsig")
                .file(doc).param("idSignConf", "1").accept("application/json"))
                .andExpect(status().isNotImplemented()).andDo(print());
    }

    /**
     * Signature xades test.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void signatureXadesTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .fileUpload("/signatures/xadesbaselineb").file(doc)
                .param("idSignConf", "1").param("idVerifSignConf", "1")
                .param("requestId", "Request-1").param("proofTag", "MonTAG")
                .param("applicantId", "RPPS").param("idProofConf", "1")
                .accept("application/json"))
                .andExpect(status().isNotImplemented()).andDo(print());

    }

    /**
     * Signature xades test no proof.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void signatureXadesTestNoProof() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .fileUpload("/signatures/xadesbaselineb").file(doc)
                .param("idSignConf", "1").accept("application/json"))
                .andExpect(status().isNotImplemented()).andDo(print());
    }

}
