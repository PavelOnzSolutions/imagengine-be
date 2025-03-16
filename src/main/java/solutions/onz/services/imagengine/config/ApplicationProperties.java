package solutions.onz.services.imagengine.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@ConfigurationProperties(prefix = "imagengine", ignoreUnknownFields = false)
public class ApplicationProperties {
    private Security security = new Security();

    @Getter
    @Setter
    public static class Security {
        public String csp;
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
