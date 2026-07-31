package org.project.paymentservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.Status;
import org.project.common.payment.Payment;
import org.project.common.payment.PaymentDTO;
import org.project.common.payment.PaymentRequestDTO;
import org.project.common.payment.PaymentResponseDTO;
import org.project.common.utility.MessageUtility;
import org.project.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) {
        // Implementation for processing payment request
        Payment payment = null;
        try {
            payment = Payment.builder()
                .sagaId(paymentRequestDTO.getSagaId())
                .paymentStatus(Status.COMPLETED)
                .amount(paymentRequestDTO.getTotal())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            //Notify Orchestrator about failure
            return new PaymentResponseDTO(
                MessageUtility.getBaseMessage(Status.FAILED, "Error processing payment: " + e.getMessage()),
                paymentRequestDTO.getSagaId(),
                null,
                Status.FAILED
            );
        }
        return new PaymentResponseDTO(
            MessageUtility.getBaseMessage(Status.SUCCESS, "Payment processed successfully"),
                paymentRequestDTO.getSagaId(),
                payment.getPaymentId(),
                Status.SUCCESS
        );
    }

    public PaymentResponseDTO reversePayment(Long paymentId) {
        // Implementation for retrieving payment by paymentId
        log.info("Payment service reversePayment {}", paymentId);
        Payment payment = null;
        try {
            payment = paymentRepository.findByPaymentId(paymentId);
            if (payment != null) {
                payment.setPaymentStatus(Status.CANCELED);
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving payment for payment ID: {}", paymentId, e);
            return new PaymentResponseDTO(
                MessageUtility.getBaseMessage(Status.FAILED, "Error reversing payment: " + paymentId),
                payment != null ? payment.getSagaId() : null,
                paymentId,
                Status.FAILED);
        }
        return new PaymentResponseDTO(
            MessageUtility.getBaseMessage(Status.SUCCESS, "Payment reversed successfully"),
            payment != null ? payment.getSagaId() : null,
            paymentId,
            payment != null ? payment.getPaymentStatus() : Status.SUCCESS
        );
    }
}
