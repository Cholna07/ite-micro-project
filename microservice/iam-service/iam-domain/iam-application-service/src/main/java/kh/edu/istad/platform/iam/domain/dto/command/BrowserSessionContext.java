package kh.edu.istad.platform.iam.domain.dto.command;

public record BrowserSessionContext(String ipAddress, String userAgent, String authenticationType,
                                    String provider, Integer maxInactiveIntervalSeconds) {
}
