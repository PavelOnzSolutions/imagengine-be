package solutions.onz.services.imagengine.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document
public class ImageEntity {
    @Id
    private String id;
    private String filePath;
}
