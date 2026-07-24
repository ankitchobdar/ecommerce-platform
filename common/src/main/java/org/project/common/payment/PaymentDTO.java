package org.project.common.payment;

import org.project.common.BaseMessage;
import org.project.common.Status;

public record PaymentDTO(
    BaseMessage baseMessage,
    Long paymentId,
    Status paymentStatus
) {}