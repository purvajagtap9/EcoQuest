package com.ecoquest.entity.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded value object — NOT its own collection. Reused inside Quest (required location +
 * radius), QuestSubmission (submitted coordinates) and School (campus coordinates).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocation {
    private Double latitude;
    private Double longitude;
}
