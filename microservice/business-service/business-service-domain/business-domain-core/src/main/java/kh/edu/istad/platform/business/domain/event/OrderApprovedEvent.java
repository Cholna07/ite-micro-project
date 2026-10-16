package kh.edu.istad.platform.business.domain.event;

import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.OrderApprovalId;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderApprovedEvent {

    private final OrderApprovalId orderApprovalId;
    private final BusinessId businessId;
    private final List<String> failureMessages;
    private final ZonedDateTime createdAt;

    public OrderApprovedEvent(
            OrderApprovalId orderApprovalId,
            BusinessId businessId,
            List<String> failureMessages,
            ZonedDateTime createdAt
    ) {
        this.orderApprovalId = orderApprovalId;
        this.businessId = businessId;
        this.failureMessages = failureMessages == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(
                new ArrayList<>(failureMessages)
        );
        this.createdAt = createdAt != null
                ? createdAt
                : ZonedDateTime.now();
    }

    public OrderApprovalId getOrderApprovalId() {
        return orderApprovalId;
    }

    public BusinessId getBusinessId() {
        return businessId;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}