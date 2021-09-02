/**
 * (c) Copyright 1998-2021, ANS. All rights reserved.
 */

package fr.asipsante.api.sign.ws.api.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.asipsante.api.sign.utils.AsipSignClientException;
import fr.asipsante.api.sign.ws.model.OpenidToken;

/**
 * The Class ApiDelegate.
 */
public class ApiDelegate {

	/**
	 * The log.
	 */
	Logger logger = LoggerFactory.getLogger(ApiDelegate.class);

	/**
	 * Gets the request.
	 *
	 * @return the request
	 */
	public Optional<NativeWebRequest> getRequest() {
		Optional<NativeWebRequest> request = Optional.empty();
		final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes();

		if (attrs != null) {
			request = Optional.of(new ServletWebRequest(attrs.getRequest()));
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

	/**
	 * Gets the OpenidToken header.
	 *
	 * @return the OpenidToken header
	 * @throws AsipSignClientException 
	 */
	public List<OpenidToken> getOpenIdTokenHeader() throws AsipSignClientException {
		ObjectMapper objectMapper = new ObjectMapper();
		Optional<String[]> arrayValues = getRequest().map(r -> r.getHeaderValues("OpenidTokens"));
		List<OpenidToken> openidTokens = new ArrayList<OpenidToken>();
		if (arrayValues.isPresent()) {

			for (String json : arrayValues.get()) {
				try {
					OpenidToken oid = objectMapper.readValue(json, OpenidToken.class);
					openidTokens.add(oid);
				} catch (JsonProcessingException e) {
					logger.error("Error lors du mapping du token", e);
					throw new AsipSignClientException("Tokens Openid non conformes.");
				}
			}
		}

		return openidTokens;
	}
}
