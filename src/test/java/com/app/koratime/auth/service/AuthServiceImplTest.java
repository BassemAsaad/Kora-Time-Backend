package com.app.koratime.auth.service;

import com.app.koratime.auth.dto.*;
import com.app.koratime.auth.model.EmailVerificationToken;
import com.app.koratime.auth.model.PasswordResetToken;
import com.app.koratime.auth.repo.EmailVerificationTokenRepo;
import com.app.koratime.auth.repo.PasswordResetTokenRepo;
import com.app.koratime.common.exception.BusinessViolatedException;
import com.app.koratime.common.exception.DuplicateResourceException;
import com.app.koratime.common.exception.ResourceNotFoundException;
import com.app.koratime.common.security.JwtService;
import com.app.koratime.common.security.UserPrincipal;
import com.app.koratime.email.EmailService;
import com.app.koratime.user.dto.UserSummary;
import com.app.koratime.user.model.ManagerProfile;
import com.app.koratime.user.model.PlayerProfile;
import com.app.koratime.user.model.User;
import com.app.koratime.user.model.Role;
import com.app.koratime.user.repo.ManagerProfileRepo;
import com.app.koratime.user.repo.PlayerProfileRepo;
import com.app.koratime.user.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
public class AuthServiceImplTest {

    @Mock
    private UserRepo userRepository;
    @Mock
    private PlayerProfileRepo playerProfileRepository;
    @Mock
    private ManagerProfileRepo managerProfileRepository;
    @Mock
    private EmailVerificationTokenRepo emailTokenRepository;
    @Mock
    private PasswordResetTokenRepo resetTokenRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private RegisterRequest playerRequest;
    private RegisterRequest managerRequest;
    private User user;
    private EmailVerificationToken verificationToken;
    LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        playerRequest = RegisterRequest.builder()
                .email("player@test.com")
                .password("password123")
                .firstName("Ali")
                .lastName("Hassan")
                .phoneNumber("01012345678")
                .role(Role.PLAYER)
                .build();

        managerRequest = RegisterRequest.builder()
                .email("manager@test.com")
                .password("password123")
                .firstName("Omar")
                .lastName("Ahmed")
                .nationalId("29901011234567")
                .role(Role.MANAGER)
                .build();

        verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token("verification-token")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        user = User.builder()
                .id(UUID.fromString("facade00-0000-4000-a000-000000000000"))
                .email("player@test.com")
                .password("password123")
                .firstName("Ali")
                .lastName("Hassan")
                .build();

        loginRequest = new LoginRequest("player@test.com", "password123");
    }

    @Test
    @DisplayName("Register player")
    void register_player_happyPath() {

        // arrange
        user.setRole(Role.PLAYER);

        when(userRepository.existsByEmail(playerRequest.email()))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(emailTokenRepository.save(any(EmailVerificationToken.class)))
                .thenReturn(verificationToken);

        when(jwtService.generateAccessToken(any()))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(any()))
                .thenReturn("refresh-token");

        PlayerProfile playerProfile = PlayerProfile.builder()
                .user(user)
                .build();

        when(playerProfileRepository.save(any(PlayerProfile.class)))
            .thenReturn(playerProfile);

        // act
        AuthResponse authResponse = authServiceImpl.register(playerRequest);

        // assert
        assertThat(authResponse.getAccessToken())
                .isEqualTo("access-token");

        assertThat(authResponse.getRefreshToken())
                .isEqualTo("refresh-token");

        assertThat(authResponse.getUser().email())
                .isEqualTo("player@test.com");

        assertThat(user.getRole())
                .isEqualTo(Role.PLAYER);

        // verify
        verify(userRepository, times(1)).existsByEmail(playerRequest.email());
        verify(userRepository,times(1)).save(any(User.class));
        verify(playerProfileRepository,times(1)).save(any(PlayerProfile.class));

        verify(emailTokenRepository,times(1)).save(any(EmailVerificationToken.class));
        verify(emailService, times(1))
                .sendVerificationEmail(anyString(), anyString(), anyString());

        verify(jwtService,times(1)).generateAccessToken(any());
        verify(jwtService,times(1)).generateRefreshToken(any());

        verify(passwordEncoder,times(1)).encode(playerRequest.password());

        verifyNoInteractions(managerProfileRepository);
    }

    @Test
    @DisplayName("Register manager")
    void register_manager_happyPath() {

        // arrange
        user.setRole(Role.MANAGER);

        when(userRepository.existsByEmail(managerRequest.email()))
                .thenReturn(false);

        when(managerProfileRepository.existsByNationalId(managerRequest.nationalId()))
                .thenReturn(false);


        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(emailTokenRepository.save(any(EmailVerificationToken.class)))
                .thenReturn(verificationToken);

        when(jwtService.generateAccessToken(any()))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(any()))
                .thenReturn("refresh-token");

        ManagerProfile managerProfile = ManagerProfile.builder()
                .user(user)
                .nationalId("1234567890123456")
                .build();
        when(managerProfileRepository.save(any(ManagerProfile.class)))
                .thenReturn(managerProfile);

        // act
        AuthResponse authResponse = authServiceImpl.register(managerRequest);

        // assert
        assertThat(authResponse.getAccessToken())
                .isEqualTo("access-token");

        assertThat(authResponse.getRefreshToken())
                .isEqualTo("refresh-token");

        assertThat(authResponse.getUser().email())
                .isEqualTo("manager@test.com");

        assertThat(user.getRole())
                .isEqualTo(Role.MANAGER);


        // verify
        verify(userRepository, times(1)).existsByEmail(managerRequest.email());
        verify(userRepository,times(1)).save(any(User.class));
        verify(managerProfileRepository,times(1)).save(any(ManagerProfile.class));

        verify(emailTokenRepository,times(1)).save(any(EmailVerificationToken.class));
        verify(emailService, times(1))
                .sendVerificationEmail(anyString(), anyString(), anyString());

        verify(jwtService,times(1)).generateAccessToken(any());
        verify(jwtService,times(1)).generateRefreshToken(any());

        verify(passwordEncoder, times(1)).encode(managerRequest.password());
        verifyNoInteractions(playerProfileRepository);
    }

    @Test
    @DisplayName("Register throws duplicate exception when email taken")
    void register_throwsDuplicate_whenEmailTaken() {

        // arrange
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        // act + assert
        assertThatThrownBy(() -> authServiceImpl.register(playerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User already exists with email: "+playerRequest.email());

        verify(userRepository,times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(managerProfileRepository, playerProfileRepository,
                emailTokenRepository, passwordEncoder, emailService);
    }

    @Test
    @DisplayName("Register throws duplicate exception when nationalId taken")
    void register_throwsDuplicate_whenNationalIdTaken() {
        // arrange
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(managerProfileRepository.existsByNationalId(anyString()))
                .thenReturn(true);

        // act + assert
        assertThatThrownBy(() -> authServiceImpl.register(managerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Manager already exists with nationalId: "+managerRequest.nationalId());

        verify(userRepository,times(1)).existsByEmail(anyString());
        verify(managerProfileRepository, times(1)).existsByNationalId(anyString());
        verify(managerProfileRepository, never()).save(any(ManagerProfile.class));
        verifyNoInteractions(playerProfileRepository, emailTokenRepository, passwordEncoder, emailService);
    }

    @Test
    @DisplayName("Register throws BusinessViolated exception when role is ADMIN")
    void register_throwsBusinessViolated_whenRoleIsAdmin() {

        // arrange
        RegisterRequest adminRequest = RegisterRequest.builder()
                .email("admin@test.com")
                .password("password123")
                .firstName("Omar")
                .lastName("Ahmed")
                .nationalId("29901011234567")
                .role(Role.ADMIN)
                .build();

        // act + assert
        assertThatThrownBy(() -> authServiceImpl.register(adminRequest))
                .isInstanceOf(BusinessViolatedException.class)
                .hasMessageContaining("Admin accounts cannot be self-registered");

        verifyNoInteractions(userRepository, managerProfileRepository, playerProfileRepository,
                emailTokenRepository, passwordEncoder, emailService);
    }

    @Test
    @DisplayName("Login user")
    void login_user_happyPath() {

        // arrange
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

        Authentication authentication = mock(Authentication.class);

        UserPrincipal principal = UserPrincipal.from(user);

        UserSummary userSummary = UserSummary.from(user);

        when(authenticationManager.authenticate(authToken))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(principal);

        when(userRepository.findById(principal.getId()))
                .thenReturn(Optional.ofNullable(user));

        when(jwtService.generateAccessToken(any()))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(any()))
                .thenReturn("refresh-token");
        // act
        AuthResponse response = authServiceImpl.login(loginRequest);

        // assert
        assertThat(response.getAccessToken())
                .isEqualTo("access-token");
        assertThat(response.getRefreshToken())
                .isEqualTo("refresh-token");
        assertThat(response.getUser())
                .isEqualTo(userSummary);

        verify(authenticationManager, times(1)).authenticate(authToken);
        verify(userRepository, times(1)).findById(principal.getId());
    }

    @Test
    @DisplayName("Login throws BadCredentialsException when bad credentials")
    void login_throwsBadCredentialsException_whenBadCredentials() {

        // arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // act + assert
        assertThatThrownBy(() -> authServiceImpl.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userRepository);

    }

    @Test
    @DisplayName("RefreshToken create new accessToken")
    void refreshToken_createNewAccessToken_happyPath() {

        RefreshTokenRequest tokenRequest = new RefreshTokenRequest("refresh-token");
        when(jwtService.isRefreshToken(tokenRequest.refreshToken()))
                .thenReturn(true);
        when(jwtService.isTokenValid(tokenRequest.refreshToken()))
                .thenReturn(true);
        when(jwtService.extractEmail(tokenRequest.refreshToken()))
                .thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any()))
                .thenReturn("new-access-token");
        UserSummary userSummary = UserSummary.from(user);

        AuthResponse response = authServiceImpl.refreshToken(tokenRequest);

        assertThat(response.getAccessToken())
                .isEqualTo("new-access-token");
        assertThat(response.getRefreshToken())
                .isEqualTo(tokenRequest.refreshToken());
        assertThat(response.getUser()).isEqualTo(userSummary);

        verify(jwtService, times(1)).isRefreshToken(tokenRequest.refreshToken());
        verify(jwtService, times(1)).isTokenValid(tokenRequest.refreshToken());
        verify(jwtService, times(1)).extractEmail(tokenRequest.refreshToken());
        verify(jwtService, times(1)).generateAccessToken(any());
        verify(userRepository, times(1)).findByEmail(anyString());

        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("RefreshToken throwBusinessViolatedException when not refreshToken")
    void refreshToken_throwsBusinessViolatedException_whenNotRefreshToken() {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest("refresh-token");
        when(jwtService.isRefreshToken(tokenRequest.refreshToken()))
                .thenReturn(false);

        assertThatThrownBy(() -> authServiceImpl.refreshToken(tokenRequest))
                .isInstanceOf(BusinessViolatedException.class)
                .hasMessageContaining("Provided token is not a refresh token");

        verify(jwtService, times(1)).isRefreshToken(tokenRequest.refreshToken());

        verify(jwtService, never()).isTokenValid(tokenRequest.refreshToken());
        verify(jwtService, never()).extractEmail(tokenRequest.refreshToken());
        verify(jwtService, never()).generateAccessToken(any());
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("RefreshToken throwBusinessViolatedException when not valid refreshToken")
    void refreshToken_throwsBusinessViolatedException_whenInvalidRefreshToken() {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest("refresh-token");
        when(jwtService.isRefreshToken(tokenRequest.refreshToken()))
                .thenReturn(true);
        when(jwtService.isTokenValid(tokenRequest.refreshToken()))
                .thenReturn(false);

        assertThatThrownBy(() -> authServiceImpl.refreshToken(tokenRequest))
                .isInstanceOf(BusinessViolatedException.class)
                .hasMessageContaining("Refresh token is invalid or expired");

        verify(jwtService, times(1)).isRefreshToken(tokenRequest.refreshToken());
        verify(jwtService, times(1)).isTokenValid(tokenRequest.refreshToken());
        verify(jwtService, never()).extractEmail(tokenRequest.refreshToken());
        verify(jwtService, never()).generateAccessToken(any());
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("RefreshToken throwResourceNotFoundException when user not found")
    void  refreshToken_throwsResourceNotFoundException_whenUserNotFound() {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest("refresh-token");
        when(jwtService.isRefreshToken(tokenRequest.refreshToken()))
                .thenReturn(true);
        when(jwtService.isTokenValid(tokenRequest.refreshToken()))
                .thenReturn(true);
        when(jwtService.extractEmail(tokenRequest.refreshToken()))
                .thenReturn("user@test.com");
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authServiceImpl.refreshToken(tokenRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(jwtService, times(1)).isRefreshToken(tokenRequest.refreshToken());
        verify(jwtService, times(1)).isTokenValid(tokenRequest.refreshToken());
        verify(jwtService, times(1)).extractEmail(tokenRequest.refreshToken());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(jwtService, never()).generateAccessToken(any());
        verify(jwtService,  never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("VerifyEmail")
    void verifyEmail_happyPath() {
        // arrange
        VerifyEmailRequest request = new VerifyEmailRequest("token");
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(request.token())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        when(emailTokenRepository.findByToken(request.token()))
                .thenReturn(Optional.ofNullable(emailVerificationToken));

        // act
        authServiceImpl.verifyEmail(request);

        // assert
        assertTrue(emailVerificationToken.isUsed());
        assertTrue(user.isEmailVerified());

        verify(emailTokenRepository, times(1)).findByToken(request.token());
        verify(emailTokenRepository, times(1)).save(emailVerificationToken);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("VerifyEmail throwsBusinessViolatedException when token invalid")
    void verifyEmail_throwsBusinessViolatedException_whenInvalidToken() {
        VerifyEmailRequest request = new VerifyEmailRequest("token");
        when(emailTokenRepository.findByToken(request.token()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authServiceImpl.verifyEmail(request))
                .hasMessageContaining("Verification link is invalid")
                .isInstanceOf(BusinessViolatedException.class);

        verify(emailTokenRepository, times(1)).findByToken(request.token());
        verifyNoInteractions(userRepository);
        verify(emailTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("VerifyEmail throwsBusinessViolatedException when token used")
    void verifyEmail_throwsBusinessViolatedException_whenTokenUsed() {
        VerifyEmailRequest request = new VerifyEmailRequest("token");
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(request.token())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isUsed(true)
                .build();

        when(emailTokenRepository.findByToken(request.token()))
                .thenReturn(Optional.ofNullable(emailVerificationToken));

        assertThatThrownBy(() -> authServiceImpl.verifyEmail(request))
                .hasMessageContaining("Verification link has expired or was already used. Request a new one.")
                .isInstanceOf(BusinessViolatedException.class);

        assertTrue(emailVerificationToken.isUsed());
        assertFalse(user.isEmailVerified());

        verify(emailTokenRepository, times(1)).findByToken(request.token());
        verifyNoInteractions(userRepository);
        verify(emailTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("ResendVerificationEmail")
    void resendVerificationEmail_happyPath() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        EmailVerificationToken emailToken = EmailVerificationToken.builder()
                .user(user)
                .token("token")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        when(emailTokenRepository.save(any(EmailVerificationToken.class)))
                .thenReturn(emailToken);

        authServiceImpl.resendVerificationEmail(user.getEmail());

        assertFalse(user.isEmailVerified());
        assertFalse(emailToken.isUsed());

        verify(emailTokenRepository, times(1)).invalidateAllTokens(user.getId());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(emailTokenRepository, times(1)).save(any(EmailVerificationToken.class));
        verify(emailService, times(1))
                .sendVerificationEmail(user.getEmail(), user.getFirstName(), emailToken.getToken());

    }

    @Test
    @DisplayName("ResendVerificationEmail throwsResourceNotFoundException when user not found")
    void resendVerificationEmail_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("wrongEmail"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authServiceImpl.resendVerificationEmail("wrongEmail"))
                .hasMessageContaining("User not found with email: wrongEmail")
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, times(1)).findByEmail("wrongEmail");
        verify(emailTokenRepository, never()).save(any(EmailVerificationToken.class));
        verify(emailService, never()).sendVerificationEmail("wrongEmail", user.getFirstName(), user.getEmail());
    }

    @Test
    @DisplayName("ResendVerificationEmail throwsBusinessViolatedException when email verified")
    void resendVerificationEmail_throwsBusinessViolatedException_whenEmailVerified() {
        user.setEmailVerified(true);
        when(userRepository.findByEmail("email"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authServiceImpl.resendVerificationEmail("email"))
                .hasMessageContaining("Email is already verified")
                .isInstanceOf(BusinessViolatedException.class);

        assertTrue(user.isEmailVerified());

        verify(userRepository, times(1)).findByEmail("email");
        verify(emailTokenRepository, never()).save(any(EmailVerificationToken.class));
        verify(emailService, never()).sendVerificationEmail("email", user.getFirstName(), user.getEmail());
    }

    @Test
    @DisplayName("ForgetPassword")
    void forgetPassword_happyPath() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("email");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .user(user)
                .token("token")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        when(resetTokenRepository.save(any(PasswordResetToken.class)))
                .thenReturn(passwordResetToken);

        authServiceImpl.forgetPassword(request);

        verify(userRepository, times(1)).findByEmail(request.email());
        verify(resetTokenRepository, times(1)).invalidateAllTokens(user.getId());
        verify(resetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1))
                .sendPasswordResetEmail(user.getEmail(), user.getFirstName(), passwordResetToken.getToken());
    }

    @Test
    @DisplayName("ForgetPassword do nothing when user not found")
    void forgetPassword_doNothing_whenUserNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("wrongEmail");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        authServiceImpl.forgetPassword(request);

        verify(userRepository, times(1)).findByEmail(request.email());
        verifyNoInteractions(resetTokenRepository);
        verifyNoInteractions(emailTokenRepository);
    }

    @Test
    @DisplayName("ResetPassword")
    void resetPassword_happyPath() {
        ResetPasswordRequest request = new ResetPasswordRequest("token", "12345678");
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .user(user)
                .token("token")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        when(resetTokenRepository.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));

        when(passwordEncoder.encode(request.newPassword()))
                .thenReturn("encodedPassword");
        authServiceImpl.resetPassword(request);

        assertTrue(passwordResetToken.isUsed());
        assertEquals(user.getPassword(), "encodedPassword");

        verify(resetTokenRepository, times(1)).findByToken(passwordResetToken.getToken());
        verify(resetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("ResetPassword throwsResourceNotFoundException when token not found")
    void  resetPassword_throwsBusinessViolatedException_whenTokenNotFound() {
        ResetPasswordRequest request = new ResetPasswordRequest("token", "12345678");
        when(resetTokenRepository.findByToken("token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authServiceImpl.resetPassword(request))
                .hasMessageContaining("Reset link is invalid")
                .isInstanceOf(ResourceNotFoundException.class);

        verify(resetTokenRepository, times(1)).findByToken("token");
        verify(resetTokenRepository, never()).save(any(PasswordResetToken.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("ResetPassword throwsBusinessViolatedException when token used")
    void resetPassword_throwsBusinessViolatedException_whenTokenUsed() {
        ResetPasswordRequest request = new ResetPasswordRequest("token", "12345678");
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .user(user)
                .token("token")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isUsed(true)
                .build();
        when(resetTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(passwordResetToken));

        assertThatThrownBy(() -> authServiceImpl.resetPassword(request))
                .hasMessageContaining("Reset link has expired or was already used. Request a new one.")
                .isInstanceOf(BusinessViolatedException.class);

        assertTrue(passwordResetToken.isUsed());

        verify(resetTokenRepository, times(1)).findByToken("token");
        verify(resetTokenRepository, never()).save(any(PasswordResetToken.class));
        verifyNoInteractions(userRepository);
    }



}
