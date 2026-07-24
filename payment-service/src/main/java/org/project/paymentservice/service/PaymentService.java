package org.project.paymentservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.Status;
import org.project.common.payment.Payment;
import org.project.common.payment.PaymentDTO;
import org.project.common.utility.MessageUtility;
import org.project.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository PaymentRepository;

    @Transactional
    public PaymentDTO processPayment(Payment payment) {
        log.info("Payment service processing payment {}", payment);
        PaymentRepository.save(payment);
        return new PaymentDTO(MessageUtility.getBaseMessage(Status.SUCCESS, "Payment processed successfully"), payment.getPaymentId(), Status.PENDING);
    }

    @Transactional
    public PaymentDTO confirmPayment(Long paymentId) {
        log.info("Payment service confirming payment {}", paymentId);
        Payment payment = PaymentRepository.findByPaymentId(paymentId);
        if (payment != null)
            payment.setPaymentStatus(Status.COMPLETED);
        else
            return new PaymentDTO(MessageUtility.getBaseMessage(Status.FAILED, "Payment not found"), -1L, Status.FAILED);
        return new PaymentDTO(MessageUtility.getBaseMessage(Status.SUCCESS, "Payment confirmed successfully"),
                payment.getPaymentId(), payment.getPaymentStatus());
    }

    @Transactional
    public PaymentDTO cancelPayment(Long paymentId) {
        log.info("Payment service canceling payment {}", paymentId);
        Payment payment = PaymentRepository.findByPaymentId(paymentId);
        if (payment != null)
            payment.setPaymentStatus(Status.CANCELED);
        else
            return new PaymentDTO(MessageUtility.getBaseMessage(Status.FAILED, "Payment not found"), -1L, Status.FAILED);
        return new PaymentDTO(MessageUtility.getBaseMessage(Status.SUCCESS, "Payment canceled successfully"),
                payment.getPaymentId(), payment.getPaymentStatus());
    }
    
}
