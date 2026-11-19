package com.ecoquest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "student_badges")
@CompoundIndex(name = "student_badge_unique_idx", def = "{'studentId': 1, 'badgeId': 1}", unique = true)
public class StudentBadge {

    @Id
    private String id;

    private String studentId;
    private String badgeId;
    private Instant earnedAt;
}
