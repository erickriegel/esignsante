/**
 * (c) Copyright 1998-2020, ANS. All rights reserved.
 */

package fr.asipsante.api.sign.ws;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * The Class Application.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "fr.asipsante.api.sign.ws", "fr.asipsante.api.sign.ws.api",
        "fr.asipsante.api.sign.config" })
public class Application extends SpringBootServletInitializer {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.boot.web.support.SpringBootServletInitializer#
     * configure(org.springframework.boot.builder.SpringApplicationBuilder)
     */
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

}
