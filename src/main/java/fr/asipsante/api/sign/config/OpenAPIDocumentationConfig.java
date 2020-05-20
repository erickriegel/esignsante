package fr.asipsante.api.sign.config;

import java.util.Optional;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The Class OpenAPIDocumentationConfig.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2019-09-13T11:20:59.740+02:00[Europe/Paris]")

@Configuration
@EnableSwagger2
public class OpenAPIDocumentationConfig {

    /**
     * Api info.
     *
     * @return the api info
     */
    ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("ASIP-Sign").description(
                "API du composant ASIP-Sign.  Ce composant dit de \"signature\" mutualise et homogénéise la mise en oeuvre des besoins autour de la signature.  Il permet aux partenaires de l'ASIP Santé de signer leurs documents ainsi que de vérifier la validité d'une signature ou d'un certificat.     ")
                .version("2.0.0.0").contact(new Contact("Asip Santé",
                        "https://esante.gouv.fr/", "support@asipsante.fr"))
                .build();
    }

    /**
     * Custom implementation.
     *
     * @param servletContext
     *            the servlet context
     * @param basePath
     *            the base path
     * @return the docket
     */
    @Bean
    public Docket customImplementation(ServletContext servletContext,
            @Value("${openapi.aSIPSign.base-path:}") String basePath) {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false).select()
                .apis(RequestHandlerSelectors
                        .basePackage("fr.asipsante.api.sign.ws.api"))
                .build()
                .directModelSubstitute(org.threeten.bp.LocalDate.class,
                        java.sql.Date.class)
                .directModelSubstitute(org.threeten.bp.OffsetDateTime.class,
                        java.util.Date.class)
                .genericModelSubstitutes(Optional.class).apiInfo(apiInfo());
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
         * @param servletContext
         *            the servlet context
         * @param basePath
         *            the base path
         */
        public BasePathAwareRelativePathProvider(ServletContext servletContext,
                String basePath) {
            super(servletContext);
            this.basePath = basePath;
        }

        /*
         * (non-Javadoc)
         * 
         * @see springfox.documentation.spring.web.paths.RelativePathProvider#
         * applicationPath()
         */
        @Override
        protected String applicationPath() {
            return Paths.removeAdjacentForwardSlashes(
                    UriComponentsBuilder.fromPath(super.applicationPath())
                            .path(basePath).build().toString());
        }

        /*
         * (non-Javadoc)
         * 
         * @see springfox.documentation.spring.web.paths.AbstractPathProvider#
         * getOperationPath(java.lang.String)
         */
        @Override
        public String getOperationPath(String operationPath) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                    .fromPath("/");
            return Paths.removeAdjacentForwardSlashes(uriComponentsBuilder
                    .path(operationPath.replaceFirst("^" + basePath, ""))
                    .build().toString());
        }
    }

}
