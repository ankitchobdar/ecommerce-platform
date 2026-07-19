package org.project.common.order;

import org.project.common.BaseMessage;

public record OrderDTO (
    BaseMessage baseMessage,
    Long orderId
) {
}
