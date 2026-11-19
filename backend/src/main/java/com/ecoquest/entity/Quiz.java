package com.ecoquest.entity;

import com.ecoquest.entity.embedded.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "quizzes")
public class Quiz {

    @Id
    private String id;

    @Indexed
    private String topicId;

    private String title;
    private List<Question> questions = new ArrayList<>();

    private Integer coinsPerCorrectAnswer = 5;
    private Integer xpPerCorrectAnswer = 10;

    private boolean published = true;

    @CreatedDate
    private Instant createdAt;
}
