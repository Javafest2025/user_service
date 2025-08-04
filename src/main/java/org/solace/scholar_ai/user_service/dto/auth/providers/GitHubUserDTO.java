package org.solace.scholar_ai.user_service.dto.auth.providers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubUserDTO {
    private Long id;
    private String login;
    private String name;
    private String email;
}
