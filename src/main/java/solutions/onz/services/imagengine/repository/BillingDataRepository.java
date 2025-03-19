package solutions.onz.services.imagengine.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import solutions.onz.services.imagengine.domain.AbstractAuditingEntity;
import solutions.onz.services.imagengine.domain.BillingRecord;

@Repository
interface BillingDataRepository extends ReactiveMongoRepository<BillingRecord, String> {
}
