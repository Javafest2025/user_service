package org.solace.scholar_ai.user_service.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String notificationType;
    private String recipientEmail;
    private String recipientName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    private Map<String, Object> templateData;

    // Optional: used by notification-service to persist and for frontend queries
    private java.util.UUID userId;

    public enum NotificationType {
        WELCOME_EMAIL,
        PASSWORD_RESET,
        EMAIL_VERIFICATION,
        ACCOUNT_UPDATE,
        WEB_SEARCH_COMPLETED,
        SUMMARIZATION_COMPLETED,
        PROJECT_DELETED,
        GAP_ANALYSIS_COMPLETED
    }
}
