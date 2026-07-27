package org.project.common.order;

import jakarta.persistence.*;
import lombok.Data;
import org.project.common.inventory.Item;
import org.project.common.Status;
import org.project.common.payment.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private Long sagaId;
    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private List<Item> items;
    private Double total;
    @Enumerated(EnumType.STRING)
    private Status orderStatus;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    //delete
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
}
