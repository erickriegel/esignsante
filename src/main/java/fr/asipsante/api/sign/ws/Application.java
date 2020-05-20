package fr.asipsante.api.sign.ws;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * The Class Application.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "fr.asipsante.api.sign.ws",
        "fr.asipsante.api.sign.ws.api", "fr.asipsante.api.sign.config" })
public class Application extends SpringBootServletInitializer {

    // TODO Variabiliser la taille max des fichiers
    /*
     * @Bean public MultipartResolver multipartResolver() { final
     * CommonsMultipartResolver multipartResolver = new
     * CommonsMultipartResolver();
     * multipartResolver.setMaxUploadSize(104857600); return multipartResolver;
     * }
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.boot.web.support.SpringBootServletInitializer#
     * configure(org.springframework.boot.builder.SpringApplicationBuilder)
     */
    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

}
