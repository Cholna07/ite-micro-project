package edu.kh.istad.platform.order.persistence.entity;

import java.util.UUID;

public record OrderIteamEntityId(
        Integer id,
        UUID orderId
) {
}
