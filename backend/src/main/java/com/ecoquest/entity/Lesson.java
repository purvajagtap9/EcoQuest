package com.ecoquest.entity;

import com.ecoquest.entity.embedded.LocalizedText;
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
 * Bite-sized content unit. Deliberately short (spec: "do not create huge textbook paragraphs") —
 * body is a few short paragraphs max, meant to render as a single scrollable card.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "lessons")
public class Lesson {

    @Id
    private String id;

    @Indexed
    private String topicId;

    private LocalizedText title;
    private LocalizedText body;              // short — a few sentences to a couple of paragraphs
    private LocalizedText localContextNote;   // Indian-context example, e.g. Swachh Bharat, Namami Gange
    private String imageUrl;
    private Integer orderIndex;

    @CreatedDate
    private Instant createdAt;
}
