package fr.asipsante.api.sign.config.utils;

import fr.asipsante.api.sign.config.CACRLConfig;
import fr.asipsante.api.sign.service.ICACRLService;
import fr.asipsante.api.sign.service.impl.CACRLServiceImpl;
import fr.asipsante.api.sign.ws.bean.object.CaConf;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class CaCrlServiceLoader
 */
public class CaCrlServiceLoader {

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(CaCrlServiceLoader.class);

    public static ICACRLService loadCaCrl(ICACRLService serviceCaCrl, List<CaConf> listCaConf) throws IOException {
        List<String> certList = listCaConf.stream().map(CaConf::getCertificate).collect(Collectors.toList());
        loadCa(serviceCaCrl, certList);

        List<String> crlList = listCaConf.stream().map(CaConf::getCrl).collect(Collectors.toList());
        loadCrl(serviceCaCrl, crlList);
        return serviceCaCrl;
    }

    private static void loadCrl(ICACRLService serviceCaCrl, List<String> crlList) throws IOException {
        File crlFile = File.createTempFile("ca-bundle", ".crl");
        CRLLoader crlLoader = new CRLLoader(crlList);
        try {
            crlLoader.buildCRLBundle(crlFile);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        log.info("Chargement du bundle des CRL, chemin : {}", crlFile.getAbsolutePath());
        serviceCaCrl.loadCRL(crlFile);
        crlFile.delete();
    }

    private static void loadCa(ICACRLService serviceCaCrl, List<String> certList) throws IOException {
        File caFile = File.createTempFile("ca-bundle", ".crt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caFile))) {
            for (String cert : certList) {
                writer.write(cert.replaceAll("(?<=\n)[ +]",""));
                writer.newLine();
            }
            log.info("Chargement du bundle des AC, chemin : {}", caFile.getAbsolutePath());
        } catch (IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        serviceCaCrl.loadCA(caFile);
        caFile.delete();
    }

}
