package kh.edu.istad.platform.iam.domain.dto.result;

public record LoginAttemptUpdate(int failedLoginAttempts, boolean locked, boolean changed) { }
