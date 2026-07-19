package org.project.paymentservice.controller;

import org.project.common.payment.Payment;
import org.project.common.payment.PaymentDTO;
import org.project.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService PaymentService;

    @PostMapping("/process")
    public PaymentDTO processPayment(@RequestBody Payment payment) {
        return PaymentService.processPayment(payment);
    }

    @GetMapping("/confirm")
    public PaymentDTO confirmPayment(@RequestParam Long paymentId) {
        return PaymentService.confirmPayment(paymentId);
    }

    @GetMapping("/cancel")
    public PaymentDTO cancelPayment(@RequestParam Long paymentId) {
        return PaymentService.cancelPayment(paymentId);
    }
}
