package org.project.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseMessage {
    private String id;
    private LocalDateTime timestamp;
    private Status status;
    private String message;
}
