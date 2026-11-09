
package kh.edu.istad.platform.iam.domain.entity;

import kh.edu.istad.platform.iam.domain.valueobject.PermissionEffect;

import java.time.Instant;

public class UserPermission {

    private Long id;

    private Long userId;
    private Long permissionId;

    // Permission effect: GRANT or DENY
    private PermissionEffect effect;

    // Assignment information
    private Instant assignedAt;
    private String assignedBy;
    private Instant expiresAt;

    // Revocation information
    private Instant revokedAt;
    private String revokedBy;
    private String reason;

    public UserPermission() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public PermissionEffect getEffect() {
        return effect;
    }

    public void setEffect(PermissionEffect effect) {
        this.effect = effect;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
