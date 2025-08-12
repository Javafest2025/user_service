package org.solace.scholar_ai.user_service.service.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.solace.scholar_ai.user_service.dto.auth.AuthResponse;
import org.solace.scholar_ai.user_service.dto.auth.EmailConfirmationStatusDTO;
import org.solace.scholar_ai.user_service.model.User;
import org.solace.scholar_ai.user_service.model.UserProfile;
import org.solace.scholar_ai.user_service.model.UserRole;
import org.solace.scholar_ai.user_service.repository.UserIdentityProviderRepository;
import org.solace.scholar_ai.user_service.repository.UserProfileRepository;
import org.solace.scholar_ai.user_service.repository.UserRepository;
import org.solace.scholar_ai.user_service.security.JwtUtils;
import org.solace.scholar_ai.user_service.service.notification.NotificationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserIdentityProviderRepository userIdentityProviderRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserLoadingService userLoadingService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;

    public Authentication authentication(String email, String password) {
        UserDetails userDetails = userLoadingService.loadUserByUsername(email);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid email ...");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // register new user
    public void registerUser(String email, String password, UserRole role) {
        // if exists in users table, not allowed
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadCredentialsException("User with email " + email + " already exists.");
        } else if (userIdentityProviderRepository.findByUserEmail(email).isPresent()) {
            throw new BadCredentialsException("This " + email
                    + " is already registered via Google/Github login. Please use social auth to continue.");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setEncryptedPassword(passwordEncoder.encode(password));
        newUser.setRole(role != null ? role : UserRole.USER); // Use provided role or default to USER
        newUser.setEmailConfirmed(false);
        newUser.setCreatedAt(Instant.now());
        newUser.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(newUser);

        // Create empty user profile
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(savedUser);
        userProfile.setCreatedAt(Instant.now());
        userProfile.setUpdatedAt(Instant.now());

        userProfileRepository.save(userProfile);

        // Send email verification with OTP
        try {
            String verificationCode = generateVerificationCode(email);
            notificationService.sendEmailVerificationEmail(email, email.split("@")[0], verificationCode);
        } catch (Exception e) {
            // Log error but don't fail registration
            // In production, you might want to implement retry logic
        }
    }

    // login registered user
    public AuthResponse loginUser(String email, String password) {
        Authentication authentication = authentication(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(userDetails.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
        refreshTokenService.saveRefreshToken(userDetails.getUsername(), refreshToken);

        // if user exists in social_users, then not allow email, pass login
        if (userIdentityProviderRepository.findByUserEmail(email).isPresent()) {
            throw new BadCredentialsException("This " + email
                    + " is already registered via Google/Github login. Please use social auth to continue.");
        }

        User user =
                userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid email ..."));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new AuthResponse(accessToken, refreshToken, userDetails.getUsername(), user.getId(), user.getRole());
    }

    // refresh access token when access token expires
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new BadCredentialsException("Refresh token is null or empty");
        }

        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token - JWT validation failed");
        }

        String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
        if (username == null || username.trim().isEmpty()) {
            throw new BadCredentialsException("Invalid refresh token - username extraction failed");
        }

        if (!refreshTokenService.isRefreshTokenValid(username, refreshToken)) {
            throw new BadCredentialsException("Refresh token is not recognized or has expired");
        }

        String newAccessToken = jwtUtils.generateAccessToken(username);
        String newRefreshToken = refreshToken;
        refreshTokenService.saveRefreshToken(username, newRefreshToken);

        User user =
                userRepository.findByEmail(username).orElseThrow(() -> new BadCredentialsException("Invalid Email..."));

        List<String> roles = userLoadingService.loadUserByUsername(username).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new AuthResponse(newAccessToken, newRefreshToken, username, user.getId(), user.getRole());
    }

    // Logout user
    public void logoutUser(String username) {
        refreshTokenService.deleteRefreshToken(username);
    }

    // Forgot Password: generate and store reset code
    public String generateResetCode(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("No user with that email."));

        String code = String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6-digit code
        String redisKey = "RESET_CODE:" + email;
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(10)); // expires in 10 min

        // Send password reset email via notification service
        try {
            notificationService.sendPasswordResetEmail(email, email.split("@")[0], code);
        } catch (Exception e) {
            // Log error but don't fail the operation
            // In production, you might want to implement retry logic
        }

        return code; // Still return for testing purposes
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // Reset Password: verify code and update password
    public void verifyCodeAndResetPassword(String email, String code, String newPassword) {
        String redisKey = "RESET_CODE:" + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null || !storedCode.equals(code)) {
            throw new IllegalArgumentException("Invalid or expired reset code");
        }

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found by this email!"));

        String encoded = passwordEncoder.encode(newPassword);
        user.setEncryptedPassword(encoded);
        user.setUpdatedAt(Instant.now());
        userRepository.saveAndFlush(user);

        redisTemplate.delete(redisKey); // invalidate used code
        refreshTokenService.deleteRefreshToken(email);
    }

    // Generate email verification code
    public String generateVerificationCode(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("No user with that email."));

        if (user.isEmailConfirmed()) {
            throw new IllegalArgumentException("Email is already confirmed");
        }

        String code = String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6-digit code
        String redisKey = "VERIFICATION_CODE:" + email;
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(10)); // expires in 10 min

        return code;
    }

    // Confirm email with OTP
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmEmail(String email, String otp) {
        String redisKey = "VERIFICATION_CODE:" + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null || !storedCode.equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found by this email!"));

        if (user.isEmailConfirmed()) {
            throw new IllegalArgumentException("Email is already confirmed");
        }

        user.setEmailConfirmed(true);
        user.setUpdatedAt(Instant.now());
        userRepository.saveAndFlush(user);

        redisTemplate.delete(redisKey); // invalidate used code

        // Send welcome email after confirmation
        try {
            notificationService.sendWelcomeEmail(email, email.split("@")[0]);
        } catch (Exception e) {
            // Log error but don't fail confirmation
        }
    }

    // Resend email verification
    public void resendEmailVerification(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("No user with that email."));

        if (user.isEmailConfirmed()) {
            throw new IllegalArgumentException("Email is already confirmed");
        }

        String verificationCode = generateVerificationCode(email);
        notificationService.sendEmailVerificationEmail(email, email.split("@")[0], verificationCode);
    }

    // Check email confirmation status
    public EmailConfirmationStatusDTO checkEmailConfirmationStatus(String email) {
        return userRepository
                .findByEmail(email)
                .map(user -> EmailConfirmationStatusDTO.builder()
                        .email(email)
                        .isEmailConfirmed(user.isEmailConfirmed())
                        .userExists(true)
                        .build())
                .orElse(EmailConfirmationStatusDTO.builder()
                        .email(email)
                        .isEmailConfirmed(false)
                        .userExists(false)
                        .build());
    }

    // Check if email is available for registration
    public boolean isEmailAvailable(String email) {
        // Check if email exists in users table
        boolean existsInUsers = userRepository.findByEmail(email).isPresent();

        // Check if email exists in social users table
        boolean existsInSocialUsers =
                userIdentityProviderRepository.findByUserEmail(email).isPresent();

        // Email is available if it doesn't exist in either table
        return !existsInUsers && !existsInSocialUsers;
    }
}
