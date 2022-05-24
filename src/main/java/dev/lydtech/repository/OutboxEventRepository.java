package dev.lydtech.repository;

import java.util.UUID;

import dev.lydtech.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
}
