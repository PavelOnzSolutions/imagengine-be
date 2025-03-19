package solutions.onz.services.imagengine.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "deep_link")
public class DeepLink implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String shortcut;
    private String path;
    private Instant created;

}
