package com.seabattle.service;

import com.seabattle.model.Coordinate;
import com.seabattle.model.GameRoom;
import com.seabattle.model.GameStatus;
import com.seabattle.model.Player;
import com.seabattle.model.Ship;
import com.seabattle.model.ShotResult;
import com.seabattle.repository.GameRoomRepository;
import com.seabattle.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRoomRepository gameRoomRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public Player createOrUpdatePlayer(String sessionId, String username) {
        Optional<Player> existingPlayer = playerRepository.findBySessionId(sessionId);

        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            player.setUsername(username);
            player.setLastActivity(Instant.now());
            return playerRepository.save(player);
        } else {
            Player newPlayer = new Player(sessionId, username);
            return playerRepository.save(newPlayer);
        }
    }

    @Transactional
    public GameRoom findOrCreateGameRoom(Player player) {
        // First check for available rooms with one player
        List<GameRoom> waitingRooms = gameRoomRepository.findByStatus(GameStatus.WAITING);

        for (GameRoom room : waitingRooms) {
            if (room.getPlayer1() != null && room.getPlayer2() == null) {
                room.setPlayer2(player);
                return gameRoomRepository.save(room);
            }
        }

        // Create a new room if no waiting room is available
        GameRoom newRoom = new GameRoom();
        newRoom.setPlayer1(player);
        newRoom.setCurrentTurnPlayerId(player.getId());
        newRoom.setGameState(new com.seabattle.model.GameState());
        return gameRoomRepository.save(newRoom);
    }

    @Transactional
    public boolean placeShips(String roomId, Long playerId, List<List<Coordinate>> shipCoordinates) {
        Optional<GameRoom> roomOpt = gameRoomRepository.findByRoomId(roomId);

        if (roomOpt.isPresent()) {
            GameRoom room = roomOpt.get();

            // Convert the list of coordinate lists to a list of ships
            List<Ship> ships = new ArrayList<>();
            for (List<Coordinate> coordinates : shipCoordinates) {
                Ship ship = new Ship(coordinates);
                ships.add(ship);
            }

            // Validate ship placement
            if (!room.getGameState().isValidShipPlacement(ships, playerId)) {
                return false;
            }

            // Set ships for the player
            if (playerId.equals(room.getPlayer1().getId())) {
                room.getGameState().setPlayer1Ships(ships);
            } else if (playerId.equals(room.getPlayer2().getId())) {
                room.getGameState().setPlayer2Ships(ships);
            } else {
                return false;
            }

            // Mark player as ready
            if (playerId.equals(room.getPlayer1().getId())) {
                room.getPlayer1().setReady(true);
            } else {
                room.getPlayer2().setReady(true);
            }

            // Check if both players are ready
            if (room.isReadyToStart()) {
                room.setStatus(GameStatus.IN_PROGRESS);
                // Randomly determine who goes first
                Random random = new Random();
                if (random.nextBoolean()) {
                    room.setCurrentTurnPlayerId(room.getPlayer1().getId());
                } else {
                    room.setCurrentTurnPlayerId(room.getPlayer2().getId());
                }
            } else {
                room.setStatus(GameStatus.SHIP_PLACEMENT);
            }

            gameRoomRepository.save(room);
            return true;
        }

        return false;
    }

    @Transactional
    public ShotResult processShot(String roomId, Long playerId, Coordinate shot) {
        Optional<GameRoom> roomOpt = gameRoomRepository.findByRoomId(roomId);

        if (roomOpt.isPresent()) {
            GameRoom room = roomOpt.get();

            // Check if it's the player's turn
            if (!playerId.equals(room.getCurrentTurnPlayerId())) {
                return ShotResult.INVALID;
            }

            // Determine target player
            Long targetPlayerId;
            if (playerId.equals(room.getPlayer1().getId())) {
                targetPlayerId = 2L; // Target player 2
                room.getGameState().getPlayer1Shots().add(shot);
            } else {
                targetPlayerId = 1L; // Target player 1
                room.getGameState().getPlayer2Shots().add(shot);
            }

            // Process the shot
            ShotResult result = room.getGameState().processShot(shot, playerId, targetPlayerId);

            // Change turn if not a hit
            if (result == ShotResult.MISS) {
                if (playerId.equals(room.getPlayer1().getId())) {
                    room.setCurrentTurnPlayerId(room.getPlayer2().getId());
                } else {
                    room.setCurrentTurnPlayerId(room.getPlayer1().getId());
                }
            }

            // Check for win
            if (result == ShotResult.SHIP_DESTROYED && room.getGameState().checkForWin(playerId)) {
                room.setStatus(GameStatus.FINISHED);
            }

            gameRoomRepository.save(room);
            return result;
        }

        return ShotResult.INVALID;
    }

    @Transactional(readOnly = true)
    public GameRoom getGameRoom(String roomId) {
        return gameRoomRepository.findByRoomId(roomId).orElse(null);
    }
}