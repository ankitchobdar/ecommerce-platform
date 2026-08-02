package org.project.common.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.BaseMessage;
import org.project.common.Status;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDTO {
    private BaseMessage baseMessage;
    private List<Item> items;
    private List<Item> outOfStockItems;
    private Status status;
}
