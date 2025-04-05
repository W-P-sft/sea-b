package com.seabattle.model;

import jakarta.persistence.Embeddable;
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
    private boolean water;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
        this.hit = false;
        this.water = false;
    }

    public Coordinate(int x, int y, boolean hit) {
        this.x = x;
        this.y = y;
        this.hit = hit;
        this.water = false;
    }
}