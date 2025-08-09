package org.solace.scholar_ai.user_service.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solace.scholar_ai.user_service.dto.auth.AuthResponse;
import org.solace.scholar_ai.user_service.dto.auth.providers.GitHubEmailDTO;
import org.solace.scholar_ai.user_service.dto.auth.providers.GitHubUserDTO;
import org.solace.scholar_ai.user_service.model.User;
import org.solace.scholar_ai.user_service.model.UserIdentityProvider;
import org.solace.scholar_ai.user_service.model.UserProfile;
import org.solace.scholar_ai.user_service.model.UserRole;
import org.solace.scholar_ai.user_service.repository.UserIdentityProviderRepository;
import org.solace.scholar_ai.user_service.repository.UserProfileRepository;
import org.solace.scholar_ai.user_service.repository.UserRepository;
import org.solace.scholar_ai.user_service.security.GoogleVerifierUtil;
import org.solace.scholar_ai.user_service.security.JwtUtils;
import org.solace.scholar_ai.user_service.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SocialAuthService {
    private static final Logger logger = LoggerFactory.getLogger(SocialAuthService.class);
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserIdentityProviderRepository userIdentityProviderRepository;
    private final UserProfileRepository userProfileRepository;
    private final GoogleVerifierUtil googleVerifierUtil;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Value("${spring.github.client-id}")
    private String githubClientId;

    @Value("${spring.github.client-secret}")
    private String githubClientSecret;

    @Value("${spring.github.redirect-uri}")
    private String githubRedirectUri;

    public AuthResponse loginWithGoogle(String idTokenString) {
        GoogleIdToken.Payload payload = googleVerifierUtil.verify(idTokenString);

        if (payload == null) {
            throw new BadCredentialsException("Invalid Google ID token");
        }

        String email = payload.getEmail();
        String providerId = payload.getSubject();
        String name = (String) payload.get("name");

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in Google ID token payload.");
        }

        // Check if user exists with this email
        if (userRepository.findByEmail(email).isPresent()) {
            User existingUser = userRepository.findByEmail(email).get();

            // Check if user has Google identity provider
            if (userIdentityProviderRepository
                    .findByUserEmailAndProvider(email, "GOOGLE")
                    .isPresent()) {
                return buildTokensForUser(existingUser);
            } else {
                // User exists but not with Google - check if they have other providers
                if (userIdentityProviderRepository.findByUserEmail(email).isPresent()) {
                    throw new BadCredentialsException(
                            "This email is already registered with another provider. Please use the original login method.");
                } else {
                    // User exists with password - not allowed to use Google
                    throw new BadCredentialsException(
                            "This email is registered with a password. Please log in using email and password.");
                }
            }
        }

        // Create new user
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setEncryptedPassword(encodedPassword);
        newUser.setRole(UserRole.USER);
        newUser.setEmailConfirmed(true); // Google emails are verified
        newUser.setCreatedAt(Instant.now());
        newUser.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(newUser);

        // Create Google identity provider mapping
        UserIdentityProvider identityProvider = new UserIdentityProvider();
        identityProvider.setUser(savedUser);
        identityProvider.setProvider("GOOGLE");
        identityProvider.setProviderUserId(providerId);
        identityProvider.setCreatedAt(Instant.now());
        identityProvider.setUpdatedAt(Instant.now());

        userIdentityProviderRepository.save(identityProvider);

        // Create empty user profile
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(savedUser);
        userProfile.setCreatedAt(Instant.now());
        userProfile.setUpdatedAt(Instant.now());
        userProfileRepository.save(userProfile);

        // Send welcome notification for new user
        try {
            String userName = name != null && !name.isEmpty() ? name : email.split("@")[0];
            notificationService.sendWelcomeEmail(email, userName);
            logger.info("Welcome notification sent successfully for new Google user: {}", email);
        } catch (Exception e) {
            // Log error but don't fail the registration process
            // In production, you might want to implement retry logic
            logger.error("Failed to send welcome notification for new Google user: {}", email, e);
        }

        // Generate tokens
        String accessToken = jwtUtils.generateAccessToken(savedUser.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(savedUser.getEmail());
        refreshTokenService.saveRefreshToken(savedUser.getEmail(), refreshToken);

        return new AuthResponse(
                accessToken, refreshToken, savedUser.getEmail(), savedUser.getId(), savedUser.getRole());
    }

    // login with github
    public AuthResponse loginWithGithub(String code) {
        // Exchange code for access token
        String accessToken = exchangeCodeForAccessToken(code);

        // Get github user profile
        GitHubUserDTO gitHubUser = fetchGitHubUser(accessToken);

        String email = gitHubUser.getEmail();
        String providerId = gitHubUser.getId().toString();
        String name = gitHubUser.getName() != null ? gitHubUser.getName() : gitHubUser.getLogin();

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in Github user profile");
        }

        // Check if user exists with this email
        if (userRepository.findByEmail(email).isPresent()) {
            User existingUser = userRepository.findByEmail(email).get();

            // Check if user has GitHub identity provider
            if (userIdentityProviderRepository
                    .findByUserEmailAndProvider(email, "GITHUB")
                    .isPresent()) {
                return buildTokensForUser(existingUser);
            } else {
                // User exists but not with GitHub - check if they have other providers
                if (userIdentityProviderRepository.findByUserEmail(email).isPresent()) {
                    throw new BadCredentialsException(
                            "This email is already registered with another provider. Please use the original login method.");
                } else {
                    // User exists with password - not allowed to use GitHub
                    throw new BadCredentialsException("This email is already registered with a password!");
                }
            }
        }

        // Create new user
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setEncryptedPassword(encodedPassword);
        newUser.setRole(UserRole.USER);
        newUser.setEmailConfirmed(true); // GitHub emails are verified
        newUser.setCreatedAt(Instant.now());
        newUser.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(newUser);

        // Create GitHub identity provider mapping
        UserIdentityProvider identityProvider = new UserIdentityProvider();
        identityProvider.setUser(savedUser);
        identityProvider.setProvider("GITHUB");
        identityProvider.setProviderUserId(providerId);
        identityProvider.setCreatedAt(Instant.now());
        identityProvider.setUpdatedAt(Instant.now());

        userIdentityProviderRepository.save(identityProvider);

        // Create empty user profile
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(savedUser);
        userProfile.setCreatedAt(Instant.now());
        userProfile.setUpdatedAt(Instant.now());
        userProfileRepository.save(userProfile);

        // Send welcome notification for new user
        try {
            String userName = name != null && !name.isEmpty() ? name : email.split("@")[0];
            notificationService.sendWelcomeEmail(email, userName);
            logger.info("Welcome notification sent successfully for new GitHub user: {}", email);
        } catch (Exception e) {
            // Log error but don't fail the registration process
            // In production, you might want to implement retry logic
            logger.error("Failed to send welcome notification for new GitHub user: {}", email, e);
        }

        // Generate tokens
        String jwtAccessToken = jwtUtils.generateAccessToken(savedUser.getEmail());
        String jwtRefreshToken = jwtUtils.generateRefreshToken(savedUser.getEmail());
        refreshTokenService.saveRefreshToken(savedUser.getEmail(), jwtRefreshToken);

        return new AuthResponse(
                jwtAccessToken, jwtRefreshToken, savedUser.getEmail(), savedUser.getId(), savedUser.getRole());
    }

    // exchange code for access token
    public String exchangeCodeForAccessToken(String code) {
        String url = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", githubClientId);
        body.add("client_secret", githubClientSecret);
        body.add("code", code);
        body.add("redirect_uri", githubRedirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            // Log the error response from GitHub if available
            if (response.getBody() != null && response.getBody().containsKey("error_description")) {
                logger.error(
                        "GitHub token exchange failed: {}", response.getBody().get("error_description"));
            }
            throw new IllegalStateException("Failed to get access token from GitHub");
        }

        return (String) response.getBody().get("access_token");
    }

    // fetch github user
    private GitHubUserDTO fetchGitHubUser(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GitHubUserDTO> response =
                restTemplate.exchange("https://api.github.com/user", HttpMethod.GET, entity, GitHubUserDTO.class);

        GitHubUserDTO userDTO = response.getBody();

        // Fetch email if null
        if (userDTO.getEmail() == null) {
            ResponseEntity<GitHubEmailDTO[]> emailResponse = restTemplate.exchange(
                    "https://api.github.com/user/emails", HttpMethod.GET, entity, GitHubEmailDTO[].class);

            for (GitHubEmailDTO mail : emailResponse.getBody()) {
                if (mail.isPrimary() && mail.isVerified()) {
                    userDTO.setEmail(mail.getEmail());
                    break;
                }
            }
        }

        return userDTO;
    }

    private AuthResponse buildTokensForUser(User user) {
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getId(), user.getRole());
    }
}
