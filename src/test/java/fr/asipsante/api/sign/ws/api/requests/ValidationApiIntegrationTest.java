/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.requests;

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
 * The Class ValidationApiIntegrationTest.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AsipSignConfigurations.class, CACRLConfig.class, ScheduledConfig.class,
        WebConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan("fr.asipsante.api.sign.ws.api")
public class ValidationApiIntegrationTest {

    /** The mock mvc. */
    @Autowired
    private MockMvc mockMvc;

    /** The doc. */
    private MockMultipartFile doc;

    static {
        String confPath =
                Thread.currentThread().getContextClassLoader().getResource("asip-sign-ws.properties").getPath();
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
        doc = new MockMultipartFile("file",
                Thread.currentThread().getContextClassLoader().getResourceAsStream("TOM_FICHIER.xml"));
    }

    /**
     * Cas passant validation XMLDsig.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifSignXMLdsigTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/signatures/xmldsigwithproof").file(doc)
                .param("idVerifSignConf", "1").param("requestId", "Request-1").param("proofTag", "MonTAG")
                .param("applicantId", "RPPS").param("idProofConf", "1").accept("application/json"))
                .andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas passant validation XADES.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifSignXadesBaselineBTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/signatures/xadesbaselinebwithproof").file(doc)
                .param("idVerifSignConf", "1").param("requestId", "Request-1").param("proofTag", "MonTAG")
                .param("applicantId", "RPPS").param("idProofConf", "1").accept("application/json"))
                .andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas non passant validation XADES certificat expiré.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifSignXadesExpireTest() throws Exception {
        MockMultipartFile document = new MockMultipartFile("file",
                Thread.currentThread().getContextClassLoader().getResourceAsStream("XADESCertExpire.xml"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/signatures/xadesbaselineb").file(document)
                .param("idVerifSignConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas passant - validation certificat expiré.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifCertificatExpireTest() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("file", Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("rpps.tra.henix.asipsante.fr-sign-expire.pem"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/certificats").file(cert)
                .param("idVerifCertConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas non passant de validation de signature XMLDSIG avec preuve avec un
     * document non conforme (XML corrompu / cassé).
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifSignXMLdsigTestBadFile() throws Exception {
        doc = new MockMultipartFile("file",
                Thread.currentThread().getContextClassLoader().getResourceAsStream("TOM_FICHIER_bad.xml"));

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/signatures/xmldsigwithproof").file(doc)
                .param("idSignConf", "1").param("idVerifSignConf", "1").param("requestId", "Request-1")
                .param("proofTag", "MonTAG").param("applicantId", "RPPS").param("idProofConf", "1")
                .accept("application/json")).andExpect(status().isInternalServerError()).andDo(print());
    }

    /**
     * Cas non passant validation XMLDSIG document altéré.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifSignXmldsigDocAltereTest() throws Exception {
        MockMultipartFile document = new MockMultipartFile("file", Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("Signature_Dsig_enveloppee_document_modifie.xml"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/signatures/XMLdsig").file(document)
                .param("idVerifSignConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas non passant validation XADES mauvaise autorité.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifSignXadesBadACTest() throws Exception {
        MockMultipartFile document = new MockMultipartFile("file",
                Thread.currentThread().getContextClassLoader().getResourceAsStream("SignatureCA-PERSONNE.xml"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/signatures/xadesbaselineb").file(document)
                .param("idVerifSignConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas passant validation certificat PEM.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifCertificatTest() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("file", Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("asip-p12-EL-TEST-ORG-SIGN-20181116-141712.pem"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/certificats").file(cert)
                .param("idVerifCertConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }
    
    /**
     * Cas passant validation certificat PEM avec preuve.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifCertificatWithProofTest() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("file", Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("asip-p12-EL-TEST-ORG-SIGN-20181116-141712.pem"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/certificatswithproof").file(cert)
                .param("idSignConf", "1").param("idVerifCertConf", "1").param("requestId", "Request-1")
                .param("proofTag", "MonTAG").param("applicantId", "RPPS").param("idProofConf", "1")
                .accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas passant validation certificat PEM avec mauvaise autorité.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifCertificatBadACTest() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("file",
                Thread.currentThread().getContextClassLoader().getResourceAsStream("classe4.pem"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/certificats").file(cert)
                .param("idVerifCertConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas passant validation certificat DER.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifCertificatDERTest() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("file", Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("asip-p12-EL-TEST-ORG-SIGN-20181116-141712.der"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/certificats").file(cert)
                .param("idVerifCertConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());
    }

    /**
     * Cas non passant validation certificat DER (mauvaise autorité).
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void verifCertificatBadCADERTest() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("file",
                Thread.currentThread().getContextClassLoader().getResourceAsStream("2600301752-Auth.crt"));
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/validation/certificats").file(cert)
                .param("idVerifCertConf", "1").accept("application/json")).andExpect(status().isOk()).andDo(print());

    }
}
