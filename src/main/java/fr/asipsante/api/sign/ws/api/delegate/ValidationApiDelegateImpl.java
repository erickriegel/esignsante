/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.delegate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.asipsante.api.sign.bean.errors.ErreurCertificat;
import fr.asipsante.api.sign.bean.errors.ErreurSignature;
import fr.asipsante.api.sign.bean.metadata.MetaDatum;
import fr.asipsante.api.sign.bean.parameters.CertificateValidationParameters;
import fr.asipsante.api.sign.bean.parameters.ProofParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureValidationParameters;
import fr.asipsante.api.sign.bean.rapports.RapportSignature;
import fr.asipsante.api.sign.bean.rapports.RapportValidationCertificat;
import fr.asipsante.api.sign.bean.rapports.RapportValidationSignature;
import fr.asipsante.api.sign.enums.MetaDataType;
import fr.asipsante.api.sign.service.ICACRLService;
import fr.asipsante.api.sign.service.ICertificateValidationService;
import fr.asipsante.api.sign.service.IProofGenerationService;
import fr.asipsante.api.sign.service.ISignatureService;
import fr.asipsante.api.sign.service.ISignatureValidationService;
import fr.asipsante.api.sign.utils.AsipSignClientException;
import fr.asipsante.api.sign.utils.AsipSignException;
import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.ws.api.ValidationApiDelegate;
import fr.asipsante.api.sign.ws.bean.Parameters;
import fr.asipsante.api.sign.ws.model.Erreur;
import fr.asipsante.api.sign.ws.model.Metadata;
import fr.asipsante.api.sign.ws.model.AsipValidationReport;
import fr.asipsante.api.sign.ws.model.AsipValidationReportWithProof;
import fr.asipsante.api.sign.ws.util.SignWsUtils;
import fr.asipsante.api.sign.ws.util.WsVars;

/**
 * The Class ValidationApiDelegateImpl.
 */
@Service
public class ValidationApiDelegateImpl extends ApiDelegate implements ValidationApiDelegate {

    /** The log. */
    Logger log = LoggerFactory.getLogger(ValidationApiDelegateImpl.class);

    /** The signature validation service. */
    @Autowired
    private ISignatureValidationService signatureValidationService;

    /** The signature service. */
    @Autowired
    private ISignatureService signatureService;

    /** The certificate validation service. */
    @Autowired
    private ICertificateValidationService certificateValidationService;

    /** The proof generation service. */
    @Autowired
    private IProofGenerationService proofGenerationService;

    /** The service ca crl. */
    @Autowired
    private ICACRLService serviceCaCrl;

    /** The params. */
    @Autowired
    private Parameters params;

    /**
     * Validate digital signature with proof.
     *
     * @param idVerifSignConf the id verif sign conf
     * @param doc             the doc
     * @param requestId       the request id
     * @param proofTag        the proof tag
     * @param applicantId     the applicant id
     * @param idProofConf     the id proof conf
     * @param isXades         the is xades
     * @return the response entity
     */
    private ResponseEntity<AsipValidationReportWithProof> validateDigitalSignatureWithProof(Long idVerifSignConf,
            MultipartFile doc, String requestId, String proofTag, String applicantId, Long idProofConf,
            boolean isXades) {
        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipValidationReportWithProof> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (doc == null || idVerifSignConf == null || requestId == null || proofTag == null || applicantId == null
                    || idProofConf == null) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                final SignatureValidationParameters signValidationParameters = params
                        .getSignatureValidationConfigurations().get(idVerifSignConf.toString());
                final SignatureParameters signProofParams = params.getProofSignatureConfigurations()
                        .get(idProofConf.toString());
                final ProofParameters proofParameters = new ProofParameters("Sign", requestId, proofTag, applicantId);

                if (signValidationParameters == null || signProofParams == null) {
                    re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    try {
                        // Validation de la signature du document
                        final RapportValidationSignature rapportVerifSignAsip = genSignVerifReport(doc, isXades,
                                signValidationParameters);

                        // Génération de la preuve
                        final String proof = proofGenerationService.generateSignVerifProof(rapportVerifSignAsip,
                                proofParameters, serviceCaCrl.getCacrlWrapper());

                        // Contrôle du certificat de signature de la preuve
                        final HttpStatus status = SignWsUtils.checkCertificate(
                                params.getSignatureConfigurations().get(idProofConf.toString()),
                                serviceCaCrl.getCacrlWrapper());
                        if (status != HttpStatus.CONTINUE) {
                            re = new ResponseEntity<>(status);
                        } else {
                            // Signature de la preuve
                            final RapportSignature rapportSignProofAsip = signatureService.signXMLDsig(proof,
                                    signProofParams);

                            final AsipValidationReportWithProof rapport = populateResultSignWithProof(
                                    rapportVerifSignAsip.getListeErreurSignature(), rapportVerifSignAsip.getMetaData(),
                                    rapportVerifSignAsip.isValide(), rapportSignProofAsip.getDocSigne());

                            re = new ResponseEntity<>(rapport, HttpStatus.OK);
                        }
                    } catch (final AsipSignClientException e1) {
                        log.error(ExceptionUtils.getFullStackTrace(e1));
                        re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
                    } catch (final AsipSignServerException e1) {
                        log.error(ExceptionUtils.getFullStackTrace(e1));
                        re = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                    } catch (IOException | AsipSignException e1) {
                        log.error(ExceptionUtils.getFullStackTrace(e1));
                        re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
        return re;
    }

    /**
     * Verif signature XM ldsig with proof.
     *
     * @param idVerifSignConf the id verif sign conf
     * @param doc             the doc
     * @param requestId       the request id
     * @param proofTag        the proof tag
     * @param applicantId     the applicant id
     * @param idProofConf     the id proof conf
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipValidationReportWithProof> verifSignatureXMLdsigWithProof(Long idVerifSignConf,
            MultipartFile doc, String requestId, String proofTag, String applicantId, Long idProofConf) {
        return validateDigitalSignatureWithProof(idVerifSignConf, doc, requestId, proofTag, applicantId, idProofConf,
                false);
    }

    /**
     * Verif signature xades with proof.
     *
     * @param idVerifSignConf the id verif sign conf
     * @param doc             the doc
     * @param requestId       the request id
     * @param proofTag        the proof tag
     * @param applicantId     the applicant id
     * @param idProofConf     the id proof conf
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipValidationReportWithProof> verifSignatureXadesWithProof(Long idVerifSignConf,
            MultipartFile doc, String requestId, String proofTag, String applicantId, Long idProofConf) {
        return validateDigitalSignatureWithProof(idVerifSignConf, doc, requestId, proofTag, applicantId, idProofConf,
                true);
    }

    /**
     * Validate digital signature.
     *
     * @param idVerifSignConf the id verif sign conf
     * @param doc             the doc
     * @param isXades         the is xades
     * @return the response entity
     */
    private ResponseEntity<AsipValidationReport> validateDigitalSignature(Long idVerifSignConf, MultipartFile doc,
            boolean isXades) {
        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipValidationReport> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (doc == null || idVerifSignConf == null) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                final SignatureValidationParameters signValidationParameters = params
                        .getSignatureValidationConfigurations().get(idVerifSignConf.toString());

                if (signValidationParameters == null) {
                    re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    try {
                        // Validation de la signature du document
                        final RapportValidationSignature rapportVerifSignAsip = genSignVerifReport(doc, isXades,
                                signValidationParameters);

                        final AsipValidationReport rapport = populateResultSign(
                                rapportVerifSignAsip.getListeErreurSignature(), rapportVerifSignAsip.getMetaData(),
                                rapportVerifSignAsip.isValide());

                        re = new ResponseEntity<>(rapport, HttpStatus.OK);
                    } catch (final AsipSignClientException e2) {
                        log.error(ExceptionUtils.getFullStackTrace(e2));
                        re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
                    } catch (final AsipSignServerException e2) {
                        log.error(ExceptionUtils.getFullStackTrace(e2));
                        re = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                    } catch (IOException | AsipSignException e2) {
                        log.error(ExceptionUtils.getFullStackTrace(e2));
                        re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
        return re;
    }

    /**
     * Verif signature XM ldsig.
     *
     * @param idVerifSignConf the id verif sign conf
     * @param doc             the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipValidationReport> verifSignatureXMLdsig(Long idVerifSignConf, MultipartFile doc) {
        return validateDigitalSignature(idVerifSignConf, doc, false);
    }

    /**
     * Verif signature xades.
     *
     * @param idVerifSignConf the id verif sign conf
     * @param doc             the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipValidationReport> verifSignatureXades(Long idVerifSignConf, MultipartFile doc) {
        return validateDigitalSignature(idVerifSignConf, doc, true);
    }

    /**
     * Generate rapport validation signature.
     *
     * @param doc
     * @param isXades
     * @param signValidationParameters
     * @return RapportValidationSignature
     * @throws IOException
     * @throws AsipSignException
     */
    private RapportValidationSignature genSignVerifReport(MultipartFile doc, boolean isXades,
            SignatureValidationParameters signValidationParameters) throws IOException, AsipSignException {

        final String docString = new String(doc.getBytes(), StandardCharsets.UTF_8);

        // Validation de la signature du document
        RapportValidationSignature rapportVerifSignAsip = null;
        if (isXades) {
            rapportVerifSignAsip = signatureValidationService.validateXADESBaseLineBSignature(docString,
                    signValidationParameters, serviceCaCrl.getCacrlWrapper());
        } else {
            rapportVerifSignAsip = signatureValidationService.validateXMLDsigSignature(docString,
                    signValidationParameters, serviceCaCrl.getCacrlWrapper());
        }

        return rapportVerifSignAsip;
    }

    /**
     * Verif certificat with proof.
     *
     * @param idVerifCertConf the id verif cert conf
     * @param doc             the doc
     * @param requestId       the request id
     * @param proofTag        the proof tag
     * @param applicantId     the applicant id
     * @param idProofConf     the id proof conf
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipValidationReportWithProof> verifCertificatWithProof(Long idVerifCertConf,
            MultipartFile doc, String requestId, String proofTag, String applicantId, Long idProofConf) {

        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipValidationReportWithProof> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (doc == null || idVerifCertConf == null || requestId == null || proofTag == null || applicantId == null
                    || idProofConf == null) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                final CertificateValidationParameters certValidationParameters = params
                        .getCertificateValidationConfigurations().get(idVerifCertConf.toString());
                final SignatureParameters signProofParams = params.getProofSignatureConfigurations()
                        .get(idProofConf.toString());
                final ProofParameters proofParameters = new ProofParameters("VerifCert", requestId, proofTag,
                        applicantId);

                if (certValidationParameters == null) {
                    re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {

                    try {
                        final RapportValidationCertificat rapportVerifCertAsip = createRapportValidationCertificat(doc,
                                certValidationParameters);

                        // Génération de la preuve
                        final String proof = proofGenerationService.generateCertVerifProof(rapportVerifCertAsip,
                                proofParameters, serviceCaCrl.getCacrlWrapper());

                        // Contrôle du certificat de signature de la preuve
                        final HttpStatus status = SignWsUtils.checkCertificate(
                                params.getSignatureConfigurations().get(idProofConf.toString()),
                                serviceCaCrl.getCacrlWrapper());
                        if (status != HttpStatus.CONTINUE) {
                            re = new ResponseEntity<>(status);
                        } else {
                            // Signature de la preuve
                            final RapportSignature rapportSignProofAsip = signatureService.signXADESBaselineB(proof,
                                    signProofParams);

                            final AsipValidationReportWithProof rapport = populateResultVerifCertWithProof(
                                    rapportVerifCertAsip.getListeErreurCertificat(), rapportVerifCertAsip.getMetaData(),
                                    rapportVerifCertAsip.isValide(), rapportSignProofAsip.getDocSigne());

                            re = new ResponseEntity<>(rapport, HttpStatus.OK);
                        }
                    } catch (final AsipSignClientException e3) {
                        log.error(ExceptionUtils.getFullStackTrace(e3));
                        re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
                    } catch (final AsipSignServerException e3) {
                        log.error(ExceptionUtils.getFullStackTrace(e3));
                        re = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                    } catch (IOException | AsipSignException e3) {
                        log.error(ExceptionUtils.getFullStackTrace(e3));
                        re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
        return re;
    }

    /**
     * Verif certificat.
     *
     * @param idVerifCertConf the id verif cert conf
     * @param doc             the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipValidationReport> verifCertificat(Long idVerifCertConf, MultipartFile doc) {
        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipValidationReport> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (doc == null || idVerifCertConf == null) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                final CertificateValidationParameters certValidationParameters = params
                        .getCertificateValidationConfigurations().get(idVerifCertConf.toString());

                if (certValidationParameters == null) {
                    re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {

                    try {
                        final RapportValidationCertificat rapportVerifCertAsip = createRapportValidationCertificat(doc,
                                certValidationParameters);

                        final AsipValidationReport rapport = populateResultVerifCert(
                                rapportVerifCertAsip.getListeErreurCertificat(), rapportVerifCertAsip.getMetaData(),
                                rapportVerifCertAsip.isValide());

                        re = new ResponseEntity<>(rapport, HttpStatus.OK);

                    } catch (final AsipSignClientException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
                    } catch (final AsipSignServerException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        re = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                    } catch (IOException | AsipSignException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
        return re;
    }

    /**
     * Generate rapport validation certificat.
     * 
     * @param doc
     * @param certValidationParameters
     * @return RapportValidationCertificat
     * @throws AsipSignException
     * @throws IOException
     */
    private RapportValidationCertificat createRapportValidationCertificat(MultipartFile doc,
            CertificateValidationParameters certValidationParameters) throws AsipSignException, IOException {
        RapportValidationCertificat rapportVerifCertAsip = null;
        if (isBinaryFile(doc)) {
            rapportVerifCertAsip = certificateValidationService.validateCertificat(doc.getBytes(),
                    certValidationParameters, serviceCaCrl.getCacrlWrapper());
        } else {
            final String docString = new String(doc.getBytes(), StandardCharsets.UTF_8);
            rapportVerifCertAsip = certificateValidationService.validateCertificat(docString, certValidationParameters,
                    serviceCaCrl.getCacrlWrapper());
        }

        return rapportVerifCertAsip;
    }

    /**
     * Checks if is binary file.
     *
     * @param doc the doc
     * @return true si binaire / false si texte
     * @throws IOException Signals that an I/O exception has occurred.
     */
    /**
     * Guess whether given file is binary. Just checks for anything under 0x09.
     */
    private boolean isBinaryFile(MultipartFile doc) throws IOException {
        boolean isBinary = false;
        final int maxSize = 1024;
        final int textThreshold = 95;
        final int base = 100;
        final InputStream in = doc.getInputStream();
        int size = in.available();
        if (size > maxSize) {
            size = maxSize;
        }
        final byte[] data = new byte[size];
        in.read(data);
        in.close();

        int ascii = 0;
        int other = 0;

        for (int i = 0; i < data.length; i++) {
            final byte b = data[i];
            if (b < 0x09) {
                isBinary = true;
                break;
            } else {
                if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D) {
                    ascii++;
                } else if (b >= 0x20 && b <= 0x7E) {
                    ascii++;
                } else {
                    other++;
                }
            }
        }

        if (!isBinary) {
            if (other == 0) {
                isBinary = false;
            } else {
                isBinary = base * other / (ascii + other) > textThreshold;
            }
        }

        return isBinary;
    }

    /**
     * Populate result sign.
     *
     * @param erreursSignature the erreurs signature
     * @param metadata         the metadata
     * @param isValide         the is valide
     * @return the rapport verif
     */
    private AsipValidationReport populateResultSign(List<ErreurSignature> erreursSignature, List<MetaDatum> metadata,
            boolean isValide) {
        final AsipValidationReport rapport = new AsipValidationReport();

        rapport.setValide(isValide);

        final List<Erreur> erreurs = new ArrayList<>();

        for (final ErreurSignature erreurAsip : erreursSignature) {
            final Erreur erreur = new Erreur();
            erreur.setCodeErreur(erreurAsip.getCode());
            erreur.setMessage(erreurAsip.getMessage());
            erreurs.add(erreur);
        }

        rapport.setErreurs(erreurs);

        final List<Metadata> metas = new ArrayList<>();
        for (final MetaDatum metadatum : metadata) {
            final Metadata meta = new Metadata();
            meta.setTypeMetadata(metadatum.getType().getName());
            if (metadatum.getType().equals(MetaDataType.RAPPORT_DIAGNOSTIQUE)
                    || metadatum.getType().equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)) {
                meta.setMessage(Base64.getEncoder().encodeToString(metadatum.getValue().getBytes()));
            } else {
                meta.setMessage(metadatum.getValue());
            }
            metas.add(meta);
        }
        rapport.setMetaData(metas);
        return rapport;
    }

    /**
     * Populate result sign with proof.
     *
     * @param erreursSignature the erreurs signature
     * @param metadata         the metadata
     * @param isValide         the is valide
     * @param preuve           the preuve
     * @return the rapport verif with proof
     */
    private AsipValidationReportWithProof populateResultSignWithProof(List<ErreurSignature> erreursSignature,
            List<MetaDatum> metadata, boolean isValide, String preuve) {
        final AsipValidationReportWithProof rapport = new AsipValidationReportWithProof();

        rapport.setValide(isValide);
        rapport.setPreuve(Base64.getEncoder().encodeToString(preuve.getBytes()));
        final List<Erreur> erreurs = new ArrayList<>();

        for (final ErreurSignature erreurAsip : erreursSignature) {
            final Erreur erreur = new Erreur();
            erreur.setCodeErreur(erreurAsip.getCode());
            erreur.setMessage(erreurAsip.getMessage());
            erreurs.add(erreur);
        }

        rapport.setErreurs(erreurs);

        final List<Metadata> metas = new ArrayList<>();
        for (final MetaDatum metadatum : metadata) {
            final Metadata meta = new Metadata();
            meta.setTypeMetadata(metadatum.getType().getName());
            if (metadatum.getType().equals(MetaDataType.RAPPORT_DIAGNOSTIQUE)
                    || metadatum.getType().equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)) {
                meta.setMessage(Base64.getEncoder().encodeToString(metadatum.getValue().getBytes()));
            } else {
                meta.setMessage(metadatum.getValue());
            }
            metas.add(meta);
        }
        rapport.setMetaData(metas);

        return rapport;
    }

    /**
     * Populate result verif cert.
     *
     * @param erreursVerifCert the erreurs verif cert
     * @param metadata         the metadata
     * @param isValide         the is valide
     * @return the rapport verif
     */
    private AsipValidationReport populateResultVerifCert(List<ErreurCertificat> erreursVerifCert,
            List<MetaDatum> metadata, boolean isValide) {
        final AsipValidationReport rapport = new AsipValidationReport();

        rapport.setValide(isValide);

        final List<Erreur> erreurs = new ArrayList<>();

        for (final ErreurCertificat erreurAsip : erreursVerifCert) {
            final Erreur erreur = new Erreur();
            erreur.setCodeErreur(erreurAsip.getType().getCode());
            erreur.setMessage(erreurAsip.getMessage());
            erreurs.add(erreur);
        }
        rapport.setErreurs(erreurs);
        final List<Metadata> metas = new ArrayList<>();

        for (final MetaDatum metadatum : metadata) {
            final Metadata meta = new Metadata();
            meta.setTypeMetadata(metadatum.getType().getName());
            if (metadatum.getType().equals(MetaDataType.RAPPORT_DIAGNOSTIQUE)
                    || metadatum.getType().equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)) {
                meta.setMessage(Base64.getEncoder().encodeToString(metadatum.getValue().getBytes()));
            } else {
                meta.setMessage(metadatum.getValue());
            }
            metas.add(meta);
        }
        rapport.setMetaData(metas);

        return rapport;

    }

    /**
     * Populate result verif cert with proof.
     *
     * @param erreursVerifCert the erreurs verif cert
     * @param metadata         the metadata
     * @param isValide         the is valide
     * @param proof            the proof
     * @return the rapport verif with proof
     */
    private AsipValidationReportWithProof populateResultVerifCertWithProof(List<ErreurCertificat> erreursVerifCert,
            List<MetaDatum> metadata, boolean isValide, String proof) {
        final AsipValidationReportWithProof rapport = new AsipValidationReportWithProof();

        rapport.setValide(isValide);

        final List<Erreur> erreurs = new ArrayList<>();

        for (final ErreurCertificat erreurAsip : erreursVerifCert) {
            final Erreur erreur = new Erreur();
            erreur.setCodeErreur(erreurAsip.getType().getCode());
            erreur.setMessage(erreurAsip.getMessage());
            erreurs.add(erreur);
        }
        rapport.setErreurs(erreurs);
        final List<Metadata> metas = new ArrayList<>();

        for (final MetaDatum metadatum : metadata) {
            final Metadata meta = new Metadata();
            meta.setTypeMetadata(metadatum.getType().getName());
            if (metadatum.getType().equals(MetaDataType.RAPPORT_DIAGNOSTIQUE)
                    || metadatum.getType().equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)) {
                meta.setMessage(Base64.getEncoder().encodeToString(metadatum.getValue().getBytes()));
            } else {
                meta.setMessage(metadatum.getValue());
            }
            metas.add(meta);
        }
        rapport.setMetaData(metas);
        rapport.setPreuve(Base64.getEncoder().encodeToString(proof.getBytes()));
        return rapport;
    }

}
