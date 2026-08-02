package org.project.common.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.project.common.inventory.Item;
import org.project.common.saga.SagaEvent;
import org.project.common.Status;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InventoryFailedEvent extends SagaEvent {
    public List<Item> items;
    public Status inventoryStatus;

    public InventoryFailedEvent(long sagaId, String orderId, LocalDateTime createdAt, LocalDateTime completedAt, List<Item> items, Status inventoryStatus) {
        super(sagaId, orderId, createdAt, completedAt);
        this.items = items;
        this.inventoryStatus = inventoryStatus;
    }
}
