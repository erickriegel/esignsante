package fr.asipsante.api.sign.ws.api.delegate;

import java.io.IOException;
import java.io.InputStream;
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
import fr.asipsante.api.sign.utils.AsipSignException;
import fr.asipsante.api.sign.ws.api.ValidationApiDelegate;
import fr.asipsante.api.sign.ws.bean.Parameters;
import fr.asipsante.api.sign.ws.model.Erreur;
import fr.asipsante.api.sign.ws.model.Metadata;
import fr.asipsante.api.sign.ws.model.RapportVerif;
import fr.asipsante.api.sign.ws.model.RapportVerifWithProof;
import fr.asipsante.api.sign.ws.util.SignWsUtils;

/**
 * The Class ValidationApiDelegateImpl.
 */
@Service
public class ValidationApiDelegateImpl implements ValidationApiDelegate {

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
     * Verif signature XM ldsig with proof.
     *
     * @param idVerifSignConf
     *            the id verif sign conf
     * @param doc
     *            the doc
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
    public ResponseEntity<RapportVerifWithProof> verifSignatureXMLdsigWithProof(
            Long idVerifSignConf, MultipartFile doc, String requestId,
            String proofTag, String applicantId, Long idProofConf) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (doc == null || idVerifSignConf == null || requestId == null
                        || proofTag == null || applicantId == null
                        || idProofConf == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            SignatureValidationParameters signValidationParameters = params
                    .getSignatureValidationConfigurations()
                    .get(idVerifSignConf.toString());
            SignatureParameters signProofParams = params
                    .getProofSignatureConfigurations()
                    .get(idProofConf.toString());
            ProofParameters proofParameters = new ProofParameters("Sign",
                    requestId, proofTag, applicantId);

            if (signValidationParameters == null || signProofParams == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                String docString = null;
                try {
                    docString = new String(doc.getBytes(),
                            StandardCharsets.UTF_8);

                } catch (IOException e1) {
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }

                String proof = "";
                RapportValidationSignature rapportVerifSignAsip = null;

                RapportSignature rapportSignProofAsip = null;

                try {
                    // Validation de la signature du document
                    rapportVerifSignAsip = signatureValidationService
                            .validateXMLDsigSignature(docString,
                                    signValidationParameters,
                                    serviceCaCrl.getCacrlWrapper());
                    // Génération de la preuve
                    proof = proofGenerationService.generateSignVerifProof(
                            rapportVerifSignAsip, proofParameters,
                            serviceCaCrl.getCacrlWrapper());
                    // Signature de la preuve
                    rapportSignProofAsip = signatureService.signXMLDsig(proof,
                            signProofParams);

                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                }

                RapportVerifWithProof rapport = populateResultSignWithProof(
                        rapportVerifSignAsip.getListeErreurSignature(),
                        rapportVerifSignAsip.getMetaData(),
                        rapportVerifSignAsip.isValide(),
                        rapportSignProofAsip.getDocSigne());

                return new ResponseEntity<>(rapport, HttpStatus.OK);
            }
        } else {

            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Verif signature xades with proof.
     *
     * @param idVerifSignConf
     *            the id verif sign conf
     * @param doc
     *            the doc
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
    public ResponseEntity<RapportVerifWithProof> verifSignatureXadesWithProof(
            Long idVerifSignConf, MultipartFile doc, String requestId,
            String proofTag, String applicantId, Long idProofConf) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (doc == null || idVerifSignConf == null || requestId == null
                        || proofTag == null || applicantId == null
                        || idProofConf == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            SignatureValidationParameters signValidationParameters = params
                    .getSignatureValidationConfigurations()
                    .get(idVerifSignConf.toString());
            SignatureParameters signProofParams = params
                    .getProofSignatureConfigurations()
                    .get(idProofConf.toString());
            ProofParameters proofParameters = new ProofParameters("Sign",
                    requestId, proofTag, applicantId);

            if (signValidationParameters == null || signProofParams == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                String docString = null;
                try {
                    docString = new String(doc.getBytes(),
                            StandardCharsets.UTF_8);

                } catch (IOException e) {
                    log.error("Impossible de convertir le document reçu", e);
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }

                String proof = "";
                RapportValidationSignature rapportVerifSignAsip = null;
                RapportSignature rapportSignProofAsip = null;

                try {
                    // Validation de la signature du document
                    rapportVerifSignAsip = signatureValidationService
                            .validateXADESBaseLineBSignature(docString,
                                    signValidationParameters,
                                    serviceCaCrl.getCacrlWrapper());
                    // Génération de la preuve
                    proof = proofGenerationService.generateSignVerifProof(
                            rapportVerifSignAsip, proofParameters,
                            serviceCaCrl.getCacrlWrapper());
                    // Signature de la preuve
                    rapportSignProofAsip = signatureService
                            .signXADESBaselineB(proof, signProofParams);

                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                }

                RapportVerifWithProof rapport = populateResultSignWithProof(
                        rapportVerifSignAsip.getListeErreurSignature(),
                        rapportVerifSignAsip.getMetaData(),
                        rapportVerifSignAsip.isValide(),
                        rapportSignProofAsip.getDocSigne());

                return new ResponseEntity<>(rapport, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Verif certificat with proof.
     *
     * @param idVerifCertConf
     *            the id verif cert conf
     * @param doc
     *            the doc
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
    public ResponseEntity<RapportVerifWithProof> verifCertificatWithProof(
            Long idVerifCertConf, MultipartFile doc, String requestId,
            String proofTag, String applicantId, Long idProofConf) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (doc == null || idVerifCertConf == null || requestId == null
                        || proofTag == null || applicantId == null
                        || idProofConf == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            CertificateValidationParameters certValidationParameters = params
                    .getCertificateValidationConfigurations()
                    .get(idVerifCertConf.toString());
            SignatureParameters signProofParams = params
                    .getProofSignatureConfigurations()
                    .get(idProofConf.toString());
            ProofParameters proofParameters = new ProofParameters("VerifCert",
                    requestId, proofTag, applicantId);

            if (certValidationParameters == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                RapportValidationCertificat rapportVerifCertAsip = null;
                try {
                    if (isBinaryFile(doc)) {
                        rapportVerifCertAsip = certificateValidationService
                                .validateCertificat(doc.getBytes(),
                                        certValidationParameters,
                                        serviceCaCrl.getCacrlWrapper());
                    } else {
                        String docString = null;
                        docString = new String(doc.getBytes(),
                                StandardCharsets.UTF_8);
                        rapportVerifCertAsip = certificateValidationService
                                .validateCertificat(docString,
                                        certValidationParameters,
                                        serviceCaCrl.getCacrlWrapper());
                    }
                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                } catch (IOException e1) {
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
                String proof = "";
                RapportSignature rapportSignProofAsip = null;
                try {

                    // Génération de la preuve
                    proof = proofGenerationService.generateCertVerifProof(
                            rapportVerifCertAsip, proofParameters,
                            serviceCaCrl.getCacrlWrapper());

                    // Signature de la preuve
                    rapportSignProofAsip = signatureService
                            .signXADESBaselineB(proof, signProofParams);

                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                }
                RapportVerifWithProof rapport = populateResultVerifCertWithProof(
                        rapportVerifCertAsip.getListeErreurCertificat(),
                        rapportVerifCertAsip.getMetaData(),
                        rapportVerifCertAsip.isValide(),
                        rapportSignProofAsip.getDocSigne());

                return new ResponseEntity<>(rapport, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Verif certificat.
     *
     * @param idVerifCertConf
     *            the id verif cert conf
     * @param doc
     *            the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<RapportVerif> verifCertificat(Long idVerifCertConf,
            MultipartFile doc) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (doc == null || idVerifCertConf == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            CertificateValidationParameters certValidationParameters = params
                    .getCertificateValidationConfigurations()
                    .get(idVerifCertConf.toString());
            RapportValidationCertificat rapportVerifCertAsip = null;

            if (certValidationParameters == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                try {
                    if (isBinaryFile(doc)) {
                        rapportVerifCertAsip = certificateValidationService
                                .validateCertificat(doc.getBytes(),
                                        certValidationParameters,
                                        serviceCaCrl.getCacrlWrapper());
                    } else {
                        String docString = null;
                        docString = new String(doc.getBytes(),
                                StandardCharsets.UTF_8);
                        rapportVerifCertAsip = certificateValidationService
                                .validateCertificat(docString,
                                        certValidationParameters,
                                        serviceCaCrl.getCacrlWrapper());
                    }
                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                } catch (IOException e1) {
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }

                RapportVerif rapport = populateResultVerifCert(
                        rapportVerifCertAsip.getListeErreurCertificat(),
                        rapportVerifCertAsip.getMetaData(),
                        rapportVerifCertAsip.isValide());

                return new ResponseEntity<>(rapport, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Verif signature XM ldsig.
     *
     * @param idVerifSignConf
     *            the id verif sign conf
     * @param doc
     *            the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<RapportVerif> verifSignatureXMLdsig(
            Long idVerifSignConf, MultipartFile doc) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (doc == null || idVerifSignConf == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            SignatureValidationParameters signValidationParameters = params
                    .getSignatureValidationConfigurations()
                    .get(idVerifSignConf.toString());

            if (signValidationParameters == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                String docString = null;
                try {
                    docString = new String(doc.getBytes(),
                            StandardCharsets.UTF_8);
                } catch (IOException e1) {
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
                RapportValidationSignature rapportVerifSignAsip = null;

                try {

                    rapportVerifSignAsip = signatureValidationService
                            .validateXMLDsigSignature(docString,
                                    signValidationParameters,
                                    serviceCaCrl.getCacrlWrapper());

                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                }
                RapportVerif rapport = populateResultSign(
                        rapportVerifSignAsip.getListeErreurSignature(),
                        rapportVerifSignAsip.getMetaData(),
                        rapportVerifSignAsip.isValide());

                return new ResponseEntity<>(rapport, HttpStatus.OK);
            }
        } else {

            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * Verif signature xades.
     *
     * @param idVerifSignConf
     *            the id verif sign conf
     * @param doc
     *            the doc
     * @return the response entity
     */
    @Override
    public ResponseEntity<RapportVerif> verifSignatureXades(
            Long idVerifSignConf, MultipartFile doc) {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                if (doc == null || idVerifSignConf == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            SignatureValidationParameters signValidationParameters = params
                    .getSignatureValidationConfigurations()
                    .get(idVerifSignConf.toString());

            if (signValidationParameters == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                String docString = null;
                try {
                    docString = new String(doc.getBytes(),
                            StandardCharsets.UTF_8);
                } catch (IOException e1) {
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
                RapportValidationSignature rapportVerifSignAsip = null;

                try {

                    rapportVerifSignAsip = signatureValidationService
                            .validateXADESBaseLineBSignature(docString,
                                    signValidationParameters,
                                    serviceCaCrl.getCacrlWrapper());

                } catch (AsipSignException e) {
                    return new ResponseEntity<>(SignWsUtils.asipHttpError(e));
                }
                RapportVerif rapport = populateResultSign(
                        rapportVerifSignAsip.getListeErreurSignature(),
                        rapportVerifSignAsip.getMetaData(),
                        rapportVerifSignAsip.isValide());

                return new ResponseEntity<>(rapport, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * 
     * @param cert
     * @return true si binaire / false si texte
     * @throws IOException
     */
    /**
     * Guess whether given file is binary. Just checks for anything under 0x09.
     */
    private boolean isBinaryFile(MultipartFile doc) throws IOException {
        final  int MAX_SIZE = 1024;
        final int TEXT_SEUIL = 95;
        final int BASE = 100;
        InputStream in = doc.getInputStream();
        int size = in.available();
        if (size > MAX_SIZE)
            size = MAX_SIZE;
        byte[] data = new byte[size];
        in.read(data);
        in.close();

        int ascii = 0;
        int other = 0;

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (b < 0x09)
                return true;

            if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D)
                ascii++;
            else if (b >= 0x20 && b <= 0x7E)
                ascii++;
            else
                other++;
        }

        if (other == 0)
            return false;

        return BASE * other / (ascii + other) > TEXT_SEUIL;
    }

    /**
     * Populate result sign.
     *
     * @param erreursSignature
     *            the erreurs signature
     * @param metadata
     *            the metadata
     * @param isValide
     *            the is valide
     * @return the rapport verif
     */
    private RapportVerif populateResultSign(
            List<ErreurSignature> erreursSignature, List<MetaDatum> metadata,
            boolean isValide) {
        RapportVerif rapport = new RapportVerif();

        rapport.setValide(isValide);

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

    /**
     * Populate result sign with proof.
     *
     * @param erreursSignature
     *            the erreurs signature
     * @param metadata
     *            the metadata
     * @param isValide
     *            the is valide
     * @param preuve
     *            the preuve
     * @return the rapport verif with proof
     */
    private RapportVerifWithProof populateResultSignWithProof(
            List<ErreurSignature> erreursSignature, List<MetaDatum> metadata,
            boolean isValide, String preuve) {
        RapportVerifWithProof rapport = new RapportVerifWithProof();

        rapport.setValide(isValide);
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

    /**
     * Populate result verif cert.
     *
     * @param erreursVerifCert
     *            the erreurs verif cert
     * @param metadata
     *            the metadata
     * @param isValide
     *            the is valide
     * @return the rapport verif
     */
    private RapportVerif populateResultVerifCert(
            List<ErreurCertificat> erreursVerifCert, List<MetaDatum> metadata,
            boolean isValide) {
        RapportVerif rapport = new RapportVerif();

        rapport.setValide(isValide);

        List<Erreur> erreurs = new ArrayList<>();

        for (ErreurCertificat erreurAsip : erreursVerifCert) {
            Erreur erreur = new Erreur();
            erreur.setCodeErreur(erreurAsip.getType().getCode());
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

    /**
     * Populate result verif cert with proof.
     *
     * @param erreursVerifCert
     *            the erreurs verif cert
     * @param metadata
     *            the metadata
     * @param isValide
     *            the is valide
     * @param proof
     *            the proof
     * @return the rapport verif with proof
     */
    private RapportVerifWithProof populateResultVerifCertWithProof(
            List<ErreurCertificat> erreursVerifCert, List<MetaDatum> metadata,
            boolean isValide, String proof) {
        RapportVerifWithProof rapport = new RapportVerifWithProof();

        rapport.setValide(isValide);

        List<Erreur> erreurs = new ArrayList<>();

        for (ErreurCertificat erreurAsip : erreursVerifCert) {
            Erreur erreur = new Erreur();
            erreur.setCodeErreur(erreurAsip.getType().getCode());
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
        rapport.setPreuve(Base64.getEncoder().encodeToString(proof.getBytes()));
        return rapport;
    }

}
