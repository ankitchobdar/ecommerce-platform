package org.project.common.payment;

import org.project.common.BaseMessage;

public record PaymentDTO(
    BaseMessage baseMessage,
    Long paymentId
) {
}
