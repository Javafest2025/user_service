package org.solace.scholar_ai.user_service.dto.auth;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.solace.scholar_ai.user_service.model.UserRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private UUID userId;
    private UserRole role;
}
