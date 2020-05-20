/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */

package fr.asipsante.api.sign.ws.util;

import fr.asipsante.api.sign.bean.cacrl.CACRLWrapper;
import fr.asipsante.api.sign.bean.parameters.CertificateValidationParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureParameters;
import fr.asipsante.api.sign.bean.rapports.RapportValidationCertificat;
import fr.asipsante.api.sign.service.ICertificateValidationService;
import fr.asipsante.api.sign.service.impl.CertificateValidationServiceImpl;
import fr.asipsante.api.sign.utils.AsipSignClientException;
import fr.asipsante.api.sign.utils.AsipSignException;
import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.validation.certificat.rules.ICertificatVisitor;
import fr.asipsante.api.sign.validation.certificat.rules.impl.ExpirationCertificat;
import fr.asipsante.api.sign.validation.certificat.rules.impl.NonRepudiation;
import fr.asipsante.api.sign.validation.certificat.rules.impl.RevocationCertificat;
import fr.asipsante.api.sign.validation.certificat.rules.impl.TrustedCertificat;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * The Class SignWsUtils.
 */
public class SignWsUtils {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SignWsUtils.class);

    /**
     * Instantiates a new sign ws utils.
     */
    private SignWsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * ANS http error.
     *
     * @param e the e
     * @return the http status
     */
    public static HttpStatus asipHttpError(final AsipSignException e) {
        LOG.error(ExceptionUtils.getStackTrace(e));
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e instanceof AsipSignClientException) {
            status = HttpStatus.NOT_IMPLEMENTED;
        } else if (e instanceof AsipSignServerException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return status;
    }

    /**
     * Retourne la liste des certificats contenus dans un KeyStore.
     *
     * @param pkcs12File the pkcs 12 file
     * @param password   mot de passe du keyStore
     * @return la liste des certificats contenus dans un KeyStore
     * @throws GeneralSecurityException the general security exception
     * @throws IOException              Signals that an I/O exception has occurred.
     */
    private static List<X509Certificate> getSignatureCertificates(final File pkcs12File, final String password)
            throws GeneralSecurityException, IOException {

        final KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(pkcs12File), password.toCharArray());
        final List<X509Certificate> list = new ArrayList<>();

        final Enumeration<String> aliases = ks.aliases();

        while (aliases.hasMoreElements()) {
            final String alias = aliases.nextElement();
            final X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            list.add(cert);

        }
        return list;
    }

    /**
     * Contrôle de la validité des certificats de signature.
     *
     * @param signParams   the sign params
     * @param caCrlWrapper the ca crl wrapper
     * @return the http status
     * @throws AsipSignException the asip sign exception
     */
    public static HttpStatus checkCertificate(

            final SignatureParameters signParams, final CACRLWrapper caCrlWrapper) throws AsipSignException {
        // On contrôle le certificat qui va signer
        final List<X509Certificate> certificateList;
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        try {
            if (signParams != null) {
                certificateList = getSignatureCertificates(signParams.getKsFile(), signParams.getPassword());

                final ICertificateValidationService certValidationService = new CertificateValidationServiceImpl();
                final List<ICertificatVisitor> certRules = new ArrayList<>();
                certRules.add(new ExpirationCertificat());
                certRules.add(new NonRepudiation());
                certRules.add(new RevocationCertificat());
                certRules.add(new TrustedCertificat());
                final CertificateValidationParameters certParams = new CertificateValidationParameters();
                certParams.setRules(certRules);

                // On boucle sur tous les certificats du Keystore
                // et on lance les règles de validation
                boolean isValide = true;
                for (final X509Certificate cert : certificateList) {
                    final RapportValidationCertificat rapportValidationCert = certValidationService
                            .validateCertificat(cert.getEncoded(), certParams, caCrlWrapper);
                    if (!rapportValidationCert.isValide()) {
                        final String error = rapportValidationCert.getListeErreurCertificat().toString();
                        LOG.error(error);
                        isValide = false;
                    }
                }
                if (isValide) {
                    status = HttpStatus.CONTINUE;
                }
            }
        } catch (final GeneralSecurityException | IOException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        }
        return status;
    }
}
