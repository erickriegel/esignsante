/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.config;

import com.google.common.base.Predicates;
import fr.asipsante.api.sign.service.impl.utils.Version;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;

import javax.servlet.ServletContext;
import java.text.ParseException;

/**
 * The Class OpenAPIDocumentationConfig.
 */
@Configuration
public class OpenAPIDocumentationConfig {

    /** Default asipsign major version. */
    private static final int MAJOR = 2;

    /** The log. */
    Logger log = LoggerFactory.getLogger(OpenAPIDocumentationConfig.class);

    /** Asip-Sign version. */
    @Value("${asipsign.version}")
    private String version;

    /**
     * Api info.
     *
     * @return the api info
     */
    ApiInfo apiInfo() {
        Version wsVersion = new Version(MAJOR, 0, 0, 0);
        try {
            wsVersion = new Version(version); // assign the current version of asip-sign-webservices
        } catch (ParseException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return new ApiInfoBuilder().title("ASIP-Sign").description(
                "API du composant ASIP-Sign.  Ce composant dit de \"signature\" mutualise et homogénéise " +
                        "la mise en oeuvre des besoins autour de la signature.  Il permet aux partenaires " +
                        "de l'ASIP Santé de signer leurs documents ainsi que de vérifier la validité " +
                        "d'une signature ou d'un certificat.     ")
                .license("").licenseUrl("http://unlicense.org").termsOfServiceUrl("").version(wsVersion.getVersion())
                .contact(new Contact("", "", "asip-sign@asipsante.fr")).build();
    }

    /**
     * Custom implementation.
     *
     * @param basePath       the base path
     * @return the docket
     */
    @Bean
    public Docket customImplementation(@Value("${openapi.aSIPSign.base-path:/}") String basePath) {
        return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false).select()
                .apis(RequestHandlerSelectors.basePackage("fr.asipsante.api.sign.ws.api"))
                .paths(Predicates.not(PathSelectors.regex("/error.*"))).build()
                .directModelSubstitute(org.threeten.bp.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(org.threeten.bp.OffsetDateTime.class, java.util.Date.class).apiInfo(apiInfo());
    }

    /**
     * The Class BasePathAwareRelativePathProvider.
     */
    class BasePathAwareRelativePathProvider extends RelativePathProvider {

        /** The base path. */
        private String basePath;

        /**
         * Instantiates a new base path aware relative path provider.
         *
         * @param servletContext the servlet context
         * @param basePath       the base path
         */
        public BasePathAwareRelativePathProvider(ServletContext servletContext, String basePath) {
            super(servletContext);
            this.basePath = basePath;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * springfox.documentation.spring.web.paths.RelativePathProvider#applicationPath
         * ()
         */
        @Override
        protected String applicationPath() {
            return Paths.removeAdjacentForwardSlashes(
                    UriComponentsBuilder.fromPath(super.applicationPath()).path(basePath).build().toString());
        }

        /*
         * (non-Javadoc)
         * 
         * @see springfox.documentation.spring.web.paths.AbstractPathProvider#
         * getOperationPath(java.lang.String)
         */
        @Override
        public String getOperationPath(String operationPath) {
            final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/");
            return Paths.removeAdjacentForwardSlashes(
                    uriComponentsBuilder.path(operationPath.replaceFirst("^" + basePath, "")).build().toString());
        }
    }

}
