package solutions.onz.services.imagengine.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.domain.User;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, UUID> {
    Mono<User> findOneByActivationKey(String activationKey);
    Flux<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);
    Mono<User> findOneByResetKey(String resetKey);
    Mono<User> findOneByEmailIgnoreCase(String email);
    Mono<User> findOneByLogin(String login);

    Flux<User> findAllByIdNotNull(Pageable pageable);
    Flux<User> findAllByIdNotNull();

    Flux<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);
}
