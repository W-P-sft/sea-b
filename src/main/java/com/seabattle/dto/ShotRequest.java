package com.seabattle.dto;

import com.seabattle.model.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShotRequest {
    private String roomId;
    private Coordinate coordinate;
}