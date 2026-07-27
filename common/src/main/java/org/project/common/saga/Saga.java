package org.project.common.saga;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.common.Status;

import java.time.LocalDateTime;

@Data
@Table(name = "sagas")
@NoArgsConstructor
@AllArgsConstructor
public class Saga {
    private Long sagaId;
    private Status status;
    private LocalDateTime createdAt;
}
