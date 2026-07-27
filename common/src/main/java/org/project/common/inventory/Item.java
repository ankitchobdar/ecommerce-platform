package org.project.common.inventory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name= "items")
@NoArgsConstructor
@AllArgsConstructor
public class Item implements Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;
    private String name;
    private Integer quantity;
    @Transient
    private Integer actualQuantity;
    private Integer reservedQuantity;
    private String reservationId;
    private Double price;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    @Override
    public Item clone() {
        try {
            return (Item) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
