package com.ecoquest.service;

import com.ecoquest.dto.auth.AuthResponse;
import com.ecoquest.dto.auth.LoginRequest;
import com.ecoquest.dto.auth.RegisterRequest;
import com.ecoquest.entity.User;
import com.ecoquest.entity.embedded.StudentProfile;
import com.ecoquest.entity.embedded.Streak;
import com.ecoquest.entity.embedded.TeacherProfile;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.UserRepository;
import com.ecoquest.security.CustomUserDetails;
import com.ecoquest.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }

        // Privilege-escalation guard: public registration may only create the very FIRST admin
        // (bootstrap). Once any admin exists, further admins can't be self-registered - they'd
        // need to be created by an existing admin/seed script instead.
        if (request.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) > 0) {
            throw ApiException.forbidden("Admin accounts cannot be created through public registration");
        }

        User.UserBuilder userBuilder = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole())
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());

        if (request.getRole() == Role.STUDENT) {
            userBuilder.studentProfile(StudentProfile.builder()
                    .schoolId(request.getSchoolId())
                    .grade(request.getGrade())
                    .preferredLanguage("en")
                    .ecoCoins(0)
                    .xp(0)
                    .streak(new Streak(0, 0, null))
                    .displayName(request.getName())
                    .showRealNameOnLeaderboard(true)
                    .build());
        } else if (request.getRole() == Role.TEACHER) {
            userBuilder.teacherProfile(TeacherProfile.builder()
                    .assignedSchoolId(request.getAssignedSchoolId())
                    .build());
        }
        // ADMIN needs neither embedded profile

        User saved = userRepository.save(userBuilder.build());

        String token = jwtService.generateToken(new CustomUserDetails(saved));

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        String token = jwtService.generateToken(new CustomUserDetails(user));

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
