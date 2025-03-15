package solutions.onz.services.imagengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@ConfigurationProperties(prefix = "imagengine", ignoreUnknownFields = false)
public class ApplicationProperties {
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
