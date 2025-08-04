package org.solace.scholar_ai.user_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.solace.scholar_ai.user_service.model.UserRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    private UserRole role = UserRole.USER; // Default to USER role
}
