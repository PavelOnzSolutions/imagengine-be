/*
 * UITest Copyright 2021 Pavel Onz @ Deltatre Ltd
 *
 * <p>For internal use only
 */
package solutions.onz.services.imagengine.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableEncryptableProperties
public class EncryptionConfiguration {
    @Value("${imagengine.encryption.password:H0M0kl4d4}") // hardcoded default, why not?
    private String encryptionPassword;

    public EncryptionConfiguration() {}

    @Bean("jasyptStringEncryptor")
    public StandardPBEStringEncryptor getStandardPBEStringEncryptor() {
        StandardPBEStringEncryptor enc = new StandardPBEStringEncryptor();
        String algo = "PBEWithMD5AndDES";

        enc.setAlgorithm(algo);
        enc.setPassword(encryptionPassword);
        log.info("Encryption configured successfully with algorithm: {}", algo);
        return enc;
    }
}
