package solutions.onz.services.imagengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "imagengine", ignoreUnknownFields = false)
public class ApplicationProperties {
}
