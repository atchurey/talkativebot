package com.atchurey.tools.talkativebot.springbootstarter.store.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "talkative_pending_interactions",
        indexes = {
                @Index(
                        name = "idx_talkative_pending_interaction_address_key",
                        columnList = "address_key",
                        unique = true
                ),
                @Index(
                        name = "idx_talkative_pending_interaction_expires_at",
                        columnList = "expires_at"
                )
        }
)
public class PendingInteractionEntity {

    @Id
    @Column(name = "id", nullable = false, length = 256)
    private String id;

    @Column(name = "address_key", nullable = false, unique = true, length = 512)
    private String addressKey;

    @Column(name = "scope", length = 256)
    private String scope;

    @Column(name = "channel", nullable = false, length = 256)
    private String channel;

    @Column(name = "user_id", length = 256)
    private String userId;

    @Column(name = "session_id", length = 256)
    private String sessionId;

    @Column(name = "conversation_id", length = 256)
    private String conversationId;

    @Column(name = "conversation_type", nullable = false, length = 512)
    private String conversationType;

    @Column(name = "current_topic_key", length = 256)
    private String currentTopicKey;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
