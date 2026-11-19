package com.ecoquest.service;

import com.ecoquest.dto.auth.AuthResponse;
import com.ecoquest.dto.auth.LoginRequest;
import com.ecoquest.dto.auth.RegisterRequest;
import com.ecoquest.entity.User;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.UserRepository;
import com.ecoquest.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Purva Student");
        registerRequest.setEmail("Student@EcoQuest.com"); // mixed case - should be normalized
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.STUDENT);
        registerRequest.setSchoolId("school-1");
    }

    @Test
    void register_savesHashedPasswordAndNormalizedEmail_andReturnsToken() {
        when(userRepository.existsByEmail("student@ecoquest.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("user-1");
            return u;
        });
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("student@ecoquest.com");
        assertThat(saved.getPasswordHash()).isEqualTo("HASHED");
        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
        assertThat(saved.getStudentProfile()).isNotNull();
        assertThat(saved.getStudentProfile().getEcoCoins()).isZero();
        assertThat(saved.getTeacherProfile()).isNull();

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("student@ecoquest.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_teacherRole_getsTeacherProfileNotStudentProfile() {
        registerRequest.setRole(Role.TEACHER);
        registerRequest.setAssignedSchoolId("school-9");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        authService.register(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getTeacherProfile()).isNotNull();
        assertThat(saved.getTeacherProfile().getAssignedSchoolId()).isEqualTo("school-9");
        assertThat(saved.getStudentProfile()).isNull();
    }

    @Test
    void register_adminRole_whenNoAdminExists_isAllowedToBootstrapFirstAdmin() {
        registerRequest.setRole(Role.ADMIN);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void register_adminRole_whenAdminAlreadyExists_isForbidden() {
        registerRequest.setRole(Role.ADMIN);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cannot be created through public registration");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsToken() {
        User existing = User.builder()
                .id("user-1").email("student@ecoquest.com").name("Purva")
                .role(Role.STUDENT).build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("Student@EcoQuest.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail("student@ecoquest.com")).thenReturn(Optional.of(existing));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("student@ecoquest.com");
        verify(authenticationManager).authenticate(any());
    }
}
