package org.solace.scholar_ai.user_service.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitAvatarRequest {
    @NotBlank(message = "Key is required")
    private String key;

    @NotBlank(message = "ETag is required")
    private String etag;
}
