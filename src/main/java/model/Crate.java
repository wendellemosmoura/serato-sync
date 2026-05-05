package main.java.model;

import java.util.ArrayList;
import java.util.List;

public class Crate {
    private String name;
    private List<String> tracks = new ArrayList<>();

    public Crate(String name) {
        this.name = name;
    }

    public void addTrack(String path) {
        tracks.add(path);
    }

    public List<String> getTracks() {
        return tracks;
    }

    public String getName() {
        return name;
    }
}
