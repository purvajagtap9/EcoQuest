package com.ecoquest.entity;

import com.ecoquest.entity.embedded.LocalizedText;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "evs_topics")
public class EvsTopic {

    @Id
    private String id;

    private LocalizedText title;
    private LocalizedText description;
    private List<LocalizedText> learningObjectives = new ArrayList<>();
    private String coverImageUrl;
    private Integer orderIndex;
    private boolean published = true;

    @CreatedDate
    private Instant createdAt;
}
