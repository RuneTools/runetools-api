package org.runetools.api.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class NemiForestLocation {
    private String author;
    private String mapUrl;
    private LocalDateTime postedAt;
    private int world;
}
