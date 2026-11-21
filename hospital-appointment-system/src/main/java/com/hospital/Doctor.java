package com.hospital;

public record Doctor(int id, String name, String specialty) {
    @Override
    public String toString() {
        return "#" + id + " Dr. " + name + " - " + specialty;
    }
}
