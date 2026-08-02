package org.project.common.events;

import lombok.*;
import org.project.common.inventory.Item;
import org.project.common.payment.PaymentType;
import org.project.common.saga.SagaEvent;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInitiatedEvent extends SagaEvent {
    private Long sagaId;
    private String orderId;
    private List<Item> items;
    private BigDecimal total;
    private PaymentType paymentType;
}
