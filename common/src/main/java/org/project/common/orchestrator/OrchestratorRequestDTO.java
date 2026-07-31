package org.project.common.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.inventory.Item;
import org.project.common.payment.PaymentType;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrchestratorRequestDTO {
    private List<Item> items;
    private BigDecimal totalAmount;
    private PaymentType paymentType;
}
