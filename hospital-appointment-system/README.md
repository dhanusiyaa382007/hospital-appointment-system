# Hospital Appointment System

A console-based hospital appointment system written in Java (Software Engineering project).

## Features
- Register patients
- View doctors and specialties
- Book an appointment (blocks double-booking and past dates)
- Cancel an appointment
- View a patient's appointments

## Project structure
```
src/main/java/com/hospital/
  Main.java                 console menu
  AppointmentService.java   business logic
  Appointment.java          appointment entity
  Patient.java, Doctor.java entities
```

## Requirements
Java 17+

## Build and run
```bash
javac -d out $(find src -name "*.java")
java -cp out com.hospital.Main
```

## Run as a website
```bash
javac -d out src/main/java/com/hospital/*.java
java -cp out com.hospital.WebServer
```
Then open http://localhost:8080 in a browser.

Pages: `/` (home), `/patients`, `/doctors`, `/book`, `/appointments`. Press Ctrl+C in the terminal to stop.

## Team
- Name 1 - role
- Name 2 - role

## Roadmap
- [ ] Persist data (file or database)
- [ ] Doctor working hours / available slots
- [ ] Admin role
- [ ] Unit tests (JUnit)
