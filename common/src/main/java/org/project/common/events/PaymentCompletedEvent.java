package org.project.common.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.project.common.Status;
import org.project.common.saga.SagaEvent;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PaymentCompletedEvent extends SagaEvent {
    
    private Status status;
}
