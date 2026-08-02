package org.project.common.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.inventory.Item;
import org.project.common.Status;
import org.project.common.payment.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    @SequenceGenerator(name = "orders_seq", sequenceName = "orders_id_seq", allocationSize = 1)
    private Long id;
    private String orderId;
    private Long sagaId;
    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private List<Item> items;
    private BigDecimal total;
    @Enumerated(EnumType.STRING)
    private Status orderStatus;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
