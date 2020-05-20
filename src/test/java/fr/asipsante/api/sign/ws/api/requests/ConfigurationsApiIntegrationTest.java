/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */

package fr.asipsante.api.sign.ws.api.requests;

import fr.asipsante.api.sign.config.CACRLConfig;
import fr.asipsante.api.sign.config.ScheduledConfig;
import fr.asipsante.api.sign.config.WebConfig;
import fr.asipsante.api.sign.config.provider.impl.ESignSanteSanteConfigurationsJson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The Class ConfigurationsApiIntegrationTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ESignSanteSanteConfigurationsJson.class, CACRLConfig.class, ScheduledConfig.class,
        WebConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan("fr.asipsante.api.sign")
public class ConfigurationsApiIntegrationTest {

    /** The mock mvc. */
    @Autowired
    private MockMvc mockMvc;

    static {
        final String confPath;
        try {
            confPath = String.valueOf(Paths.get(Paths.get(Objects.requireNonNull(Thread.currentThread().
                    getContextClassLoader().getResource("esignsante-conf.json")).toURI()).toString()));
            System.setProperty("ws.conf", confPath);
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configurations get test.
     *
     * @throws Exception the exception
     */
    @Test
    public void configurationsGetTest() throws Exception {

        final String expectedBody = "{\"signature\":[{\"idSignConf\":1,\"associatedProofId\":1,\"description\":\"Scheduling Test.\",\"signaturePackaging\":\"ENVELOPING\",\"digestAlgorithm\":\"SHA512\",\"canonicalisationAlgorithm\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=testsign.test.asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"},{\"idSignConf\":2,\"associatedProofId\":2,\"description\":\"Fichier de configuration de Ioanna pour les signatures de documents.\",\"signaturePackaging\":\"ENVELOPING\",\"digestAlgorithm\":\"SHA512\",\"canonicalisationAlgorithm\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=asipsign.flux.henix.asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"},{\"idSignConf\":3,\"associatedProofId\":5,\"description\":\"Fichier de configuration de Ioanna pour les signatures de documents.\",\"signaturePackaging\":\"ENVELOPED\",\"digestAlgorithm\":\"SHA512\",\"canonicalisationAlgorithm\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=asipsign.flux.henix.asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"},{\"idSignConf\":5,\"associatedProofId\":6,\"description\":\"Scheduling Test.\",\"signaturePackaging\":\"ENVELOPING\",\"digestAlgorithm\":\"SHA512\",\"canonicalisationAlgorithm\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=rpps.tra.henix\\\\;asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"},{\"idSignConf\":6,\"associatedProofId\":7,\"description\":\"Scheduling Test.\",\"signaturePackaging\":\"ENVELOPING\",\"digestAlgorithm\":\"SHA512\",\"canonicalisationAlgorithm\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=tomwsrevoque.henix.asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"}],\"signatureVerification\":[{\"idVerifSignConf\":1,\"description\":\"\",\"rules\":[{\"id\":\"TrustedCertificat\",\"description\":\"L'autoritÃ© de Certification est reconnue.\"},{\"id\":\"FormatSignature\",\"description\":\"Le format de signature est correct (Xades Baseline B, XMlDsig-core-1).\"},{\"id\":\"SignatureCertificatValide\",\"description\":\"La signature du certificat est valide.\"},{\"id\":\"ExistenceBaliseSigningTime\",\"description\":\"La balise 'SigningTime' existe dans la signature.\"},{\"id\":\"ExistenceDuCertificatDeSignature\",\"description\":\"Le certificat utilisÃ© pour la signature existe dans la signature.\"},{\"id\":\"ExpirationCertificat\",\"description\":\"Le certificat n'est pas expirÃ©.\"},{\"id\":\"NonRepudiation\",\"description\":\"L'usage de la clÃ© du certificat correspond Ã  un usage de signature Ã©lectronique et de non rÃ©pudiation.\"},{\"id\":\"RevocationCertificat\",\"description\":\"Le certificat n'est pas rÃ©voquÃ©.\"},{\"id\":\"SignatureNonVide\",\"description\":\"La signature existe et n'est pas vide.\"},{\"id\":\"SignatureIntacte\",\"description\":\"La signature est intacte.\"},{\"id\":\"DocumentIntact\",\"description\":\"Le document est intact.\"}],\"metaData\":[\"DATE_SIGNATURE\",\"DN_CERTIFICAT\",\"RAPPORT_DIAGNOSTIQUE\",\"DOCUMENT_ORIGINAL_NON_SIGNE\",\"RAPPORT_DSS\"]},{\"idVerifSignConf\":2,\"description\":\"\",\"rules\":[{\"id\":\"TrustedCertificat\",\"description\":\"L'autoritÃ© de Certification est reconnue.\"},{\"id\":\"SignatureCertificatValide\",\"description\":\"La signature du certificat est valide.\"},{\"id\":\"ExistenceBaliseSigningTime\",\"description\":\"La balise 'SigningTime' existe dans la signature.\"},{\"id\":\"ExistenceDuCertificatDeSignature\",\"description\":\"Le certificat utilisÃ© pour la signature existe dans la signature.\"},{\"id\":\"ExpirationCertificat\",\"description\":\"Le certificat n'est pas expirÃ©.\"},{\"id\":\"NonRepudiation\",\"description\":\"L'usage de la clÃ© du certificat correspond Ã  un usage de signature Ã©lectronique et de non rÃ©pudiation.\"},{\"id\":\"RevocationCertificat\",\"description\":\"Le certificat n'est pas rÃ©voquÃ©.\"},{\"id\":\"SignatureNonVide\",\"description\":\"La signature existe et n'est pas vide.\"},{\"id\":\"SignatureIntacte\",\"description\":\"La signature est intacte.\"},{\"id\":\"DocumentIntact\",\"description\":\"Le document est intact.\"}],\"metaData\":[\"\"]}],\"certificatVerification\":[{\"idVerifCertConf\":1,\"description\":\"\",\"rules\":[{\"id\":\"ExpirationCertificat\",\"description\":\"Le certificat n'est pas expirÃ©.\"},{\"id\":\"RevocationCertificat\",\"description\":\"Le certificat n'est pas rÃ©voquÃ©.\"},{\"id\":\"SignatureCertificatValide\",\"description\":\"La signature du certificat est valide.\"},{\"id\":\"TrustedCertificat\",\"description\":\"L'autoritÃ© de Certification est reconnue.\"},{\"id\":\"NonRepudiation\",\"description\":\"L'usage de la clÃ© du certificat correspond Ã  un usage de signature Ã©lectronique et de non rÃ©pudiation.\"}],\"metaData\":[\"DN_CERTIFICAT\",\"RAPPORT_DIAGNOSTIQUE\",\"RAPPORT_DSS\"]},{\"idVerifCertConf\":2,\"description\":\"\",\"rules\":[{\"id\":\"ExpirationCertificat\",\"description\":\"Le certificat n'est pas expirÃ©.\"},{\"id\":\"RevocationCertificat\",\"description\":\"Le certificat n'est pas rÃ©voquÃ©.\"},{\"id\":\"SignatureCertificatValide\",\"description\":\"La signature du certificat est valide.\"},{\"id\":\"TrustedCertificat\",\"description\":\"L'autoritÃ© de Certification est reconnue.\"},{\"id\":\"NonRepudiation\",\"description\":\"L'usage de la clÃ© du certificat correspond Ã  un usage de signature Ã©lectronique et de non rÃ©pudiation.\"}],\"metaData\":[\"\"]}],\"proof\":[{\"idProofConf\":1,\"description\":\"Fichier par defaut pour la preuve\",\"signaturePackagingForProof\":\"ENVELOPED\",\"digestAlgorithmForProof\":\"SHA256\",\"canonicalisationAlgorithmForProof\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=testsign.test.asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"},{\"idProofConf\":2,\"description\":\"Configuration de Ioanna pour la preuve\",\"signaturePackagingForProof\":\"ENVELOPED\",\"digestAlgorithmForProof\":\"SHA256\",\"canonicalisationAlgorithmForProof\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=asipsign.preuve.henix.asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"},{\"idProofConf\":5,\"description\":\"Fichier par defaut pour la preuve\",\"signaturePackagingForProof\":\"ENVELOPED\",\"digestAlgorithmForProof\":\"SHA256\",\"canonicalisationAlgorithmForProof\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=rpps.tra.henix\\\\;asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"},{\"idProofConf\":6,\"description\":\"Fichier par defaut pour la preuve\",\"signaturePackagingForProof\":\"ENVELOPED\",\"digestAlgorithmForProof\":\"SHA256\",\"canonicalisationAlgorithmForProof\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=tomwsrevoque.henix.asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"},{\"idProofConf\":7,\"description\":\"Fichier par defaut pour la preuve\",\"signaturePackagingForProof\":\"ENVELOPED\",\"digestAlgorithmForProof\":\"SHA256\",\"canonicalisationAlgorithmForProof\":\"http://www.w3.org/2001/10/xml-exc-c14n#\",\"dn\":\"CN=mock.platines.henix.asipsante.fr,OU=318751275100020,O=ASIP-SANTE,ST=Paris (75),C=FR\"}]}";

        final MvcResult result = mockMvc.perform(get("/configurations").accept("application/json")).andExpect(status().isOk())
                .andDo(print()).andReturn();

        assertEquals(expectedBody, result.getResponse().getContentAsString());
    }
}
