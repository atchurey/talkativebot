package com.atchurey.talkativebot.springbootstarter.store.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface JpaPendingInteractionRepository extends JpaRepository<PendingInteractionEntity, String> {

    Optional<PendingInteractionEntity> findByAddressKey(String addressKey);

    void deleteByAddressKey(String addressKey);

    void deleteByExpiresAtBefore(Instant instant);
}
