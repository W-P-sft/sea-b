package com.seabattle.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor

public class GameState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Ship> player1Ships = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Ship> player2Ships = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Coordinate> player1Shots = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Coordinate> player2Shots = new ArrayList<>();

    @Transient
    private static final int BOARD_SIZE = 32;

    public boolean isValidShipPlacement(List<Ship> ships, Long playerId) {
        // Check if player already has 4 ships
        if (ships.size() > 4) return false;

        // Check if each ship has exactly 4 cells
        for (Ship ship : ships) {
            if (ship.getCoordinates().size() != 4) return false;
        }

        // Check if ships are within board boundaries and don't overlap
        boolean[][] occupied = new boolean[BOARD_SIZE][BOARD_SIZE];

        for (Ship ship : ships) {
            for (Coordinate coord : ship.getCoordinates()) {
                if (coord.getX() < 0 || coord.getX() >= BOARD_SIZE ||
                        coord.getY() < 0 || coord.getY() >= BOARD_SIZE ||
                        occupied[coord.getX()][coord.getY()]) {
                    return false;
                }
                occupied[coord.getX()][coord.getY()] = true;
            }
        }

        return true;
    }

    public ShotResult processShot(Coordinate shot, Long shooterPlayerId, Long targetPlayerId) {
        List<Ship> targetShips = targetPlayerId.equals(1L) ? player1Ships : player2Ships;

        for (Ship ship : targetShips) {
            for (Coordinate coord : ship.getCoordinates()) {
                if (coord.getX() == shot.getX() && coord.getY() == shot.getY()) {
                    if (!coord.isHit()) {
                        coord.setHit(true);

                        // Check if ship is completely destroyed
                        boolean shipDestroyed = true;
                        for (Coordinate shipCoord : ship.getCoordinates()) {
                            if (!shipCoord.isHit()) {
                                shipDestroyed = false;
                                break;
                            }
                        }

                        if (shipDestroyed) {
                            ship.setDestroyed(true);
                            return ShotResult.SHIP_DESTROYED;
                        }

                        return ShotResult.HIT;
                    } else {
                        return ShotResult.ALREADY_HIT;
                    }
                }
            }
        }

        return ShotResult.MISS;
    }

    public boolean checkForWin(Long playerId) {
        List<Ship> opponentShips = playerId.equals(1L) ? player2Ships : player1Ships;

        for (Ship ship : opponentShips) {
            if (!ship.isDestroyed()) {
                return false;
            }
        }

        return true;
    }
}
