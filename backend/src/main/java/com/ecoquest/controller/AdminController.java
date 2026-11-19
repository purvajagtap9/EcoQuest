package com.ecoquest.controller;

import com.ecoquest.dto.admin.AdminDashboardResponse;
import com.ecoquest.dto.admin.LeaderboardPrivacyRequest;
import com.ecoquest.dto.admin.SchoolRequest;
import com.ecoquest.dto.admin.SchoolResponse;
import com.ecoquest.dto.admin.UpdateUserStatusRequest;
import com.ecoquest.dto.admin.UserAdminResponse;
import com.ecoquest.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Platform analytics, schools, students, teachers")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {
        return adminService.getDashboard();
    }

    // ---------- Schools ----------

    @GetMapping("/schools")
    public Page<SchoolResponse> listSchools(Pageable pageable) {
        return adminService.listSchools(pageable);
    }

    @GetMapping("/schools/{id}")
    public SchoolResponse getSchool(@PathVariable String id) {
        return adminService.getSchool(id);
    }

    @PostMapping("/schools")
    public ResponseEntity<SchoolResponse> createSchool(@Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createSchool(request));
    }

    @PutMapping("/schools/{id}")
    public SchoolResponse updateSchool(@PathVariable String id, @Valid @RequestBody SchoolRequest request) {
        return adminService.updateSchool(id, request);
    }

    @DeleteMapping("/schools/{id}")
    public ResponseEntity<Void> deleteSchool(@PathVariable String id) {
        adminService.deleteSchool(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Students ----------

    @GetMapping("/students")
    public Page<UserAdminResponse> listStudents(Pageable pageable) {
        return adminService.listStudents(pageable);
    }

    @GetMapping("/students/{id}")
    public UserAdminResponse getStudent(@PathVariable String id) {
        return adminService.getUser(id);
    }

    @PutMapping("/students/{id}/status")
    public UserAdminResponse setStudentStatus(@PathVariable String id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminService.setUserEnabled(id, request.getEnabled());
    }

    @PutMapping("/students/{id}/leaderboard-privacy")
    public UserAdminResponse setLeaderboardPrivacy(@PathVariable String id, @Valid @RequestBody LeaderboardPrivacyRequest request) {
        return adminService.setLeaderboardPrivacy(id, request.getDisplayName(), request.getShowRealNameOnLeaderboard());
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Teachers ----------

    @GetMapping("/teachers")
    public Page<UserAdminResponse> listTeachers(Pageable pageable) {
        return adminService.listTeachers(pageable);
    }

    @GetMapping("/teachers/{id}")
    public UserAdminResponse getTeacher(@PathVariable String id) {
        return adminService.getUser(id);
    }

    @PutMapping("/teachers/{id}/status")
    public UserAdminResponse setTeacherStatus(@PathVariable String id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminService.setUserEnabled(id, request.getEnabled());
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
