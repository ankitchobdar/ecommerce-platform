package org.project.common.events;

import lombok.*;
import org.project.common.inventory.Item;
import org.project.common.saga.SagaEvent;
import org.project.common.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdatedEvent extends SagaEvent {
    private Long sagaId;
    private String orderId;
    private List<Item> items;
    private BigDecimal total;
    private List<Item> outOfStockItems;
    private Status status;

    public InventoryUpdatedEvent(long sagaId, String orderId, LocalDateTime createdAt, LocalDateTime completedAt, List<Item> items, List<Item> outOfStockItems, Status status) {
        super(sagaId, orderId, createdAt, completedAt);
        this.items = items;
        this.outOfStockItems = outOfStockItems;
        this.status = status;
    }
}
