package org.project.common.utility;

import org.project.common.BaseMessage;
import org.project.common.Status;

public class MessageUtility {

    public static BaseMessage getBaseMessage(Status status, String message) {
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.setId(java.util.UUID.randomUUID().toString());
        baseMessage.setTimestamp(java.time.LocalDateTime.now());
        baseMessage.setStatus(status);
        baseMessage.setMessage(message);
        return baseMessage;
    }
}
