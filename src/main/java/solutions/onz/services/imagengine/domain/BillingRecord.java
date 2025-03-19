package solutions.onz.services.imagengine.domain;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;

@Data
@Document
public class BillingRecord extends AbstractAuditingEntity<String> implements Serializable {
    @Id
    private String id;
}
