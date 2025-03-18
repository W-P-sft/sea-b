package com.seabattle.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
//@NoArgsConstructor
public class GameRoom {
    @Id
    @GeneratedValue
    private Long id;

    private String roomId;

    @OneToOne
    private Player player1;

    @OneToOne
    private Player player2;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    private GameState gameState;

    private Long currentTurnPlayerId;

    public GameRoom() {
        this.roomId = UUID.randomUUID().toString();
        this.status = GameStatus.WAITING;
    }

    public boolean isFull() {
        return player1 != null && player2 != null;
    }

    public boolean isReadyToStart() {
        return isFull() && player1.isReady() && player2.isReady();
    }

    public Player getOpponent(Player player) {
        if (player.getId().equals(player1.getId())) {
            return player2;
        } else if (player.getId().equals(player2.getId())) {
            return player1;
        }
        return null;
    }
}
