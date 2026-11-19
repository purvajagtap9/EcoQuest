package com.ecoquest.entity.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One language's rendering of a piece of content. Stored per-language rather than hard-coded
 * into frontend components, per the spec's regional-language requirement (English required,
 * Hindi/Marathi optional per record for the prototype).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalizedText {
    private String en;
    private String hi;
    private String mr;

    public String get(String langCode) {
        return switch (langCode == null ? "en" : langCode) {
            case "hi" -> (hi != null && !hi.isBlank()) ? hi : en;
            case "mr" -> (mr != null && !mr.isBlank()) ? mr : en;
            default -> en;
        };
    }
}
