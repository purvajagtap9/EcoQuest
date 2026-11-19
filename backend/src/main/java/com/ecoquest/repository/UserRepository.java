package com.ecoquest.repository;

import com.ecoquest.entity.User;
import com.ecoquest.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    // Unpaged - used internally to sum eco-coins across all students for the admin dashboard.
    List<User> findByRole(Role role);

    long countByRole(Role role);

    long countByEnabled(boolean enabled);

    Page<User> findByRoleAndStudentProfile_SchoolId(Role role, String schoolId, Pageable pageable);

    // Unpaged variant used internally for aggregation (e.g. collecting a school's student ids
    // to scope submission counts) — not exposed directly via any controller.
    List<User> findByRoleAndStudentProfile_SchoolId(Role role, String schoolId);

    long countByRoleAndStudentProfile_SchoolId(Role role, String schoolId);

    // Used to compute a student's live rank: count how many students have strictly more
    // eco-coins, then rank = that count + 1. This is the real computation the Phase 14
    // LeaderboardSnapshot cache will later sit on top of for scale.
    long countByRoleAndStudentProfile_EcoCoinsGreaterThan(Role role, Integer ecoCoins);
}
