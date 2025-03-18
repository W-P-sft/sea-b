
package com.seabattle.dto;

import com.seabattle.model.Coordinate;
import com.seabattle.model.ShotResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameUpdate {
    private String type;
    private String roomId;
    private String message;
    private Long currentTurnPlayerId;
    private boolean gameOver;
    private Long winnerId;
    private Coordinate lastShot;
    private ShotResult lastShotResult;

    // Static factory methods for different update types
    public static GameUpdate playerJoined(String roomId, String username) {
        GameUpdate update = new GameUpdate();
        update.setType("PLAYER_JOINED");
        update.setRoomId(roomId);
        update.setMessage(username + " joined the game");
        return update;
    }

    public static GameUpdate gameStarted(String roomId, Long firstPlayerId) {
        GameUpdate update = new GameUpdate();
        update.setType("GAME_STARTED");
        update.setRoomId(roomId);
        update.setMessage("Game started!");
        update.setCurrentTurnPlayerId(firstPlayerId);
        return update;
    }

    public static GameUpdate shotFired(String roomId, Long currentTurnPlayerId, Coordinate shot, ShotResult result) {
        GameUpdate update = new GameUpdate();
        update.setType("SHOT_FIRED");
        update.setRoomId(roomId);
        update.setCurrentTurnPlayerId(currentTurnPlayerId);
        update.setLastShot(shot);
        update.setLastShotResult(result);

        switch (result) {
            case HIT:
                update.setMessage("Hit!");
                break;
            case MISS:
                update.setMessage("Miss!");
                break;
            case SHIP_DESTROYED:
                update.setMessage("Ship destroyed!");
                break;
            default:
                update.setMessage("Shot fired!");
        }

        return update;
    }

    public static GameUpdate gameOver(String roomId, Long winnerId) {
        GameUpdate update = new GameUpdate();
        update.setType("GAME_OVER");
        update.setRoomId(roomId);
        update.setGameOver(true);
        update.setWinnerId(winnerId);
        update.setMessage("Game over! Player " + winnerId + " wins!");
        return update;
    }
}