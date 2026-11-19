package com.ecoquest.entity;

import com.ecoquest.entity.embedded.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "schools")
public class School {

    @Id
    private String id;

    private String name;
    private String city;
    private String state;
    private GeoLocation campusLocation;

    @CreatedDate
    private Instant createdAt;
}
