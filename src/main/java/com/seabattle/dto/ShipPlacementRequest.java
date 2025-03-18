package com.seabattle.dto;

import com.seabattle.model.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipPlacementRequest {
    private String roomId;
    private List<List<Coordinate>> ships;
}
