package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;

public record IssuedAccessToken(String value, Instant expiresAt) {
}
