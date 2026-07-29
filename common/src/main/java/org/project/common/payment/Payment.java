package org.project.common.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payments_seq")
    @SequenceGenerator(name = "payments_seq", sequenceName = "payments_id_seq", allocationSize = 1)
    private Long paymentId;
    private Long sagaId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    @Enumerated(EnumType.STRING)
    private Status paymentStatus;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
