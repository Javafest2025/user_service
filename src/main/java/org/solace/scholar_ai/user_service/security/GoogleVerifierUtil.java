package org.solace.scholar_ai.user_service.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleVerifierUtil {

    @Value("${spring.google.client-id}")
    private String clientId;

    private GoogleIdTokenVerifier verifier;

    // Added for testability
    public GoogleVerifierUtil(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    public GoogleVerifierUtil() {
        // Default constructor for Spring
    }

    @PostConstruct
    public void init() throws Exception {
        if (this.verifier == null) {
            this.verifier = new GoogleIdTokenVerifier.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
        }
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            return (idToken != null) ? idToken.getPayload() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
