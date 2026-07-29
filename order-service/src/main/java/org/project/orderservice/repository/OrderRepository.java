package org.project.orderservice.repository;

import org.project.common.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    public Order findByOrderId(Long orderId);

}
