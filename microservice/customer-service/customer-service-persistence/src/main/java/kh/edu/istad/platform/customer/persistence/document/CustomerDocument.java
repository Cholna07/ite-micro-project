package kh.edu.istad.platform.customer.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customers")
public class CustomerDocument implements Persistable<String> {
    @Id
    private String id;
    private String username;
    private String familyName;
    private String givenName;
    private String email;
    private String phoneNumber;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedBy
    private String modifiedBy;

    @LastModifiedDate
    private String modifiedAt;

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
