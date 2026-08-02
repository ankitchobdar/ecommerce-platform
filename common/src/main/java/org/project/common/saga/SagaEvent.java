package org.project.common.saga;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public abstract class SagaEvent {
    public Long sagaId;
    public String orderId;
    public LocalDateTime createdAt;
    public LocalDateTime completedAt;

    public SagaEvent(Long sagaId, String orderId, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }
}