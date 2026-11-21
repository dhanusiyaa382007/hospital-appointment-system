package com.hospital;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {
    public enum Status { BOOKED, CANCELLED }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final int id;
    private final Patient patient;
    private final Doctor doctor;
    private final LocalDateTime time;
    private Status status = Status.BOOKED;

    public Appointment(int id, Patient patient, Doctor doctor, LocalDateTime time) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.time = time;
    }

    public int getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getTime() { return time; }
    public Status getStatus() { return status; }
    public void cancel() { this.status = Status.CANCELLED; }

    @Override
    public String toString() {
        return "Appt #" + id + " | " + time.format(FMT) + " | " + patient.name()
                + " with Dr. " + doctor.name() + " | " + status;
    }
}
