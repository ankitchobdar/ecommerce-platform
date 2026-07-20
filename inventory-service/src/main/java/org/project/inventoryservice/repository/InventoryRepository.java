package org.project.inventoryservice.repository;

import org.project.common.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Item, Long> {

    public Item getItemByItemId(Long itemId);
}
