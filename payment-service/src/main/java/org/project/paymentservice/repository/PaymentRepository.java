package org.project.paymentservice.repository;

import org.project.common.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    public Payment findByPaymentId(Long paymentId);
}
