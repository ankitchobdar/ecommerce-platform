package org.project.common.inventory;

import org.project.common.BaseMessage;
import org.project.common.Item;

import java.util.List;

public record InventoryDTO(BaseMessage baseMessage, List<Item> items) {
}
