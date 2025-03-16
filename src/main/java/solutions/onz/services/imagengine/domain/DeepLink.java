package solutions.onz.services.imagengine.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Document
public class DeepLink {
    @Id
    private UUID id;
    private String shortcut;
    private String path;
    private Instant created;
}
