package org.project.orchestrator.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OrchestratorService {

    @Transactional
    public String processOrder() {
        //checkInventory
        //processOrder
        //processPayment
        boolean orderReversed = false;
        try {
            //checkInventory
            //processOrder
            //processPayment
        } catch (Exception e) {
            reverseOrder();
            orderReversed = true;
        } finally {
            if(!orderReversed)
                reverseOrder();
        }
        return null;
    }

    @Transactional
    public String reverseOrder() {
        try {
            //reverseOrder
            //reversePayment
        } catch (Exception e) {
            //log error
        }
        return null;
    }
}
