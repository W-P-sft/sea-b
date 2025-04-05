package com.seabattle.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private static final int BOARD_SIZE = 14;

    public boolean isValidShipPlacement(List<Ship> ships, Long playerId) {
        // Check if player already has 4 ships
        if (ships.size() > 4) return false;

        // Check if each ship has exactly 4 cells
        for (Ship ship : ships) {
            if (ship.getCoordinates().size() != 4) return false;
        }

        // Check if ships are within board boundaries, don't overlap, and don't border each other
        boolean[][] occupied = new boolean[BOARD_SIZE][BOARD_SIZE];

        for (Ship ship : ships) {
            for (Coordinate coord : ship.getCoordinates()) {
                // Check boundaries
                if (coord.getX() < 0 || coord.getX() >= BOARD_SIZE ||
                        coord.getY() < 0 || coord.getY() >= BOARD_SIZE) {
                    return false;
                }

                // Check if the cell is already occupied
                if (occupied[coord.getX()][coord.getY()]) {
                    return false;
                }

                // Check if the cell borders another ship's cell
                if (hasAdjacentShip(coord.getX(), coord.getY(), occupied)) {
                    return false;
                }
            }

            // Mark ship cells as occupied
            for (Coordinate coord : ship.getCoordinates()) {
                occupied[coord.getX()][coord.getY()] = true;
            }
        }

        return true;
    }

    // Helper method to check if a cell borders any occupied cell
    private boolean hasAdjacentShip(int x, int y, boolean[][] occupied) {
        // Check all 8 adjacent cells (horizontal, vertical, and diagonal)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                // Skip the cell itself
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                // Check if the adjacent cell is within bounds and occupied
                if (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE && occupied[nx][ny]) {
                    return true;
                }
            }
        }

        return false;
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

                            // Mark water cells around the destroyed ship
                            markWaterAroundShip(ship);

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

    // Helper method to mark water cells around a destroyed ship
    private void markWaterAroundShip(Ship ship) {
        List<Coordinate> waterCells = new ArrayList<>();

        // For each ship cell, mark surrounding cells as water
        for (Coordinate shipCoord : ship.getCoordinates()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    // Skip the ship cell itself
                    if (dx == 0 && dy == 0) continue;

                    int nx = shipCoord.getX() + dx;
                    int ny = shipCoord.getY() + dy;

                    // Check if cell is within board boundaries
                    if (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE) {
                        // Check if this cell is not part of the ship
                        boolean isShipCell = false;
                        for (Coordinate coord : ship.getCoordinates()) {
                            if (coord.getX() == nx && coord.getY() == ny) {
                                isShipCell = true;
                                break;
                            }
                        }

                        if (!isShipCell) {
                            // Create a new water cell and mark it as hit
                            Coordinate waterCell = new Coordinate(nx, ny, false);
                            waterCell.setWater(true);
                            waterCells.add(waterCell);
                        }
                    }
                }
            }
        }

        // Add water cells to the appropriate shots list
        ship.setWaterCells(waterCells);
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