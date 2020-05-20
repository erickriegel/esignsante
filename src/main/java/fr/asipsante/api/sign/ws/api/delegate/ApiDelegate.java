/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.api.delegate;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The Class ApiDelegate.
 */
public class ApiDelegate {

    /**
     * Gets the request.
     *
     * @return the request
     */
    public Optional<HttpServletRequest> getRequest() {
        Optional<HttpServletRequest> request = Optional.empty();
        final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes();
        if (attrs != null) {
            request = Optional.ofNullable(attrs.getRequest());
        }
        return request;
    }

    /**
     * Gets the accept header.
     *
     * @return the accept header
     */
    public Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }
}
