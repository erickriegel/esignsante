package fr.asipsante.api.sign.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * The Class SwaggerDocumentationConfig.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-09-13T15:23:25.627+02:00[Europe/Paris]")
@Configuration
public class SwaggerDocumentationConfig {

    /**
     * Api info.
     *
     * @return the api info
     */
    ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("ASIP-Sign").description(
                "API du composant ASIP-Sign.  Ce composant dit de \"signature\" mutualise et homogénéise la mise en oeuvre des besoins autour de la signature.  Il permet aux partenaires de l'ASIP Santé de signer leurs documents ainsi que de vérifier la validité d'une signature ou d'un certificat.     ")
                .version("2.0.0.0").contact(new Contact("Asip santé",
                        "https://esante.gouv.fr", "support@asipsante.fr"))
                .build();
    }

    /**
     * Custom implementation.
     *
     * @return the docket
     */
    @Bean
    public Docket customImplementation() {
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

}
