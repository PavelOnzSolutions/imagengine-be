package solutions.onz.services.imagengine.config;

import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.ActuatorOpenApiCustomizer;
import org.springdoc.core.customizers.ActuatorOperationCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

import static org.springdoc.core.utils.Constants.DEFAULT_GROUP_NAME;

@Slf4j
@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            openApi.getInfo().setTitle("ImageNgine Provider API");
            openApi.getInfo().setDescription("ImageNgine Provider API documentation. Provider allows user to upload a set of images, then apply filters and transformations to them and expose modified images via URL links.");
            openApi.getInfo().setVersion("0.0.1");
            openApi.addServersItem(new Server().url("/api"));
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "apiFirstGroupedOpenAPI")
    public GroupedOpenApi apiFirstGroupedOpenAPI() {
        log.debug("Initializing OpenApi API first group");
        return GroupedOpenApi.builder()
                .group("openapi")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "openAPIDefaultGroupedOpenAPI")
    public GroupedOpenApi openAPIDefaultGroupedOpenAPI(
            List<OpenApiCustomizer> openApiCustomizers,
            List<OperationCustomizer> operationCustomizers,
            @Qualifier("apiFirstGroupedOpenAPI") Optional<GroupedOpenApi> apiFirstGroupedOpenAPI
    ) {
        log.debug("Initializing OpenApi default group");
        GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                .group(DEFAULT_GROUP_NAME)
                .pathsToMatch("/api/**");
        openApiCustomizers
                .stream()
                .filter(customizer -> !(customizer instanceof ActuatorOpenApiCustomizer))
                .forEach(builder::addOpenApiCustomizer);
        operationCustomizers
                .stream()
                .filter(customizer -> !(customizer instanceof ActuatorOperationCustomizer))
                .forEach(builder::addOperationCustomizer);
        apiFirstGroupedOpenAPI
                .map(GroupedOpenApi::getPackagesToScan)
                .ifPresent(packagesToScan -> packagesToScan.forEach(builder::packagesToExclude));
        return builder.build();
    }
}
