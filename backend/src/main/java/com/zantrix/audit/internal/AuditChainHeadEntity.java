package com.zantrix.audit.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Single row holding the hash of the most recent audit entry. Recording an
 * entry locks this row, so audit writes are serialized against each other and
 * the chain stays consistent, without serializing the rest of the application.
 */
@Entity
@Table(name = "audit_chain_head")
class AuditChainHeadEntity {

    @Id
    private Long id;

    @Column(name = "last_hash", nullable = false)
    private String lastHash;

    protected AuditChainHeadEntity() {
    }

    String getLastHash() {
        return lastHash;
    }

    void setLastHash(String lastHash) {
        this.lastHash = lastHash;
    }
}
