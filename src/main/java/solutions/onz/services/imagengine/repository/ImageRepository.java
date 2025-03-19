package solutions.onz.services.imagengine.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import solutions.onz.services.imagengine.domain.ImageEntity;

import java.util.UUID;

@Repository
public interface ImageRepository extends ReactiveMongoRepository<ImageEntity, String> {
}
