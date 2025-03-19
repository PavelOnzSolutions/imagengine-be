package solutions.onz.services.imagengine.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.domain.DeepLink;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface DeepLinkRepository extends ReactiveMongoRepository<DeepLink, String> {
    Mono<DeepLink> findByShortcut(String shortcut);
    Mono<DeepLink> findByPath(String path);
    Flux<DeepLink> findByPathAndCreatedAfter(String path, Instant createdAfter);
}
