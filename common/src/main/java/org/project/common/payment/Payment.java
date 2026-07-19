package org.project.common.payment;

import jakarta.persistence.*;
import lombok.Data;
import org.project.common.Status;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    private Long orderId;
    private PaymentType paymentType;
    private Status paymentStatus;
}
