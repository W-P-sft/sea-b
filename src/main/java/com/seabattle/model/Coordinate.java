package com.seabattle.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {
    private int x;
    private int y;
    private boolean hit;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
        this.hit = false;
    }
}