package org.solace.scholar_ai.user_service.dto.auth.providers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubEmailDTO {
    private String email;
    private boolean primary;
    private boolean verified;
}
