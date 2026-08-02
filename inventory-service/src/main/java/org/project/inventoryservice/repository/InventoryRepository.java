package org.project.inventoryservice.repository;

import org.project.common.inventory.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Item, Long> {

    public Item getItemById(Long id);

    public List<Item> getItemByReservationId(String reservationId);
}
