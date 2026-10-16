package kh.edu.istad.platform.business.domain.event;

import kh.edu.istad.platform.business.domain.entity.Business;

import java.time.ZonedDateTime;

public class BusinessRegisteredEvent {

    private final Business business;
    private final ZonedDateTime createdAt;

    public BusinessRegisteredEvent(
            Business business,
            ZonedDateTime createdAt
    ) {
        if (business == null) {
            throw new IllegalArgumentException(
                    "Business cannot be null"
            );
        }

        this.business = business;
        this.createdAt = createdAt != null
                ? createdAt
                : ZonedDateTime.now();
    }

    public Business getBusiness() {
        return business;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}