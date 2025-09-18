package org.solace.scholar_ai.user_service.service.notification;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solace.scholar_ai.user_service.dto.notification.NotificationRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.notification.exchange.name}")
    private String notificationExchangeName;

    @Value("${spring.rabbitmq.notification.routing.key}")
    private String notificationRoutingKey;

    public void sendWelcomeEmail(String email, String name) {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.WELCOME_EMAIL.name())
                .recipientEmail(email)
                .recipientName(name)
                .timestamp(Instant.now())
                .templateData(Map.of(
                        "userName",
                        name,
                        "welcomeMessage",
                        "Welcome to ScholarAI! We're excited to have you on board.",
                        "appName",
                        "ScholarAI",
                        "supportEmail",
                        "support@scholarai.com"))
                .build();

        sendNotification(request);
    }

    public void sendPasswordResetEmail(String email, String name, String resetCode) {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.PASSWORD_RESET.name())
                .recipientEmail(email)
                .recipientName(name)
                .timestamp(Instant.now())
                .templateData(Map.of(
                        "userName",
                        name,
                        "resetCode",
                        resetCode,
                        "appName",
                        "ScholarAI",
                        "supportEmail",
                        "support@scholarai.com"))
                .build();

        sendNotification(request);
    }

    public void sendEmailVerificationEmail(String email, String name, String verificationCode) {
        NotificationRequest request = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.EMAIL_VERIFICATION.name())
                .recipientEmail(email)
                .recipientName(name)
                .timestamp(Instant.now())
                .templateData(Map.of(
                        "userName",
                        name,
                        "verificationCode",
                        verificationCode,
                        "appName",
                        "ScholarAI",
                        "supportEmail",
                        "support@scholarai.com"))
                .build();

        sendNotification(request);
    }

    private void sendNotification(NotificationRequest request) {
        try {
            rabbitTemplate.convertAndSend(notificationExchangeName, notificationRoutingKey, request);
            log.info(
                    "Notification sent successfully: {} to {}",
                    request.getNotificationType(),
                    request.getRecipientEmail());
        } catch (Exception e) {
            log.error(
                    "Failed to send notification: {} to {}",
                    request.getNotificationType(),
                    request.getRecipientEmail(),
                    e);
            // In a production environment, you might want to implement retry logic or dead
            // letter queue
        }
    }

    public void sendGenericNotificationToUser(
            UUID userId,
            String notificationType,
            Map<String, Object> templateData,
            String recipientEmail,
            String recipientName) {
        Map<String, Object> data =
                templateData != null ? new java.util.HashMap<>(templateData) : new java.util.HashMap<>();
        // Ensure common variables exist
        data.putIfAbsent("userName", recipientName);
        data.putIfAbsent("appName", "ScholarAI");
        data.putIfAbsent("supportEmail", "support@scholarai.com");

        NotificationRequest request = NotificationRequest.builder()
                .notificationType(notificationType)
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .timestamp(Instant.now())
                .templateData(data)
                .userId(userId)
                .build();
        sendNotification(request);
    }
}
