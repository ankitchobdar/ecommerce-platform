package org.project.common.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private Long sagaId;
    private String orderId;
    private BigDecimal total;
    private PaymentType paymentType;
}
