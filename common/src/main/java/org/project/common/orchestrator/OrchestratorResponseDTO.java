package org.project.common.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.BaseMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrchestratorResponseDTO {
    private BaseMessage baseMessage;
    private Long sagaId;
    private Long orderId;
    private Long paymentId;
    private Long transactionId;
}
