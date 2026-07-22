package org.project.common.order;

import jakarta.persistence.*;
import lombok.Data;
import org.project.common.Item;
import org.project.common.Status;
import org.project.common.payment.PaymentType;

import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long orderId;
    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private List<Item> items;
    private Double total;
    private Status orderStatus;
    private PaymentType paymentType;
}
