package solutions.onz.services.imagengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public Optional<DeepLink> getDeepLink(String shortLink) {
        Optional<DeepLink> out = Optional.empty();
        if (this.deepLinkRepository.findByShortcut(shortLink).isPresent()) {
            DeepLink dl = this.deepLinkRepository.findByShortcut(shortLink).get();
            if (Instant.now().getEpochSecond() - dl.getCreated().getEpochSecond() < (60 * 60) * 168) {
                out = Optional.of(dl);
            } else {
                log.info("Deep link expired: {}", shortLink);
                this.deepLinkRepository.deleteById(dl.getId());
            }
        } else {
            log.info("Deep link not found: {}", shortLink);
        }
        return out;
    }

    public DeepLink createLink(String url) {
        List<DeepLink> dl = this.deepLinkRepository.findByPathAndCreatedAfter(url, Instant.now().minusSeconds((60 * 60) * 168));

        if (!dl.isEmpty()) {
            DeepLink d = dl.getFirst();
            d.setCreated(Instant.now()); // Reset timer
            return this.deepLinkRepository.save(d);
        }

        return this.deepLinkRepository.save(new DeepLink(null, generateLinkHash(), url, Instant.now()));
    }

    public String generateLinkHash() {
        StringBuilder hash = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            hash.append(CONSONANTS[RANDOM.nextInt(CONSONANTS.length)]);
            hash.append(VOWELS[RANDOM.nextInt(VOWELS.length)]);
        }
        return hash.toString();
    }
}
