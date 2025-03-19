package solutions.onz.services.imagengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.domain.DeepLink;
import solutions.onz.services.imagengine.repository.DeepLinkRepository;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Reactive Service for managing deep links.
 */
@Slf4j
@Service
public class DeepLinkService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CONSONANTS = "bcdfghjklmnpqrstvwxyz".toCharArray();
    private static final char[] VOWELS = "aeiou".toCharArray();

    private final DeepLinkRepository deepLinkRepository;

    /**
     * Constructor for DeepLinkService.
     *
     * @param deepLinkRepository the repository for deep links
     */
    public DeepLinkService(DeepLinkRepository deepLinkRepository) {
        this.deepLinkRepository = deepLinkRepository;
    }

    /**
     * Retrieves a deep link by its shortcut.
     *
     * @param shortLink the shortcut of the deep link
     * @return a Mono emitting the deep link if found and not expired, otherwise an error
     */
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

    /**
     * Creates a new deep link for the given URL.
     *
     * @param url the URL to create a deep link for
     * @return a Mono emitting the created deep link
     */
    public Mono<DeepLink> createLink(String url) {
        return this.deepLinkRepository.findByPathAndCreatedAfter(url, Instant.now().minusSeconds((60 * 60) * 168))
                .collectList()
                .flatMap(dl -> {
                    if (!dl.isEmpty()) {
                        DeepLink d = dl.getFirst();
                        d.setCreated(Instant.now()); // Reset timer
                        return this.deepLinkRepository.save(d);
                    }
                    DeepLink d = new DeepLink();
                    d.setShortcut(generateLinkHash());
                    d.setPath(url);
                    d.setCreated(Instant.now());

                    return this.deepLinkRepository.save(d);
                })
                .doOnError(error -> log.error("Error creating deep link: {}", error.getMessage()));
    }

    /**
     * Generates a random hash for the deep link.
     *
     * @return the generated hash
     */
    public String generateLinkHash() {
        StringBuilder hash = new StringBuilder(10);
        for (int i = 0; i < 5; i++) {
            hash.append(CONSONANTS[RANDOM.nextInt(CONSONANTS.length)]);
            hash.append(VOWELS[RANDOM.nextInt(VOWELS.length)]);
        }
        return hash.toString();
    }
}
