package com.ecoquest.entity;

import com.ecoquest.entity.embedded.StudentProfile;
import com.ecoquest.entity.embedded.TeacherProfile;
import com.ecoquest.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Single collection for Student / Teacher / Admin, distinguished by role.
 * Design note: the master spec lists User/Student/Teacher as separate entities, which maps to
 * separate SQL tables under JPA. Under MongoDB the idiomatic equivalent is one document per
 * person with a role-specific embedded profile (studentProfile / teacherProfile) — this avoids
 * a join on every single request (every dashboard call needs "the user" first) while keeping
 * the two profile shapes cleanly separated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;   // BCrypt — NEVER exposed via DTO

    private String name;

    private Role role;

    private String phone;

    // Only one of these is populated, matching `role`.
    private StudentProfile studentProfile;
    private TeacherProfile teacherProfile;

    private boolean enabled = true;

    @CreatedDate
    private Instant createdAt;

    private Instant updatedAt;
}
