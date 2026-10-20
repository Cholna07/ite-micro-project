package kh.edu.istad.platform.customersegment.persistence.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "customerSegment")
public class CustomerSegmentDocument {

    @Id
    private String id;
    private String type;
    private String description;
}