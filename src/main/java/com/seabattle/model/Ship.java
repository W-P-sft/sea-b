package com.seabattle.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Coordinate> coordinates = new ArrayList<>();

    private boolean destroyed;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Coordinate> waterCells = new ArrayList<>();

    public Ship(List<Coordinate> coordinates) {
        this.coordinates = new ArrayList<>(coordinates);
        this.destroyed = false;
        this.waterCells = new ArrayList<>();
    }
}