package org.project.common.inventory;

import org.project.common.BaseMessage;

import java.util.List;

public record InventoryDTO(BaseMessage baseMessage, List<Item> items) {
}
