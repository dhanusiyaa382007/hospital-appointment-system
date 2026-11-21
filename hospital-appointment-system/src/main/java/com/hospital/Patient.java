package com.hospital;

public record Patient(int id, String name, String phone) {
    @Override
    public String toString() {
        return "#" + id + " " + name + " (" + phone + ")";
    }
}
