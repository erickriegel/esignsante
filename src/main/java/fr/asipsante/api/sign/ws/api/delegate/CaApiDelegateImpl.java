package fr.asipsante.api.sign.ws.api.delegate;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.asipsante.api.sign.service.ICACRLService;
import fr.asipsante.api.sign.ws.api.CaApiDelegate;

/**
 * The Class CaApiDelegateImpl.
 */
@Service
public class CaApiDelegateImpl implements CaApiDelegate {

    /** The log. */
    Logger log = LoggerFactory.getLogger(CaApiDelegateImpl.class);

    /** The cacrl service. */
    @Autowired
    private ICACRLService cacrlService;

    /**
     * Gets the cacrl service.
     *
     * @return the cacrl service
     */
    public ICACRLService getCacrlService() {
        return cacrlService;
    }

    /**
     * Sets the cacrl service.
     *
     * @param cacrlService
     *            the new cacrl service
     */
    public void setCacrlService(ICACRLService cacrlService) {
        this.cacrlService = cacrlService;
    }

    /**
     * Gets the object mapper.
     *
     * @return the object mapper
     */
    public Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

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
     * Gets the ca.
     *
     * @return the ca
     */
    @Override
    public ResponseEntity<List<String>> getCA() {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                return new ResponseEntity<>(cacrlService.getCa(),
                        HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
