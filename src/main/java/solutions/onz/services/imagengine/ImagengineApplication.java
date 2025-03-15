package solutions.onz.services.imagengine;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import nu.pattern.OpenCV;
import solutions.onz.services.imagengine.config.ApplicationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({ ApplicationProperties.class })
public class ImagengineApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImagengineApplication.class, args);
	}

	@PostConstruct
	public void init() {
		log.info("Loading OpenCV");
		OpenCV.loadLocally();
		log.info("OpenCV loaded");
	}
}
