package org.project.common.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.BaseMessage;
import org.project.common.Status;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private BaseMessage baseMessage;
    private Long sagaId;
    private String orderId;
    private Status orderStatus;
    private BigDecimal total;
}
