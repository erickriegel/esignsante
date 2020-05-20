/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */

package fr.asipsante.api.sign.ws.api.delegate;

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
import fr.asipsante.api.sign.service.*;
import fr.asipsante.api.sign.service.impl.utils.Version;
import fr.asipsante.api.sign.utils.AsipSignClientException;
import fr.asipsante.api.sign.utils.AsipSignException;
import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.ws.api.ValidationApiDelegate;
import fr.asipsante.api.sign.ws.bean.ConfigurationLoader;
import fr.asipsante.api.sign.ws.bean.config.IGlobalConf;
import fr.asipsante.api.sign.ws.bean.object.CertVerifConf;
import fr.asipsante.api.sign.ws.bean.object.ProofConf;
import fr.asipsante.api.sign.ws.bean.object.SignVerifConf;
import fr.asipsante.api.sign.ws.model.AsipValidationReport;
import fr.asipsante.api.sign.ws.model.AsipValidationReportWithProof;
import fr.asipsante.api.sign.ws.model.Erreur;
import fr.asipsante.api.sign.ws.model.Metadata;
import fr.asipsante.api.sign.ws.util.SignWsUtils;
import fr.asipsante.api.sign.ws.util.WsVars;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * The Class ValidationApiDelegateImpl.
 */
@Service
public class ValidationApiDelegateImpl extends ApiDelegate implements ValidationApiDelegate {

    /** Default asipsign major version. */
    private static final int MAJOR = 2;

    /** Default asipsign version. */
    private static final Version DEFAULT_VERSION = new Version(MAJOR, 0, 0, 0);

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

    /** The global configurations. */
    @Autowired
    private IGlobalConf globalConf;

    /** Asip-Sign version. */
    @Value("${asipsign.version}")
    private String version;

    /**
     * Validate digital signature with proof.
     *
     * @param idVerifSignConf the id verif sign conf
     * @param doc             the doc
     * @param proofParameters the proof parameters
     * @param idProofConf     the id proof conf
     * @param isXades         the is xades
     * @return the response entity
     */
    private ResponseEntity<AsipValidationReportWithProof> validateDigitalSignatureWithProof(final Long idVerifSignConf,
                                                                                            final MultipartFile doc, final ProofParameters proofParameters, final Long idProofConf, final boolean isXades) {
        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipValidationReportWithProof> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        final Optional<SignVerifConf> verifConf = globalConf.getSignatureVerificationById(idVerifSignConf.toString());
        final Optional<ProofConf> signProofConf = globalConf.getProofById(idProofConf.toString());
        if (!verifConf.isPresent() || !signProofConf.isPresent()){
            re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
                if (doc == null || proofParamsMissing(proofParameters)) {
                    re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                } else {

                    final SignatureValidationParameters signVerifParams = ConfigurationLoader.loadSignVerifConf(verifConf.get());
                    final SignatureParameters signProofParams = ConfigurationLoader.loadProofSignConf(signProofConf.get());

                    re = validateWithProof(doc, proofParameters, isXades, signVerifParams,
                            signProofParams);
                }
            }
        }
        return re;
    }

    /**
     * Validate with Proof.
     *
     * @param doc                      the doc
     * @param proofParameters          the proof parameters
     * @param isXades                  the is xades
     * @param signValidationParameters the sign validation parameters
     * @param signProofParams          the sign proof params
     * @return the response entity
     */
    private ResponseEntity<AsipValidationReportWithProof> validateWithProof(final MultipartFile doc,
                                                                            final ProofParameters proofParameters, final boolean isXades,
                                                                            final SignatureValidationParameters signValidationParameters, final SignatureParameters signProofParams) {
        ResponseEntity<AsipValidationReportWithProof> re;
        try {
            // Validation de la signature du document
            final RapportValidationSignature rapportVerifSignAsip = genSignVerifReport(doc, isXades,
                    signValidationParameters);

            // Génération de la preuve
            final String proof = proofGenerationService.generateSignVerifProof(rapportVerifSignAsip, proofParameters,
                    serviceCaCrl.getCacrlWrapper());

            // Contrôle du certificat de signature de la preuve
            final HttpStatus status = SignWsUtils.checkCertificate(signProofParams, serviceCaCrl.getCacrlWrapper());
            if (status != HttpStatus.CONTINUE) {
                re = new ResponseEntity<>(status);
            } else {
                // Signature de la preuve
                final RapportSignature rapportSignProofAsip = signatureService.signXMLDsig(proof, signProofParams);

                final AsipValidationReportWithProof rapport = populateResultSignWithProof(
                        rapportVerifSignAsip.getListeErreurSignature(), rapportVerifSignAsip.getMetaData(),
                        rapportVerifSignAsip.isValide(), rapportSignProofAsip.getDocSigne());

                re = new ResponseEntity<>(rapport, HttpStatus.OK);
            }
            signProofParams.getKsFile().delete();
        } catch (final AsipSignClientException e1) {
            log.error(ExceptionUtils.getFullStackTrace(e1));
            re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        } catch (final AsipSignServerException e1) {
            log.error(ExceptionUtils.getFullStackTrace(e1));
            re = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (final IOException | AsipSignException e1) {
            log.error(ExceptionUtils.getFullStackTrace(e1));
            re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return re;
    }

    /**
     * Called operation.
     *
     * @param opName the op name
     * @return the string
     */
    private String calledOperation(final String opName) {
        final Optional<NativeWebRequest> request = getRequest();
        String operation = opName;
        if (request.isPresent()) {
            operation = request.get().getContextPath() + opName;
        }
        return operation;
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
    public ResponseEntity<AsipValidationReportWithProof> verifSignatureXMLdsigWithProof(final Long idVerifSignConf,
                                                                                        final MultipartFile doc, final String requestId, final String proofTag, final String applicantId, final Long idProofConf) {
        Version wsVersion = DEFAULT_VERSION;
        try {
            wsVersion = new Version(version);
        } catch (final ParseException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        final ProofParameters proofParameters = new ProofParameters("Sign", requestId, proofTag, applicantId,
                calledOperation("/validation/signatures/xmldsigwithproof"), wsVersion);
        return validateDigitalSignatureWithProof(idVerifSignConf, doc, proofParameters, idProofConf, false);
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
    public ResponseEntity<AsipValidationReportWithProof> verifSignatureXadesWithProof(final Long idVerifSignConf,
                                                                                      final MultipartFile doc, final String requestId, final String proofTag, final String applicantId, final Long idProofConf) {
        Version wsVersion = DEFAULT_VERSION;
        try {
            wsVersion = new Version(version);
        } catch (final ParseException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        final ProofParameters proofParameters = new ProofParameters("Sign", requestId, proofTag, applicantId,
                calledOperation("/validation/signatures/xadesbaselinebwithproof"), wsVersion);
        return validateDigitalSignatureWithProof(idVerifSignConf, doc, proofParameters, idProofConf, true);
    }

    /**
     * Validate digital signature.
     *
     * @param idVerifSignConf the id verif sign conf
     * @param doc             the doc
     * @param isXades         the is xades
     * @return the response entity
     */
    private ResponseEntity<AsipValidationReport> validateDigitalSignature(final Long idVerifSignConf, final MultipartFile doc,
                                                                          final boolean isXades) {
        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipValidationReport> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        final Optional<SignVerifConf> verifConf = globalConf.getSignatureVerificationById(idVerifSignConf.toString());

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (doc == null) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else if (!verifConf.isPresent()) {
                re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {

                final SignatureValidationParameters signVerifParams = ConfigurationLoader.loadSignVerifConf(verifConf.get());

                re = validate(doc, isXades, signVerifParams);
            }
        }
        return re;
    }

    /**
     * Validate.
     *
     * @param doc                      the doc
     * @param isXades                  the is xades
     * @param signValidationParameters the sign validation parameters
     * @return the response entity
     */
    private ResponseEntity<AsipValidationReport> validate(final MultipartFile doc, final boolean isXades,
                                                          final SignatureValidationParameters signValidationParameters) {
        ResponseEntity<AsipValidationReport> re;
        try {
            // Validation de la signature du document
            final RapportValidationSignature rapportVerifSignAsip = genSignVerifReport(doc, isXades,
                    signValidationParameters);
            final AsipValidationReport rapport = populateResultSign(rapportVerifSignAsip.getListeErreurSignature(),
                    rapportVerifSignAsip.getMetaData(), rapportVerifSignAsip.isValide());

            re = new ResponseEntity<>(rapport, HttpStatus.OK);
        } catch (final AsipSignClientException e2) {
            log.error(ExceptionUtils.getFullStackTrace(e2));
            re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        } catch (final AsipSignServerException e2) {
            log.error(ExceptionUtils.getFullStackTrace(e2));
            re = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (final IOException | AsipSignException e2) {
            log.error(ExceptionUtils.getFullStackTrace(e2));
            re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<AsipValidationReport> verifSignatureXMLdsig(final Long idVerifSignConf, final MultipartFile doc) {
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
    public ResponseEntity<AsipValidationReport> verifSignatureXades(final Long idVerifSignConf, final MultipartFile doc) {
        return validateDigitalSignature(idVerifSignConf, doc, true);
    }

    /**
     * Generate rapport validation signature.
     *
     * @param doc original document
     * @param isXades Xades or D-sig
     * @param signValidationParameters signature validation parameters
     * @return RapportValidationSignature
     * @throws IOException stream file exception
     * @throws AsipSignException asipsign exception
     */
    private RapportValidationSignature genSignVerifReport(final MultipartFile doc, final boolean isXades,
                                                          final SignatureValidationParameters signValidationParameters) throws IOException, AsipSignException {

        final String docString = new String(doc.getBytes(), StandardCharsets.UTF_8);

        // Validation de la signature du document
        final RapportValidationSignature rapportVerifSignAsip;
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
    public ResponseEntity<AsipValidationReportWithProof> verifCertificatWithProof(final Long idVerifCertConf,
                                                                                  final MultipartFile doc, final String requestId, final String proofTag, final String applicantId, final Long idProofConf) {

        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipValidationReportWithProof> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        final Optional<CertVerifConf> verifConf = globalConf.getCertificateVerificationById(idVerifCertConf.toString());
        final Optional<ProofConf> signProofConf = globalConf.getProofById(idProofConf.toString());

        Version wsVersion = DEFAULT_VERSION;
        try {
            wsVersion = new Version(version);
        } catch (final ParseException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        final ProofParameters proofParameters = new ProofParameters("VerifCert", requestId, proofTag,
                applicantId, wsVersion);

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (doc == null || proofParamsMissing(proofParameters)) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else if (!verifConf.isPresent() || !signProofConf.isPresent()) {
                re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {

                final CertificateValidationParameters certVerifParams = ConfigurationLoader.loadCertVerifConf(verifConf.get());
                final SignatureParameters signProofParams = ConfigurationLoader.loadProofSignConf(signProofConf.get());

                re = validateCertWithProof(doc, certVerifParams, signProofParams,
                        proofParameters);
            }
        }
        return re;
    }

    /**
     * Checks if proof params are present.
     *
     * @param proofParameters the proof parameters
     * @return boolean
     */
    private boolean proofParamsMissing(final ProofParameters proofParameters) {
        return proofParameters.getRequestid() == null || proofParameters.getProofTag() == null
                || proofParameters.getApplicantId() == null;
    }

    /**
     * Validate cert with proof.
     *
     * @param doc                      the doc
     * @param certValidationParameters the cert validation parameters
     * @param signProofParams          the sign proof params
     * @param proofParameters          the proof parameters
     * @return the response entity
     */
    private ResponseEntity<AsipValidationReportWithProof> validateCertWithProof(final MultipartFile doc,
                                                                                final CertificateValidationParameters certValidationParameters, final SignatureParameters signProofParams,
                                                                                final ProofParameters proofParameters) {
        
        ResponseEntity<AsipValidationReportWithProof> re;
        
        try {
            final RapportValidationCertificat rapportVerifCertAsip = createRapportValidationCertificat(doc,
                    certValidationParameters);

            // Génération de la preuve
            final String proof = proofGenerationService.generateCertVerifProof(rapportVerifCertAsip, proofParameters,
                    serviceCaCrl.getCacrlWrapper());

            // Contrôle du certificat de signature de la preuve
            final HttpStatus status = SignWsUtils.checkCertificate(signProofParams, serviceCaCrl.getCacrlWrapper());
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
            signProofParams.getKsFile().delete();
        } catch (final AsipSignClientException e3) {
            log.error(ExceptionUtils.getFullStackTrace(e3));
            re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        } catch (final AsipSignServerException e3) {
            log.error(ExceptionUtils.getFullStackTrace(e3));
            re = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (final IOException | AsipSignException e3) {
            log.error(ExceptionUtils.getFullStackTrace(e3));
            re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<AsipValidationReport> verifCertificat(final Long idVerifCertConf, final MultipartFile doc) {
        
        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipValidationReport> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        final Optional<CertVerifConf> verifConf = globalConf.getCertificateVerificationById(idVerifCertConf.toString());

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (doc == null) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else if (!verifConf.isPresent()) {
                re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {

                final CertificateValidationParameters certVerifParams = ConfigurationLoader.loadCertVerifConf(verifConf.get());

                re = validateCert(doc, certVerifParams);
            }
        }
        return re;
    }

    /**
     * Validate cert.
     *
     * @param doc                      the doc
     * @param certValidationParameters the cert validation parameters
     * @return the response entity
     */
    private ResponseEntity<AsipValidationReport> validateCert(final MultipartFile doc,
                                                              final CertificateValidationParameters certValidationParameters) {
        
        ResponseEntity<AsipValidationReport> re;
        
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
        } catch (final IOException | AsipSignException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return re;
    }

    /**
     * Generate rapport validation certificat.
     * 
     * @param doc original document
     * @param certValidationParameters certificate validation paramters
     * @return RapportValidationCertificat
     * @throws AsipSignException asipsign exception
     * @throws IOException stream file exception
     */
    private RapportValidationCertificat createRapportValidationCertificat(final MultipartFile doc,
                                                                          final CertificateValidationParameters certValidationParameters) throws AsipSignException, IOException {
        
        final RapportValidationCertificat rapportVerifCertAsip;
        
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
     * Guess whether given file is binary. Just checks for anything under 0x09.
     * @param doc the doc
     * @return true si binaire / false si texte
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private boolean isBinaryFile(final MultipartFile doc) throws IOException {
        
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

        for (final byte b : data) {
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
            if (!(other == 0)) {
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
    private AsipValidationReport populateResultSign(final List<ErreurSignature> erreursSignature, final List<MetaDatum> metadata,
                                                    final boolean isValide) {
        
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
                    || metadatum.getType().equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)
                    || metadatum.getType().equals(MetaDataType.RAPPORT_DSS)) {
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
    private AsipValidationReportWithProof populateResultSignWithProof(final List<ErreurSignature> erreursSignature,
                                                                      final List<MetaDatum> metadata, final boolean isValide, final String preuve) {
        
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
                    || metadatum.getType().equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)
                    || metadatum.getType().equals(MetaDataType.RAPPORT_DSS)) {
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
    private AsipValidationReport populateResultVerifCert(final List<ErreurCertificat> erreursVerifCert,
                                                         final List<MetaDatum> metadata, final boolean isValide) {
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
                    || metadatum.getType().equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)
                    || metadatum.getType().equals(MetaDataType.RAPPORT_DSS)) {
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
    private AsipValidationReportWithProof populateResultVerifCertWithProof(final List<ErreurCertificat> erreursVerifCert,
                                                                           final List<MetaDatum> metadata, final boolean isValide, final String proof) {
        final AsipValidationReportWithProof rapport = new AsipValidationReportWithProof();

        rapport.setValide(isValide);
        rapport.setPreuve(Base64.getEncoder().encodeToString(proof.getBytes()));
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
                    || metadatum.getType().equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)
                    || metadatum.getType().equals(MetaDataType.RAPPORT_DSS)) {
                meta.setMessage(Base64.getEncoder().encodeToString(metadatum.getValue().getBytes()));
            } else {
                meta.setMessage(metadatum.getValue());
            }
            metas.add(meta);
        }
        rapport.setMetaData(metas);

        return rapport;
    }

}
