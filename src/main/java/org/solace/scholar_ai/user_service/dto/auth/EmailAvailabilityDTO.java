package org.solace.scholar_ai.user_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAvailabilityDTO {

    private String email;
    private boolean isAvailable;
}
