package fr.asipsante.api.sign.ws.api.delegate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
import fr.asipsante.api.sign.utils.AsipSignException;
import fr.asipsante.api.sign.ws.api.SignaturesApiDelegate;
import fr.asipsante.api.sign.ws.bean.Parameters;
import fr.asipsante.api.sign.ws.model.Erreur;
import fr.asipsante.api.sign.ws.model.Metadata;
import fr.asipsante.api.sign.ws.model.RapportSignatureWithProof;
import fr.asipsante.api.sign.ws.util.SignWsUtils;

/**
 * The Class SignaturesApiDelegateImpl.
 */
@Service
public class SignaturesApiDelegateImpl implements SignaturesApiDelegate {

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
     * Gets the request.
     *
     * @return the request
     */
    public Optional<HttpServletRequest> getRequest() {
        try {
            final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes();
            if (attrs != null) {
                return Optional.ofNullable(attrs.getRequest());
            }
        } catch (final Exception e) {
            log.trace("Unable to obtain the http request", e);
        }
        return Optional.empty();
    }

    /**
     * Gets the accept header.
     *
     * @return the accept header
     */
    public Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    /**
     * Signature XM ldsig with proof.
     *
     * @param idSignConf
     *            the id sign conf
     * @param doc
     *            the doc
     * @param idVerifSignConf
     *            the id verif sign conf
     * @param requestId
     *            the request id
     * @param proofTag
     *            the proof tag
     * @param applicantId
     *            the applicant id
     * @param idProofConf
     *            the id proof conf
     * @return the response entity
     */
    @Override
    public ResponseEntity<RapportSignatureWithProof> signatureXMLdsigWithProof(
            Long idSignConf, MultipartFile doc, Long idVerifSignConf,
            String requestId, String proofTag, String applicantId,
            Long idProofConf) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (idSignConf == null || doc == null || idVerifSignConf == null
                        || requestId == null || proofTag == null
                        || applicantId == null || idProofConf == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                } else {
                    if (doc.isEmpty()) {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }
            }

            SignatureParameters signParams = params.getSignatureConfigurations()
                    .get(idSignConf.toString());
            SignatureValidationParameters signValidationParameters = params
                    .getSignatureValidationConfigurations()
                    .get(idVerifSignConf.toString());
            SignatureParameters signProofParams = params
                    .getProofSignatureConfigurations()
                    .get(idProofConf.toString());
            ProofParameters proofParameters = new ProofParameters("Sign",
                    requestId, proofTag, applicantId);

            if (signParams == null || signValidationParameters == null
                    || signProofParams == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {

                RapportSignature rapportSignature = null;
                RapportSignature rapportSignaturePreuve = null;
                RapportValidationSignature rapportVerifSignature = null;

                String docString = null;

                try {
                    InputStreamReader reader = new InputStreamReader(
                            doc.getInputStream());
                    docString = new String(doc.getBytes(),
                            reader.getEncoding());
                } catch (IOException e1) {
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }

                String proof = "";

                try {
                    HttpStatus status;
                    // Contrôle du certificat de signature
                    status = SignWsUtils.checkCertificate(
                            params.getSignatureConfigurations()
                                    .get(idSignConf.toString()),
                            serviceCaCrl.getCacrlWrapper());
                    if (status != HttpStatus.CONTINUE) {
                        return new ResponseEntity<>(status);
                    }

                    // Signature du document
                    rapportSignature = signatureService.signXMLDsig(docString,
                            signParams);
                    // Validation de la signature
                    rapportVerifSignature = signatureValidationService
                            .validateXMLDsigSignature(
                                    rapportSignature.getDocSigne(),
                                    signValidationParameters,
                                    serviceCaCrl.getCacrlWrapper());
                    // Géneration de la preuve
                    proof = proofGenerationService.generateSignVerifProof(
                            rapportVerifSignature, proofParameters,
                            serviceCaCrl.getCacrlWrapper());

                    // Contrôle du certificat de signature de la preuve
                    status = SignWsUtils.checkCertificate(
                            params.getSignatureConfigurations()
                                    .get(idProofConf.toString()),
                            serviceCaCrl.getCacrlWrapper());
                    if (status != HttpStatus.CONTINUE) {
                        return new ResponseEntity<>(status);
                    }

                    // Signature de la preuve
                    rapportSignaturePreuve = signatureService
                            .signXADESBaselineB(proof, signProofParams);
                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                }
                RapportSignatureWithProof rapport = populateResultSignWithProof(
                        rapportVerifSignature.getListeErreurSignature(),
                        rapportVerifSignature.getMetaData(),
                        rapportVerifSignature.isValide(),
                        rapportSignature.getDocSigne(),
                        rapportSignaturePreuve.getDocSigne());
                return new ResponseEntity<>(rapport, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Signature xades with proof.
     *
     * @param idSignConf
     *            the id sign conf
     * @param doc
     *            the doc
     * @param idVerifSignConf
     *            the id verif sign conf
     * @param requestId
     *            the request id
     * @param proofTag
     *            the proof tag
     * @param applicantId
     *            the applicant id
     * @param idProofConf
     *            the id proof conf
     * @return the response entity
     */
    @Override
    public ResponseEntity<RapportSignatureWithProof> signatureXadesWithProof(
            Long idSignConf, MultipartFile doc, Long idVerifSignConf,
            String requestId, String proofTag, String applicantId,
            Long idProofConf) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (idSignConf == null || doc == null || idVerifSignConf == null
                        || requestId == null || proofTag == null
                        || applicantId == null || idProofConf == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            SignatureParameters signParams = params.getSignatureConfigurations()
                    .get(idSignConf.toString());
            SignatureValidationParameters signValidationParameters = params
                    .getSignatureValidationConfigurations()
                    .get(idVerifSignConf.toString());
            SignatureParameters signProofParams = params
                    .getProofSignatureConfigurations()
                    .get(idProofConf.toString());
            ProofParameters proofParameters = new ProofParameters("Sign",
                    requestId, proofTag, applicantId);

            if (signParams == null || signValidationParameters == null
                    || signProofParams == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {

                RapportSignature rapportSignAsip = null;
                RapportSignature rapportSignProofAsip = null;
                RapportValidationSignature rapportVerifSignAsip = null;

                String docString = null;
                try {
                    docString = new String(doc.getBytes(),
                            StandardCharsets.UTF_8);
                } catch (IOException e1) {
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }

                String proof = "";

                try {
                    HttpStatus status;
                    // Contrôle du certificat de signature
                    status = SignWsUtils.checkCertificate(
                            params.getSignatureConfigurations()
                                    .get(idSignConf.toString()),
                            serviceCaCrl.getCacrlWrapper());
                    if (status != HttpStatus.CONTINUE) {
                        return new ResponseEntity<>(status);
                    }

                    // Signature du document
                    rapportSignAsip = signatureService
                            .signXADESBaselineB(docString, signParams);
                    // Validation de la signature
                    rapportVerifSignAsip = signatureValidationService
                            .validateXADESBaseLineBSignature(
                                    rapportSignAsip.getDocSigne(),
                                    signValidationParameters,
                                    serviceCaCrl.getCacrlWrapper());
                    // Génération de la preuve
                    proof = proofGenerationService.generateSignVerifProof(
                            rapportVerifSignAsip, proofParameters,
                            serviceCaCrl.getCacrlWrapper());

                    // Contrôle du certificat de signature de la preuve
                    status = SignWsUtils.checkCertificate(
                            params.getSignatureConfigurations()
                                    .get(idProofConf.toString()),
                            serviceCaCrl.getCacrlWrapper());
                    if (status != HttpStatus.CONTINUE) {
                        return new ResponseEntity<>(status);
                    }

                    // Signature de la preuve
                    rapportSignProofAsip = signatureService
                            .signXADESBaselineB(proof, signProofParams);
                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                }
                RapportSignatureWithProof rapport = populateResultSignWithProof(
                        rapportVerifSignAsip.getListeErreurSignature(),
                        rapportVerifSignAsip.getMetaData(),
                        rapportVerifSignAsip.isValide(),
                        rapportSignAsip.getDocSigne(),
                        rapportSignProofAsip.getDocSigne());
                return new ResponseEntity<>(rapport, HttpStatus.OK);
            }
        } else {

            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Signature XM ldsig.
     *
     * @param idSignConf
     *            the id sign conf
     * @param doc
     *            the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<fr.asipsante.api.sign.ws.model.RapportSignature> signatureXMLdsig(
            Long idSignConf, MultipartFile doc) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (idSignConf == null || doc == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            if (params.getSignatureConfigurations()
                    .get(idSignConf.toString()) == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            String docString;
            try {
                docString = new String(doc.getBytes(), StandardCharsets.UTF_8);
            } catch (IOException e1) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            RapportSignature rapportAsip = null;

            HttpStatus status;
            try {
                // Contrôle du certificat de signature
                status = SignWsUtils.checkCertificate(
                        params.getSignatureConfigurations()
                                .get(idSignConf.toString()),
                        serviceCaCrl.getCacrlWrapper());
                if (status != HttpStatus.CONTINUE) {
                    return new ResponseEntity<>(status);
                }

                // Signature
                rapportAsip = signatureService.signXMLDsig(docString,
                        params.getSignatureConfigurations()
                                .get(idSignConf.toString()));
            } catch (AsipSignException e) {
                return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
            }

            fr.asipsante.api.sign.ws.model.RapportSignature rapport = populateResultSign(
                    rapportAsip.getListeErreurSignature(),
                    rapportAsip.getDocSigne());

            return new ResponseEntity<>(rapport, HttpStatus.OK);
        } else {

            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Signature xades.
     *
     * @param idSignConf
     *            the id sign conf
     * @param doc
     *            the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<fr.asipsante.api.sign.ws.model.RapportSignature> signatureXades(
            Long idSignConf, MultipartFile doc) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (idSignConf == null || doc == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            if (params.getSignatureConfigurations()
                    .get(idSignConf.toString()) == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            }

            String docString = null;
            try {
                docString = new String(doc.getBytes(), StandardCharsets.UTF_8);
            } catch (IOException e1) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            RapportSignature rapportAsip = null;

            HttpStatus status;
            try {
                // Contrôle du certificat de signature
                status = SignWsUtils.checkCertificate(
                        params.getSignatureConfigurations()
                                .get(idSignConf.toString()),
                        serviceCaCrl.getCacrlWrapper());
                if (status != HttpStatus.CONTINUE) {
                    return new ResponseEntity<>(status);
                }

                // Signature
                rapportAsip = signatureService.signXADESBaselineB(docString,
                        params.getSignatureConfigurations()
                                .get(idSignConf.toString()));
            } catch (AsipSignException e) {
                return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
            }
            fr.asipsante.api.sign.ws.model.RapportSignature rapport = populateResultSign(
                    rapportAsip.getListeErreurSignature(),
                    rapportAsip.getDocSigne());

            return new ResponseEntity<>(rapport, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Populate result sign.
     *
     * @param erreursSignature
     *            the erreurs signature
     * @param signedDocument
     *            the signed document
     * @return the fr.asipsante.api.sign.ws.model. rapport signature
     */
    private fr.asipsante.api.sign.ws.model.RapportSignature populateResultSign(
            List<ErreurSignature> erreursSignature, String signedDocument) {
        fr.asipsante.api.sign.ws.model.RapportSignature rapport = new fr.asipsante.api.sign.ws.model.RapportSignature();

        rapport.setDocSigne(
                Base64.getEncoder().encodeToString(signedDocument.getBytes()));

        List<Erreur> erreurs = new ArrayList<>();

        for (ErreurSignature erreurAsip : erreursSignature) {
            Erreur erreur = new Erreur();
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
     * @param erreursSignature
     *            the erreurs signature
     * @param metadata
     *            the metadata
     * @param isValide
     *            the is valide
     * @param signedDocument
     *            the signed document
     * @param preuve
     *            the preuve
     * @return the rapport signature with proof
     */
    private RapportSignatureWithProof populateResultSignWithProof(
            List<ErreurSignature> erreursSignature, List<MetaDatum> metadata,
            boolean isValide, String signedDocument, String preuve) {
        RapportSignatureWithProof rapport = new RapportSignatureWithProof();

        rapport.setValide(isValide);
        rapport.setDocSigne(
                Base64.getEncoder().encodeToString(signedDocument.getBytes()));
        rapport.setPreuve(
                Base64.getEncoder().encodeToString(preuve.getBytes()));

        List<Erreur> erreurs = new ArrayList<>();

        for (ErreurSignature erreurAsip : erreursSignature) {
            Erreur erreur = new Erreur();
            erreur.setCodeErreur(erreurAsip.getCode());
            erreur.setMessage(erreurAsip.getMessage());
            erreurs.add(erreur);
        }
        rapport.setErreurs(erreurs);

        List<Metadata> metas = new ArrayList<>();

        for (MetaDatum metadatum : metadata) {
            Metadata meta = new Metadata();
            meta.setTypeMetadata(metadatum.getType().getName());
            if (metadatum.getType().equals(MetaDataType.RAPPORT_DIAGNOSTIQUE)
                    || metadatum.getType()
                            .equals(MetaDataType.DOCUMENT_ORIGINAL_NON_SIGNE)) {
                meta.setMessage(Base64.getEncoder()
                        .encodeToString(metadatum.getValue().getBytes()));
            } else {
                meta.setMessage(metadatum.getValue());
            }
            metas.add(meta);
        }
        rapport.setMetaData(metas);

        return rapport;
    }
}
