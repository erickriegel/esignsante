/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.delegate;

import java.io.IOException;
import java.io.InputStreamReader;
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

import fr.asipsante.api.sign.bean.errors.ErreurSignature;
import fr.asipsante.api.sign.bean.metadata.MetaDatum;
import fr.asipsante.api.sign.bean.parameters.ProofParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureParameters;
import fr.asipsante.api.sign.bean.parameters.SignatureValidationParameters;
import fr.asipsante.api.sign.bean.rapports.RapportSignature;
import fr.asipsante.api.sign.bean.rapports.RapportValidationSignature;
import fr.asipsante.api.sign.enums.MetaDataType;
import fr.asipsante.api.sign.service.ICACRLService;
import fr.asipsante.api.sign.service.IProofGenerationService;
import fr.asipsante.api.sign.service.ISignatureService;
import fr.asipsante.api.sign.service.ISignatureValidationService;
import fr.asipsante.api.sign.utils.AsipSignClientException;
import fr.asipsante.api.sign.utils.AsipSignException;
import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.ws.api.SignaturesApiDelegate;
import fr.asipsante.api.sign.ws.bean.Parameters;
import fr.asipsante.api.sign.ws.model.AsipSignatureReport;
import fr.asipsante.api.sign.ws.model.AsipSignatureReportWithProof;
import fr.asipsante.api.sign.ws.model.Erreur;
import fr.asipsante.api.sign.ws.model.Metadata;
import fr.asipsante.api.sign.ws.util.SignWsUtils;
import fr.asipsante.api.sign.ws.util.WsVars;

/**
 * The Class SignaturesApiDelegateImpl.
 */
@Service
public class SignaturesApiDelegateImpl extends ApiDelegate implements SignaturesApiDelegate {

    /** The log. */
    Logger log = LoggerFactory.getLogger(SignaturesApiDelegateImpl.class);

    /** The signature service. */
    @Autowired
    private ISignatureService signatureService;

    /** The signature validation service. */
    @Autowired
    private ISignatureValidationService signatureValidationService;

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
     * Digital signature with proof.
     *
     * @param idSignConf      the id sign conf
     * @param doc             the doc
     * @param idVerifSignConf the id verif sign conf
     * @param requestId       the request id
     * @param proofTag        the proof tag
     * @param applicantId     the applicant id
     * @param idProofConf     the id proof conf
     * @param isXades         the is xades
     * @return the response entity
     */
    private ResponseEntity<AsipSignatureReportWithProof> digitalSignatureWithProof(Long idSignConf, MultipartFile doc,
            Long idVerifSignConf, String requestId, String proofTag, String applicantId, Long idProofConf,
            boolean isXades) {

        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipSignatureReportWithProof> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (allParamsPresent(idSignConf, doc, idVerifSignConf, requestId, proofTag, applicantId, idProofConf)) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else if (doc.isEmpty()) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                final SignatureParameters signParams = params.getSignatureConfigurations().get(idSignConf.toString());
                final SignatureValidationParameters signValidationParameters = params
                        .getSignatureValidationConfigurations().get(idVerifSignConf.toString());
                final SignatureParameters signProofParams = params.getProofSignatureConfigurations()
                        .get(idProofConf.toString());
                final ProofParameters proofParameters = new ProofParameters("Sign", requestId, proofTag, applicantId);

                if (signParams == null || signValidationParameters == null || signProofParams == null) {
                    re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {

                    RapportSignature rapportSignature = null;
                    RapportValidationSignature rapportVerifSignature = null;

                    try (InputStreamReader reader = new InputStreamReader(doc.getInputStream())) {
                        final String docString = new String(doc.getBytes(), reader.getEncoding());

                        // Contrôle du certificat de signature
                        HttpStatus status = SignWsUtils.checkCertificate(
                                params.getSignatureConfigurations().get(idSignConf.toString()),
                                serviceCaCrl.getCacrlWrapper());
                        if (status != HttpStatus.CONTINUE) {
                            re = new ResponseEntity<>(status);
                        } else {
                            // Signature du document
                            if (isXades) {
                                rapportSignature = signatureService.signXADESBaselineB(docString, signParams);
                                // Validation de la signature
                                rapportVerifSignature = signatureValidationService.validateXADESBaseLineBSignature(
                                        rapportSignature.getDocSigne(), signValidationParameters,
                                        serviceCaCrl.getCacrlWrapper());

                            } else {
                                rapportSignature = signatureService.signXMLDsig(docString, signParams);
                                // Validation de la signature
                                rapportVerifSignature = signatureValidationService.validateXMLDsigSignature(
                                        rapportSignature.getDocSigne(), signValidationParameters,
                                        serviceCaCrl.getCacrlWrapper());
                            }

                            // Géneration de la preuve
                            final String proof = proofGenerationService.generateSignVerifProof(rapportVerifSignature,
                                    proofParameters, serviceCaCrl.getCacrlWrapper());

                            // Contrôle du certificat de signature de la preuve
                            status = SignWsUtils.checkCertificate(
                                    params.getSignatureConfigurations().get(idProofConf.toString()),
                                    serviceCaCrl.getCacrlWrapper());
                            if (status != HttpStatus.CONTINUE) {
                                re = new ResponseEntity<>(status);
                            } else {
                                // Signature de la preuve
                                final RapportSignature rapportSignaturePreuve = signatureService
                                        .signXADESBaselineB(proof, signProofParams);

                                final AsipSignatureReportWithProof rapport = populateResultSignWithProof(
                                        rapportVerifSignature.getListeErreurSignature(),
                                        rapportVerifSignature.getMetaData(), rapportVerifSignature.isValide(),
                                        rapportSignature.getDocSigne(), rapportSignaturePreuve.getDocSigne());
                                re = new ResponseEntity<>(rapport, HttpStatus.OK);
                            }
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
     * Checks if all params are present.
     * 
     * @param idSignConf
     * @param doc
     * @param idVerifSignConf
     * @param requestId
     * @param proofTag
     * @param applicantId
     * @param idProofConf
     * @return boolean
     */
    private boolean allParamsPresent(Long idSignConf, MultipartFile doc, Long idVerifSignConf, String requestId,
            String proofTag, String applicantId, Long idProofConf) {
        return idSignConf == null || doc == null || idVerifSignConf == null || requestId == null || proofTag == null
                || applicantId == null || idProofConf == null;
    }

    /**
     * Signature XM ldsig with proof.
     *
     * @param idSignConf      the id sign conf
     * @param doc             the doc
     * @param idVerifSignConf the id verif sign conf
     * @param requestId       the request id
     * @param proofTag        the proof tag
     * @param applicantId     the applicant id
     * @param idProofConf     the id proof conf
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipSignatureReportWithProof> signatureXMLdsigWithProof(Long idSignConf, MultipartFile doc,
            Long idVerifSignConf, String requestId, String proofTag, String applicantId, Long idProofConf) {
        return digitalSignatureWithProof(idSignConf, doc, idVerifSignConf, requestId, proofTag, applicantId,
                idProofConf, false);
    }

    /**
     * Signature xades with proof.
     *
     * @param idSignConf      the id sign conf
     * @param doc             the doc
     * @param idVerifSignConf the id verif sign conf
     * @param requestId       the request id
     * @param proofTag        the proof tag
     * @param applicantId     the applicant id
     * @param idProofConf     the id proof conf
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipSignatureReportWithProof> signatureXadesWithProof(Long idSignConf, MultipartFile doc,
            Long idVerifSignConf, String requestId, String proofTag, String applicantId, Long idProofConf) {
        return digitalSignatureWithProof(idSignConf, doc, idVerifSignConf, requestId, proofTag, applicantId,
                idProofConf, true);
    }

    /**
     * Digital signature.
     *
     * @param idSignConf the id sign conf
     * @param doc        the doc
     * @param isXades    the is xades
     * @return the response entity
     */
    private ResponseEntity<AsipSignatureReport> digitalSignature(Long idSignConf, MultipartFile doc, boolean isXades) {
        final Optional<String> acceptHeader = getAcceptHeader();
        ResponseEntity<AsipSignatureReport> re = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        if (acceptHeader.isPresent() && acceptHeader.get().contains(WsVars.HEADER_TYPE.getVar())) {
            if (idSignConf == null || doc == null) {
                re = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else if (params.getSignatureConfigurations().get(idSignConf.toString()) == null) {
                re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                try {
                    final String docString = new String(doc.getBytes(), StandardCharsets.UTF_8);

                    // Contrôle du certificat de signature
                    final HttpStatus status = SignWsUtils.checkCertificate(
                            params.getSignatureConfigurations().get(idSignConf.toString()),
                            serviceCaCrl.getCacrlWrapper());
                    if (status != HttpStatus.CONTINUE) {
                        re = new ResponseEntity<>(status);
                    } else {
                        // Signature
                        final RapportSignature rapportSignature;
                        if (isXades) {
                            rapportSignature = signatureService.signXADESBaselineB(docString,
                                    params.getSignatureConfigurations().get(idSignConf.toString()));
                        } else {
                            rapportSignature = signatureService.signXMLDsig(docString,
                                    params.getSignatureConfigurations().get(idSignConf.toString()));
                        }

                        final AsipSignatureReport rapport = populateResultSign(
                                rapportSignature.getListeErreurSignature(), rapportSignature.getDocSigne());

                        re = new ResponseEntity<>(rapport, HttpStatus.OK);
                    }
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
        return re;
    }

    /**
     * Signature XM ldsig.
     *
     * @param idSignConf the id sign conf
     * @param doc        the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipSignatureReport> signatureXMLdsig(Long idSignConf, MultipartFile doc) {
        return digitalSignature(idSignConf, doc, false);
    }

    /**
     * Signature xades.
     *
     * @param idSignConf the id sign conf
     * @param doc        the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<AsipSignatureReport> signatureXades(Long idSignConf, MultipartFile doc) {
        return digitalSignature(idSignConf, doc, true);
    }

    /**
     * Populate result sign.
     *
     * @param erreursSignature the erreurs signature
     * @param signedDocument   the signed document
     * @return the fr.asipsante.api.sign.ws.model. rapport signature
     */
    private AsipSignatureReport populateResultSign(List<ErreurSignature> erreursSignature, String signedDocument) {
        final AsipSignatureReport rapport = new AsipSignatureReport();

        rapport.setDocSigne(Base64.getEncoder().encodeToString(signedDocument.getBytes()));

        final List<Erreur> erreurs = new ArrayList<>();

        for (final ErreurSignature erreurAsip : erreursSignature) {
            final Erreur erreur = new Erreur();
            erreur.setCodeErreur(erreurAsip.getCode());
            erreur.setMessage(erreurAsip.getMessage());
            erreurs.add(erreur);
        }
        rapport.setErreurs(erreurs);

        return rapport;
    }

    /**
     * Populate result sign with proof.
     *
     * @param erreursSignature the erreurs signature
     * @param metadata         the metadata
     * @param isValide         the is valide
     * @param signedDocument   the signed document
     * @param preuve           the preuve
     * @return the rapport signature with proof
     */
    private AsipSignatureReportWithProof populateResultSignWithProof(List<ErreurSignature> erreursSignature,
            List<MetaDatum> metadata, boolean isValide, String signedDocument, String preuve) {
        final AsipSignatureReportWithProof rapport = new AsipSignatureReportWithProof();

        rapport.setValide(isValide);
        rapport.setDocSigne(Base64.getEncoder().encodeToString(signedDocument.getBytes()));
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
}
