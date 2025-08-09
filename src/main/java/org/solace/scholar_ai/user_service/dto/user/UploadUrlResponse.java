package org.solace.scholar_ai.user_service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadUrlResponse {
    private String putUrl;
    private String key;
    private String publicUrl;
}
