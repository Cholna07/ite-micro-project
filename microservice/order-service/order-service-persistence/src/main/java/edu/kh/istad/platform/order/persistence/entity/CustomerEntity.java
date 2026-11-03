package edu.kh.istad.platform.order.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("customers")
public class CustomerEntity {
    @Id
    private UUID id;
    private String family_name;
    private String given_name;
    private String username;
}
