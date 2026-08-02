package org.project.common.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.project.common.inventory.Item;
import org.project.common.saga.SagaEvent;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InventoryReservedEvent extends SagaEvent {
    private Long sagaId;
    private List<Item> items;

    public InventoryReservedEvent(long sagaId, String orderId, LocalDateTime createdAt, LocalDateTime completedAt, List<Item> items) {
        super(sagaId, orderId, createdAt, completedAt);
        this.items = items;
    }
}

