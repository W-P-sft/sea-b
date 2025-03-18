package com.seabattle.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Ship {
    @Id
    @GeneratedValue
    private Long id;

    @ElementCollection
    private List<Coordinate> coordinates = new ArrayList<>();

    private boolean destroyed;

    public Ship(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
        this.destroyed = false;
    }
}