package fr.asipsante.api.sign.ws.api.delegate;

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

import fr.asipsante.api.sign.utils.AsipSignServerException;
import fr.asipsante.api.sign.ws.api.ConfigurationsApiDelegate;
import fr.asipsante.api.sign.ws.bean.Parameters;
import fr.asipsante.api.sign.ws.model.Conf;

/**
 * The Class ConfigurationsApiDelegateImpl.
 */
@Service
public class ConfigurationsApiDelegateImpl
        implements ConfigurationsApiDelegate {

    /** The log. */
    Logger log = LoggerFactory.getLogger(ConfigurationsApiDelegateImpl.class);

    /** The parameters. */
    @Autowired
    private Parameters parameters;

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
     * Gets the configurations.
     *
     * @return the configurations
     */
    @Override
    public ResponseEntity<Conf> getConfigurations() {
        if (getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json"))
                try {
                    return new ResponseEntity<>(parameters.getConfigs(),
                            HttpStatus.OK);
                } catch (AsipSignServerException e) {
                    return new ResponseEntity<>(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }

        } else {
            log.warn("Le header Accept:application/json est absent.");
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
