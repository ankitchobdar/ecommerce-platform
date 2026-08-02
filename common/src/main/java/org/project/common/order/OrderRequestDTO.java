package org.project.common.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.inventory.Item;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderRequestDTO {
    private Long sagaId;
    private String orderId;
    private List<Item> items;
    private BigDecimal total;
}
