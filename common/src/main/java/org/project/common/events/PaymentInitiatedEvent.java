package org.project.common.events;

import lombok.*;
import org.project.common.payment.PaymentType;
import org.project.common.saga.SagaEvent;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PaymentInitiatedEvent extends SagaEvent {
    private Long sagaId;
    private String orderId;
    private BigDecimal total;
    private PaymentType paymentType;
}
