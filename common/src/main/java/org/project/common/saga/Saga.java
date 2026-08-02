package org.project.common.saga;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.Status;

import java.time.LocalDateTime;

@Data
@Table(name = "sagas")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Saga {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sagas_seq")
    @SequenceGenerator(name = "sagas_seq", sequenceName = "sagas_id_seq", allocationSize = 1)
    private Long sagaId;
    @Enumerated(value = EnumType.STRING)
    private SagaStatus status;
    private String orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
