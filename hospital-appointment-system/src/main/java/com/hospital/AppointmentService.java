package com.hospital;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentService {
    private final List<Patient> patients = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Appointment> appointments = new ArrayList<>();
    private int nextPatientId = 1, nextDoctorId = 1, nextApptId = 1;

    public Patient addPatient(String name, String phone) {
        Patient p = new Patient(nextPatientId++, name, phone);
        patients.add(p);
        return p;
    }

    public Doctor addDoctor(String name, String specialty) {
        Doctor d = new Doctor(nextDoctorId++, name, specialty);
        doctors.add(d);
        return d;
    }

    public List<Doctor> getDoctors() { return List.copyOf(doctors); }
    public List<Patient> getPatients() { return List.copyOf(patients); }
    public List<Appointment> getAppointments() { return List.copyOf(appointments); }

    public Optional<Patient> findPatient(int id) {
        return patients.stream().filter(p -> p.id() == id).findFirst();
    }

    public Optional<Doctor> findDoctor(int id) {
        return doctors.stream().filter(d -> d.id() == id).findFirst();
    }

    /** A doctor can't have two BOOKED appointments at the same time. */
    public boolean isSlotFree(Doctor doctor, LocalDateTime time) {
        return appointments.stream().noneMatch(a ->
                a.getStatus() == Appointment.Status.BOOKED
                && a.getDoctor().equals(doctor)
                && a.getTime().equals(time));
    }

    public Appointment book(Patient patient, Doctor doctor, LocalDateTime time) {
        if (time.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Cannot book in the past.");
        if (!isSlotFree(doctor, time))
            throw new IllegalStateException("That slot is already taken.");
        Appointment a = new Appointment(nextApptId++, patient, doctor, time);
        appointments.add(a);
        return a;
    }

    public boolean cancel(int appointmentId) {
        for (Appointment a : appointments) {
            if (a.getId() == appointmentId && a.getStatus() == Appointment.Status.BOOKED) {
                a.cancel();
                return true;
            }
        }
        return false;
    }

    public List<Appointment> forPatient(Patient patient) {
        return appointments.stream().filter(a -> a.getPatient().equals(patient)).toList();
    }
}
