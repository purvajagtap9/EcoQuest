package com.ecoquest.entity.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Embedded inside User when role == TEACHER.
 * assignedSchoolId enforces business rule #4: only teachers assigned to a school can verify
 * that school's students' submissions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfile {
    private String assignedSchoolId;
    private List<String> subjectAreas = new ArrayList<>();
}
