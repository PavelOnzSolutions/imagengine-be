package solutions.onz.services.imagengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.domain.DeepLink;
import solutions.onz.services.imagengine.repository.DeepLinkRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DeepLinkService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CONSONANTS = "bcdfghjklmnpqrstvwxyz".toCharArray();
    private static final char[] VOWELS = "aeiou".toCharArray();

    private final DeepLinkRepository deepLinkRepository;

    public DeepLinkService(DeepLinkRepository deepLinkRepository) {
        this.deepLinkRepository = deepLinkRepository;
    }

    public Mono<DeepLink> getDeepLink(String shortLink) {
        return this.deepLinkRepository.findByShortcut(shortLink)
                .flatMap(dl -> {
                    if (Instant.now().getEpochSecond() - dl.getCreated().getEpochSecond() < (60 * 60) * 168) {
                        return Mono.just(dl);
                    } else {
                        log.info("Deep link expired: {}", shortLink);
                        return this.deepLinkRepository.deleteById(dl.getId())
                                .then(Mono.empty());
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Deep link not found or expired")))
                .doOnError(error -> log.error("Error retrieving deep link: {}", error.getMessage()));
    }

    public Mono<DeepLink> createLink(String url) {
        return this.deepLinkRepository.findByPathAndCreatedAfter(url, Instant.now().minusSeconds((60 * 60) * 168))
                .collectList()
                .flatMap(dl -> {
                    if (!dl.isEmpty()) {
                        DeepLink d = dl.get(0);
                        d.setCreated(Instant.now()); // Reset timer
                        return this.deepLinkRepository.save(d);
                    }
                    return this.deepLinkRepository.save(new DeepLink(null, generateLinkHash(), url, Instant.now()));
                })
                .doOnError(error -> log.error("Error creating deep link: {}", error.getMessage()));
    }

    public String generateLinkHash() {
        StringBuilder hash = new StringBuilder(10);
        for (int i = 0; i < 4; i++) {
            hash.append(CONSONANTS[RANDOM.nextInt(CONSONANTS.length)]);
            hash.append(VOWELS[RANDOM.nextInt(VOWELS.length)]);
        }
        return hash.toString();
    }
}
