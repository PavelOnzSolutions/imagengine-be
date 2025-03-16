package solutions.onz.services.imagengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import solutions.onz.services.imagengine.aop.logging.LoggingAspect;


@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @Profile("local")
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }
}
