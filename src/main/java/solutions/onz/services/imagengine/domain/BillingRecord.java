package solutions.onz.services.imagengine.domain;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Document
public class BillingRecord {
    @Id
    private UUID id;
}
