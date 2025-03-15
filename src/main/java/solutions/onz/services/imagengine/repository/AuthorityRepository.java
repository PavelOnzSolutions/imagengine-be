package solutions.onz.services.imagengine.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import solutions.onz.services.imagengine.domain.Authority;

import java.util.UUID;

@Repository
public interface AuthorityRepository extends ReactiveMongoRepository<Authority, UUID> {
}
