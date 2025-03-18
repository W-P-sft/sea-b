package com.seabattle.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Player {
    @Id
    @GeneratedValue
    private Long id;

    private String sessionId;
    private String username;
    private boolean ready;
    private Instant lastActivity;

    public Player(String sessionId, String username) {
        this.sessionId = sessionId;
        this.username = username;
        this.ready = false;
        this.lastActivity = Instant.now();
    }
}
