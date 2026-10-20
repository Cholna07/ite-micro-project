
package kh.edu.istad.platform.customer.persistence.specification;

import kh.edu.istad.platform.customer.domain.dto.CustomerFilter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CustomerMongoFilter {

    public Query build(CustomerFilter filter) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (filter.username() != null
                && !filter.username().isBlank()) {
            criteriaList.add(
                    containsIgnoreCase("username", filter.username())
            );
        }

        if (filter.familyName() != null
                && !filter.familyName().isBlank()) {
            criteriaList.add(
                    containsIgnoreCase("familyName", filter.familyName())
            );
        }

        if (filter.givenName() != null
                && !filter.givenName().isBlank()) {
            criteriaList.add(
                    containsIgnoreCase("givenName", filter.givenName())
            );
        }

        if (filter.email() != null
                && !filter.email().isBlank()) {
            criteriaList.add(
                    containsIgnoreCase("email", filter.email())
            );
        }

        if (filter.status() != null
                && !filter.status().isBlank()) {
            criteriaList.add(
                    Criteria.where("status")
                            .is(filter.status().trim().toUpperCase())
            );
        }

        if (filter.segmentId() != null) {
            criteriaList.add(
                    Criteria.where("segmentId").is(filter.segmentId())
            );
        }

        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            query.addCriteria(
                    new Criteria().andOperator(
                            criteriaList.toArray(new Criteria[0])
                    )
            );
        }

        return query;
    }

    private Criteria containsIgnoreCase(String field, String value) {
        return Criteria.where(field).regex(
                Pattern.quote(value.trim()),
                "i"
        );
    }
}
