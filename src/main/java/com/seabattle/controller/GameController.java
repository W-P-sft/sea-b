package com.seabattle.controller;

import com.seabattle.dto.GameUpdate;
import com.seabattle.dto.JoinGameRequest;
import com.seabattle.dto.ShipPlacementRequest;
import com.seabattle.dto.ShotRequest;
import com.seabattle.model.Coordinate;
import com.seabattle.model.GameRoom;
import com.seabattle.model.Player;
import com.seabattle.model.Ship;
import com.seabattle.model.ShotResult;
import com.seabattle.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @MessageMapping("/join")
    public void joinGame(@Payload JoinGameRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        // Create or update player
        Player player = gameService.createOrUpdatePlayer(sessionId, request.getUsername());

        // Find or create a game room
        GameRoom room = gameService.findOrCreateGameRoom(player);

        // Store room ID in WebSocket session
        headerAccessor.getSessionAttributes().put("roomId", room.getRoomId());
        headerAccessor.getSessionAttributes().put("playerId", player.getId());

        // Send game state to the player
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/game-state",
                room
        );

        // Notify all players in the room
        GameUpdate update = GameUpdate.playerJoined(room.getRoomId(), player.getUsername());
        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), update);
    }

    @MessageMapping("/place-ships")
    public void placeShips(@Payload ShipPlacementRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String roomId = request.getRoomId();
        Long playerId = (Long) headerAccessor.getSessionAttributes().get("playerId");

        List<List<Coordinate>> shipCoordinatesList = request.getShips();
        boolean success = gameService.placeShips(roomId, playerId, shipCoordinatesList);

        if (success) {
            // Check if game started
            GameRoom room = gameService.getGameRoom(roomId);
            if (room.getStatus().equals(com.seabattle.model.GameStatus.IN_PROGRESS)) {
                // Game started, notify players
                GameUpdate update = GameUpdate.gameStarted(roomId, room.getCurrentTurnPlayerId());
                messagingTemplate.convertAndSend("/topic/room/" + roomId, update);

                // Send initial game state to both players
                messagingTemplate.convertAndSendToUser(
                        room.getPlayer1().getSessionId(),
                        "/queue/game-state",
                        room
                );
                messagingTemplate.convertAndSendToUser(
                        room.getPlayer2().getSessionId(),
                        "/queue/game-state",
                        room
                );
            }
        }
    }

    @MessageMapping("/fire")
    public void fireShot(@Payload ShotRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String roomId = request.getRoomId();
        Long playerId = (Long) headerAccessor.getSessionAttributes().get("playerId");
        Coordinate shot = request.getCoordinate();

        ShotResult result = gameService.processShot(roomId, playerId, shot);

        // Retrieve updated game room
        GameRoom room = gameService.getGameRoom(roomId);

        if (result != ShotResult.INVALID) {
            // Notify players about the shot
            GameUpdate update;
            if (room.getStatus().equals(com.seabattle.model.GameStatus.FINISHED)) {
                update = GameUpdate.gameOver(roomId, playerId);
            } else {
                update = GameUpdate.shotFired(roomId, room.getCurrentTurnPlayerId(), shot, result);
            }

            messagingTemplate.convertAndSend("/topic/room/" + roomId, update);

            // Send updated game state to both players
            messagingTemplate.convertAndSendToUser(
                    room.getPlayer1().getSessionId(),
                    "/queue/game-state",
                    room
            );
            messagingTemplate.convertAndSendToUser(
                    room.getPlayer2().getSessionId(),
                    "/queue/game-state",
                    room
            );
        }
    }
}