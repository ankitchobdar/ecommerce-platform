package org.project.common.order;

import jakarta.persistence.*;
import lombok.Data;
import org.project.common.Status;

import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    @ElementCollection
    private List<org.project.common.order.Item> items;
    private Double total;
    private Status orderStatus;
}
