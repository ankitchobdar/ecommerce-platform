package org.project.paymentservice.service;

import jakarta.transaction.Transactional;
import org.project.common.Status;
import org.project.common.payment.Payment;
import org.project.common.payment.PaymentDTO;
import org.project.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository PaymentRepository;

    @Transactional
    public PaymentDTO processPayment(Payment Payment) {
        PaymentRepository.save(Payment);
        return new PaymentDTO(null, Payment.getPaymentId());
    }

    @Transactional
    public PaymentDTO confirmPayment(Long paymentId) {
        Payment payment = PaymentRepository.findByPaymentId(paymentId);
        if (payment != null)
            payment.setPaymentStatus(Status.COMPLETED);
        else
            return new PaymentDTO(null, -1L);
        return new PaymentDTO(null, payment.getPaymentId());
    }

    @Transactional
    public PaymentDTO cancelPayment(Long paymentId) {
        Payment payment = PaymentRepository.findByPaymentId(paymentId);
        if (payment != null)
            payment.setPaymentStatus(Status.CANCELED);
        else
            return new PaymentDTO(null, -1L);
        return new PaymentDTO(null, payment.getPaymentId());
    }
    
}
