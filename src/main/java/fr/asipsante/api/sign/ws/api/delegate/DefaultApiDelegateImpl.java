package fr.asipsante.api.sign.ws.api.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import fr.asipsante.api.sign.ws.api.DefaultApiDelegate;

/**
 * The Class DefaultApiDelegateImpl.
 */
@Service
public class DefaultApiDelegateImpl implements DefaultApiDelegate {

    /** The log. */
    Logger log = LoggerFactory.getLogger(DefaultApiDelegateImpl.class);

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
     * Gets the operations.
     *
     * @return the operations
     */
    @Override
    public ResponseEntity<List<String>> getOperations() {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                List<String> methods = new ArrayList<>();
                methods.add("/");
                methods.add("/configurations");
                methods.add("/ca");
                methods.add("/signatures/xmldsig");
                methods.add("/signatures/xmldsigwithproof");
                methods.add("/signatures/xadesbaselineb");
                methods.add("/signatures/xadesbaselinebwithproof");
                methods.add("/validation/signatures/xmldsig");
                methods.add("/validation/signatures/xmldsigwithproof");
                methods.add("/validation/signatures/xadesbaselineb");
                methods.add("/validation/signatures/xadesbaselinebwithproof");
                methods.add("/validation/certificats");
                methods.add("/validation/certificatswithproof");
                return new ResponseEntity<>(methods, HttpStatus.OK);
            }
        } else {
            log.warn(
                    "ObjectMapper or HttpServletRequest not configured in default DefaultApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
