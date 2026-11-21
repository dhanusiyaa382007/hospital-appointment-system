package com.hospital;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        AppointmentService svc = new AppointmentService();
        svc.addDoctor("Smith", "Cardiology");
        svc.addDoctor("Lee", "Pediatrics");
        svc.addDoctor("Khan", "Dermatology");

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("\n1) Register patient  2) List doctors  3) Book  4) Cancel  5) My appointments  0) Exit");
            System.out.print("> ");
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Name: "); String n = in.nextLine();
                        System.out.print("Phone: "); String ph = in.nextLine();
                        System.out.println("Registered: " + svc.addPatient(n, ph));
                    }
                    case "2" -> svc.getDoctors().forEach(System.out::println);
                    case "3" -> {
                        System.out.print("Patient id: "); int pid = Integer.parseInt(in.nextLine());
                        System.out.print("Doctor id: "); int did = Integer.parseInt(in.nextLine());
                        System.out.print("Date/time (yyyy-MM-dd HH:mm): ");
                        LocalDateTime t = LocalDateTime.parse(in.nextLine(), FMT);
                        Patient p = svc.findPatient(pid).orElseThrow(() -> new IllegalArgumentException("No such patient."));
                        Doctor d = svc.findDoctor(did).orElseThrow(() -> new IllegalArgumentException("No such doctor."));
                        System.out.println("Booked: " + svc.book(p, d, t));
                    }
                    case "4" -> {
                        System.out.print("Appointment id: ");
                        System.out.println(svc.cancel(Integer.parseInt(in.nextLine())) ? "Cancelled." : "Not found or already cancelled.");
                    }
                    case "5" -> {
                        System.out.print("Patient id: ");
                        Patient p = svc.findPatient(Integer.parseInt(in.nextLine()))
                                .orElseThrow(() -> new IllegalArgumentException("No such patient."));
                        svc.forPatient(p).forEach(System.out::println);
                    }
                    case "0" -> { return; }
                    default -> System.out.println("Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            } catch (DateTimeParseException e) {
                System.out.println("Use format yyyy-MM-dd HH:mm");
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
