package com.seabattle.repository;

import com.seabattle.model.GameRoom;
import com.seabattle.model.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findByRoomId(String roomId);
    List<GameRoom> findByStatus(GameStatus status);
}