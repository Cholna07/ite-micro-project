package edu.kh.istad.platform.order.persistence.entity;

import kh.edu.istad.common.domain.valueobject.BusinessId;
import kh.edu.istad.common.domain.valueobject.ProductId;

import java.util.UUID;

public record BusinessEntityId(
       UUID businessId,
       UUID productId
) {
}
