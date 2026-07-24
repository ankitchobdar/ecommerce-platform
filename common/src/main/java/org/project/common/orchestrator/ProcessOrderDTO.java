package org.project.common.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.BaseMessage;
import org.project.common.Item;
import org.project.common.Status;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessOrderDTO {
    private BaseMessage baseMessage;
    private List<Item> items;
    private Double totalPrice;
    private Status paymentStatus;
}
