package org.solace.scholar_ai.user_service.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solace.scholar_ai.user_service.dto.auth.AuthResponse;
import org.solace.scholar_ai.user_service.dto.auth.EmailAvailabilityDTO;
import org.solace.scholar_ai.user_service.dto.auth.EmailConfirmationDTO;
import org.solace.scholar_ai.user_service.dto.auth.EmailConfirmationStatusDTO;
import org.solace.scholar_ai.user_service.dto.auth.LoginDTO;
import org.solace.scholar_ai.user_service.dto.auth.RefreshTokenRequest;
import org.solace.scholar_ai.user_service.dto.auth.ResendEmailConfirmationDTO;
import org.solace.scholar_ai.user_service.dto.auth.SignupDTO;
import org.solace.scholar_ai.user_service.dto.response.APIResponse;
import org.solace.scholar_ai.user_service.service.auth.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@Tag(
        name = "Authentication",
        description = "Authentication endpoints for user registration, login, and token management")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @Operation(
            summary = "User Registration",
            description =
                    """
                        Register a new user account with email and password.

                        **For Swagger Testing:**
                        1. Use this endpoint to create a new account
                        2. Then use the login endpoint to get your JWT tokens
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "User registered successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Successful Registration",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 201,
                                          "message": "User registered successfully",
                                          "success": true,
                                          "data": null
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Registration failed - email might already exist or validation failed",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Registration Error",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 400,
                                          "message": "Registration failed: Email already exists",
                                          "success": false,
                                          "data": null
                                        }
                                        """)))
            })
    @PostMapping("/register")
    public ResponseEntity<APIResponse<String>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "User registration details",
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            name = "Registration Example",
                                                            value =
                                                                    """
                                        {
                                          "email": "user@example.com",
                                          "password": "securePassword123",
                                          "role": "USER"
                                        }
                                        """)))
                    @Valid
                    @RequestBody
                    SignupDTO signupDTO,
            HttpServletRequest request) {
        try {
            logger.info("/register endpoint hit with request: {}", request.getRemoteAddr());

            authService.registerUser(signupDTO.getEmail(), signupDTO.getPassword(), signupDTO.getRole());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.success(HttpStatus.CREATED.value(), "User registered successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(
                            HttpStatus.BAD_REQUEST.value(), "Registration failed: " + e.getMessage(), null));
        }
    }

    @Operation(
            summary = "User Login",
            description =
                    """
                        Authenticate user and get JWT tokens.

                        **For Swagger Testing:**
                        1. Use this endpoint to login
                        2. Copy the `accessToken` from the response
                        3. Click the 🔒 **Authorize** button at the top
                        4. Enter: `Bearer <your-access-token>`
                        5. Now you can test protected endpoints!

                        **Response includes:**
                        - `accessToken`: Use this for API authentication (expires in 15 min)
                        - `refreshToken`: Automatically stored in secure cookie (expires in 7 days)
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Login successful",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Successful Login",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Login successful",
                                          "success": true,
                                          "data": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                            "refreshToken": "stored-in-httponly-cookie",
                                            "tokenType": "Bearer",
                                            "expiresIn": 900
                                          }
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "401",
                        description = "Invalid credentials",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Credentials",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 401,
                                          "message": "Invalid email or password",
                                          "success": false,
                                          "data": null
                                        }
                                        """))),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Login credentials",
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            name = "Login Example",
                                                            value =
                                                                    """
                                        {
                                          "email": "user@example.com",
                                          "password": "your-password"
                                        }
                                        """)))
                    @Valid
                    @RequestBody
                    LoginDTO loginDTO,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            logger.info("login endpoint hit with request: {}", request.getRemoteAddr());

            AuthResponse authResponse = authService.loginUser(loginDTO.getEmail(), loginDTO.getPassword());

            logger.info("got auth response from authService for login request");

            // Create secure HttpOnly cookie for refresh token using ResponseCookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                    .httpOnly(false) // Allow JavaScript access for debugging
                    .secure(false) // Allow over plain HTTP for development
                    .sameSite("None") // Important: cross-origin cookie
                    .path("/") // Send for all paths
                    .maxAge(Duration.ofDays(7))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // Note: Refresh token is stored securely in HttpOnly cookie
            // For development/testing, you can keep it in response by commenting the next
            // line
            // authResponse.setRefreshToken(null);

            logger.info("response cookie added, authResponse: {}", authResponse);
            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Login successful", authResponse));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password", null));
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Login error: " + e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Refresh Access Token",
            description =
                    """
                        Refresh the access token using the refresh token.

                        **How it works:**
                        1. First tries to extract refresh token from request body
                        2. If not found in body, extracts from HttpOnly cookie
                        3. Validates and generates new access token
                        4. Updates refresh token in cookie
                        5. Returns new access token in response

                        **Request Body (Optional):**
                        ```json
                        {
                          "refreshToken": "your-refresh-token-here"
                        }
                        ```
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Token refreshed successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Token Refreshed",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Token refreshed successfully",
                                          "success": true,
                                          "data": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                            "tokenType": "Bearer",
                                            "expiresIn": 900
                                          }
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "401",
                        description = "Invalid or missing refresh token",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Refresh Token",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 401,
                                          "message": "Invalid refresh token",
                                          "success": false,
                                          "data": null
                                        }
                                        """))),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<AuthResponse>> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Refresh token request (optional - can also use cookie)",
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            name = "Refresh Token Example",
                                                            value =
                                                                    """
                                        {
                                          "refreshToken": "your-refresh-token-here"
                                        }
                                        """)))
                    @RequestBody(required = false)
                    RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            logger.info("refresh endpoint hit");

            // Extract refresh token from request body first, then from cookies
            String refreshToken = null;

            // Try to get refresh token from request body
            if (refreshTokenRequest != null
                    && refreshTokenRequest.getRefreshToken() != null
                    && !refreshTokenRequest.getRefreshToken().trim().isEmpty()) {
                refreshToken = refreshTokenRequest.getRefreshToken();
                logger.debug("Found refresh token in request body");
            } else {
                // Extract refresh token from cookies if not in request body
                if (request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if ("refreshToken".equals(cookie.getName())) {
                            refreshToken = cookie.getValue();
                            logger.debug("Found refresh token in cookie for user");
                            break;
                        }
                    }
                }
            }

            if (refreshToken == null) {
                logger.warn("No refresh token found in request body or cookies");
                throw new BadCredentialsException("Missing refresh token");
            }

            logger.debug("Attempting to refresh token");
            AuthResponse refreshed = authService.refreshToken(refreshToken);

            // Set new refresh token in cookie using ResponseCookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshed.getRefreshToken())
                    .httpOnly(false) // Allow JavaScript access for debugging
                    .secure(false) // Allow over plain HTTP for development
                    .sameSite("None") // Important: cross-origin cookie
                    .path("/") // Send for all paths
                    .maxAge(Duration.ofDays(7))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // Keep refresh token in response body for frontend to set cookie on its domain
            // Don't remove it - frontend needs it to set cookie on :3000 domain

            logger.info("Token refreshed successfully for user: {}", refreshed.getEmail());
            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Token refreshed successfully", refreshed));
        } catch (BadCredentialsException e) {
            logger.warn("Refresh token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token", null));
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Refresh error: " + e.getMessage(), null));
        }
    }

    @SecurityRequirement(name = "jwtAuth")
    @Operation(
            summary = "Logout User",
            description =
                    """
                        Logout user and invalidate tokens.

                        **What happens:**
                        1. Invalidates the current user's tokens
                        2. Clears the refresh token cookie
                        3. User will need to login again for new tokens
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Logged out successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Logout Success",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Logged out successfully",
                                          "success": true,
                                          "data": null
                                        }
                                        """))),
                @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @PostMapping("/logout")
    public ResponseEntity<APIResponse<String>> logout(
            HttpServletRequest request, HttpServletResponse response, Principal principal) {
        try {
            logger.info("logout endpoint hit");

            String email = principal.getName();
            authService.logoutUser(email);

            // Clear the cookie using ResponseCookie
            ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(false) // Allow JavaScript access for debugging
                    .secure(false) // Allow over plain HTTP for development
                    .sameSite("None") // Important: cross-origin cookie
                    .path("/") // Send for all paths
                    .maxAge(Duration.ofSeconds(0)) // Expire immediately
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Logged out successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Logout failed: " + e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Forgot Password",
            description =
                    """
                        Generate a password reset code for the given email.

                        **Note:** In production, this code will be sent via email/notification service.
                        For development/testing, the code is returned in the response.
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Reset code generated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Reset Code Generated",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Reset code generated successfully. Code: 123456 (This will be sent via notification service later)",
                                          "success": true,
                                          "data": "123456"
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Email not found or invalid",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Email Not Found",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 400,
                                          "message": "User not found with email: user@example.com",
                                          "success": false,
                                          "data": null
                                        }
                                        """)))
            })
    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse<String>> forgotPassword(
            @Parameter(description = "Email address for password reset", example = "user@example.com") @RequestParam
                    String email) {
        try {
            logger.info("forgot password endpoint hit with email: {}", email);

            String resetCode = authService.generateResetCode(email);
            return ResponseEntity.ok(APIResponse.success(
                    HttpStatus.OK.value(),
                    "Reset code generated successfully. Code: " + resetCode
                            + " (This will be sent via notification service later)",
                    resetCode));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Reset Password",
            description =
                    """
                        Reset user password using the reset code and new password.

                        **Process:**
                        1. Verify the reset code for the given email
                        2. Update the password if code is valid
                        3. Invalidate the reset code after use
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Password reset successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Password Reset Success",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Password reset successfully.",
                                          "success": true,
                                          "data": null
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid reset code or email",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Reset Code",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 400,
                                          "message": "Invalid reset code or code expired",
                                          "success": false,
                                          "data": null
                                        }
                                        """)))
            })
    @PostMapping("/reset-password")
    public ResponseEntity<APIResponse<String>> resetPassword(
            @Parameter(description = "Email address", example = "user@example.com") @RequestParam String email,
            @Parameter(description = "Reset code received via email", example = "123456") @RequestParam String code,
            @Parameter(description = "New password", example = "newSecurePassword123") @RequestParam
                    String newPassword) {
        try {
            logger.info("reset-password endpoint hit with email: {}, code: {}", email, code);

            authService.verifyCodeAndResetPassword(email, code, newPassword);
            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Password reset successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Confirm Email",
            description =
                    """
                        Confirm user email using the OTP code sent during registration.

                        **Process:**
                        1. Verify the OTP code for the given email
                        2. Mark email as confirmed if code is valid
                        3. Send welcome email after confirmation
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Email confirmed successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Email Confirmation Success",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Email confirmed successfully. Welcome email sent.",
                                          "success": true,
                                          "data": null
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid OTP code or email already confirmed",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid OTP",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 400,
                                          "message": "Invalid or expired verification code",
                                          "success": false,
                                          "data": null
                                        }
                                        """)))
            })
    @PostMapping("/confirm-email")
    public ResponseEntity<APIResponse<String>> confirmEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Email confirmation details",
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            name = "Email Confirmation Example",
                                                            value =
                                                                    """
                                        {
                                          "email": "user@example.com",
                                          "otp": "123456"
                                        }
                                        """)))
                    @Valid
                    @RequestBody
                    EmailConfirmationDTO emailConfirmationDTO) {
        try {
            logger.info("confirm-email endpoint hit with email: {}", emailConfirmationDTO.getEmail());

            authService.confirmEmail(emailConfirmationDTO.getEmail(), emailConfirmationDTO.getOtp());
            return ResponseEntity.ok(APIResponse.success(
                    HttpStatus.OK.value(), "Email confirmed successfully. Welcome email sent.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Resend Email Verification",
            description =
                    """
                        Resend email verification OTP to the user's email address.

                        **Use case:** When the original verification email expires or user didn't receive it.
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Verification email resent successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Email Resent",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Verification email sent successfully",
                                          "success": true,
                                          "data": null
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Email not found or already confirmed",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Email Not Found",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 400,
                                          "message": "No user with that email",
                                          "success": false,
                                          "data": null
                                        }
                                        """)))
            })
    @PostMapping("/resend-email-verification")
    public ResponseEntity<APIResponse<String>> resendEmailVerification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Resend email verification request",
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            name = "Resend Verification Example",
                                                            value =
                                                                    """
                                        {
                                          "email": "user@example.com"
                                        }
                                        """)))
                    @Valid
                    @RequestBody
                    ResendEmailConfirmationDTO resendEmailConfirmationDTO) {
        try {
            logger.info("resend-email-verification endpoint hit with email: {}", resendEmailConfirmationDTO.getEmail());

            authService.resendEmailVerification(resendEmailConfirmationDTO.getEmail());
            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Verification email sent successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Check Email Confirmation Status",
            description =
                    """
                        Check if a user exists and whether their email is confirmed.

                        **Response includes:**
                        - `userExists`: Whether a user with this email exists
                        - `isEmailConfirmed`: Whether the email is confirmed (only if user exists)
                        - `email`: The email address being checked
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Email status retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Email Status",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Email status retrieved successfully",
                                          "success": true,
                                          "data": {
                                            "email": "user@example.com",
                                            "isEmailConfirmed": true,
                                            "userExists": true
                                          }
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid email format",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Email",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 400,
                                          "message": "Invalid email format",
                                          "success": false,
                                          "data": null
                                        }
                                        """)))
            })
    @GetMapping("/check-email-status")
    public ResponseEntity<APIResponse<EmailConfirmationStatusDTO>> checkEmailStatus(
            @Parameter(description = "Email address to check", example = "user@example.com") @RequestParam
                    String email) {
        try {
            logger.info("check-email-status endpoint hit with email: {}", email);

            // Basic email validation
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid email format", null));
            }

            EmailConfirmationStatusDTO status = authService.checkEmailConfirmationStatus(email.trim());
            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Email status retrieved successfully", status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Check Email Availability",
            description =
                    """
                        Check if an email address is available for registration.

                        **Checks both:**
                        - Regular user accounts (users table)
                        - Social login accounts (user_identity_providers table)

                        **Response:**
                        - `isAvailable: true` if email can be used for registration
                        - `isAvailable: false` if email is already taken
                        """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Email availability checked successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Email Available",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 200,
                                          "message": "Email availability checked successfully",
                                          "success": true,
                                          "data": {
                                            "email": "newuser@example.com",
                                            "isAvailable": true
                                          }
                                        }
                                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid email format",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Email",
                                                        value =
                                                                """
                                        {
                                          "statusCode": 400,
                                          "message": "Invalid email format",
                                          "success": false,
                                          "data": null
                                        }
                                        """)))
            })
    @GetMapping("/check-email-availability")
    public ResponseEntity<APIResponse<EmailAvailabilityDTO>> checkEmailAvailability(
            @Parameter(description = "Email address to check", example = "newuser@example.com") @RequestParam
                    String email) {
        try {
            logger.info("check-email-availability endpoint hit with email: {}", email);

            // Basic email validation
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid email format", null));
            }

            boolean isAvailable = authService.isEmailAvailable(email.trim());
            EmailAvailabilityDTO availability = EmailAvailabilityDTO.builder()
                    .email(email.trim())
                    .isAvailable(isAvailable)
                    .build();

            return ResponseEntity.ok(APIResponse.success(
                    HttpStatus.OK.value(), "Email availability checked successfully", availability));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }
}
