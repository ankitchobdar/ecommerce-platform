package org.project.paymentservice.controller;

import org.project.common.payment.Payment;
import org.project.common.payment.PaymentDTO;
import org.project.common.payment.PaymentRequestDTO;
import org.project.common.payment.PaymentResponseDTO;
import org.project.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public PaymentResponseDTO processPayment(@RequestBody PaymentRequestDTO paymentRequestDTO) {
        return paymentService.processPayment(paymentRequestDTO);
    }

    @GetMapping("reverse")
    public PaymentResponseDTO reversePayment(@RequestParam(name = "paymentid") Long paymentId) {
        return paymentService.reversePayment(paymentId);
    }
}
