package com.hospital;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Hospital appointment system as a multi-page website.
 * Pages: /  /patients  /doctors  /book  /appointments
 * Run, then open http://localhost:8080
 */
public class WebServer {
    private static final AppointmentService svc = new AppointmentService();

    public static void main(String[] args) throws IOException {
        svc.addDoctor("Smith", "Cardiology");
        svc.addDoctor("Lee", "Pediatrics");
        svc.addDoctor("Khan", "Dermatology");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", WebServer::home);
        server.createContext("/patients", WebServer::patients);
        server.createContext("/doctors", WebServer::doctors);
        server.createContext("/book", WebServer::book);
        server.createContext("/appointments", WebServer::appointments);
        server.createContext("/cancel", WebServer::cancel);
        server.start();
        System.out.println("Server started. Open http://localhost:8080 in your browser.");
    }

    // ---------- page handlers ----------

    private static void home(HttpExchange ex) throws IOException {
        if (!"/".equals(ex.getRequestURI().getPath())) {
            send(ex, 404, layout("", "", "<div class='card'><h3>Page not found</h3><p><a href='/'>Go home</a></p></div>"));
            return;
        }
        int booked = 0;
        for (Appointment a : svc.getAppointments())
            if (a.getStatus() == Appointment.Status.BOOKED) booked++;

        String c = "<div class='card'><h3>Welcome</h3><p>Manage patients, doctors and appointments.</p></div>"
                + "<div class='stats'>"
                + stat(svc.getPatients().size(), "Patients", "/patients")
                + stat(svc.getDoctors().size(), "Doctors", "/doctors")
                + stat(booked, "Booked", "/appointments")
                + "</div>"
                + "<p><a class='btn' href='/book'>Book an appointment</a></p>";
        send(ex, 200, layout("/", msg(ex), c));
    }

    private static void patients(HttpExchange ex) throws IOException {
        if (isPost(ex)) {
            Map<String, String> f = parse(body(ex));
            String name = f.getOrDefault("name", "").trim();
            String phone = f.getOrDefault("phone", "").trim();
            if (name.isEmpty() || phone.isEmpty()) { redirect(ex, "/patients", "Enter name and phone."); return; }
            Patient p = svc.addPatient(name, phone);
            redirect(ex, "/patients", "Registered: " + p);
            return;
        }
        StringBuilder c = new StringBuilder();
        c.append("<div class='card'><h3>Register patient</h3><form method='post' action='/patients'>")
         .append("<label>Name</label><input name='name' required>")
         .append("<label>Phone</label><input name='phone' required>")
         .append("<button>Register</button></form></div>")
         .append("<div class='card'><h3>All patients</h3>");
        if (svc.getPatients().isEmpty()) c.append("<p>No patients yet.</p>");
        for (Patient p : svc.getPatients())
            c.append("<div class='row'><span>").append(esc(p.toString())).append("</span></div>");
        c.append("</div>");
        send(ex, 200, layout("/patients", msg(ex), c.toString()));
    }

    private static void doctors(HttpExchange ex) throws IOException {
        if (isPost(ex)) {
            Map<String, String> f = parse(body(ex));
            String name = f.getOrDefault("name", "").trim();
            String spec = f.getOrDefault("specialty", "").trim();
            if (name.isEmpty() || spec.isEmpty()) { redirect(ex, "/doctors", "Enter name and specialty."); return; }
            Doctor d = svc.addDoctor(name, spec);
            redirect(ex, "/doctors", "Added: " + d);
            return;
        }
        StringBuilder c = new StringBuilder();
        c.append("<div class='card'><h3>Our doctors</h3>");
        for (Doctor d : svc.getDoctors())
            c.append("<div class='row'><span>").append(esc(d.toString())).append("</span></div>");
        c.append("</div>")
         .append("<div class='card'><h3>Add doctor</h3><form method='post' action='/doctors'>")
         .append("<label>Name</label><input name='name' required>")
         .append("<label>Specialty</label><input name='specialty' required>")
         .append("<button>Add doctor</button></form></div>");
        send(ex, 200, layout("/doctors", msg(ex), c.toString()));
    }

    private static void book(HttpExchange ex) throws IOException {
        if (isPost(ex)) {
            Map<String, String> f = parse(body(ex));
            try {
                Patient p = svc.findPatient(Integer.parseInt(f.getOrDefault("patient", "")))
                        .orElseThrow(() -> new IllegalArgumentException("Register a patient first."));
                Doctor d = svc.findDoctor(Integer.parseInt(f.getOrDefault("doctor", "")))
                        .orElseThrow(() -> new IllegalArgumentException("No such doctor."));
                LocalDateTime t = LocalDateTime.parse(f.getOrDefault("time", ""));
                Appointment a = svc.book(p, d, t);
                redirect(ex, "/appointments", "Booked: " + a);
            } catch (RuntimeException e) {
                redirect(ex, "/book", "Error: " + e.getMessage());
            }
            return;
        }
        StringBuilder c = new StringBuilder();
        c.append("<div class='card'><h3>Book appointment</h3>");
        if (svc.getPatients().isEmpty()) {
            c.append("<p>No patients yet. <a href='/patients'>Register a patient</a> first.</p></div>");
        } else {
            c.append("<form method='post' action='/book'><label>Patient</label><select name='patient'>");
            for (Patient p : svc.getPatients())
                c.append("<option value='").append(p.id()).append("'>").append(esc(p.toString())).append("</option>");
            c.append("</select><label>Doctor</label><select name='doctor'>");
            for (Doctor d : svc.getDoctors())
                c.append("<option value='").append(d.id()).append("'>").append(esc(d.toString())).append("</option>");
            c.append("</select><label>Date and time</label><input type='datetime-local' name='time' required>")
             .append("<button>Book</button></form></div>");
        }
        send(ex, 200, layout("/book", msg(ex), c.toString()));
    }

    private static void appointments(HttpExchange ex) throws IOException {
        StringBuilder c = new StringBuilder();
        c.append("<div class='card'><h3>Appointments</h3>");
        if (svc.getAppointments().isEmpty()) c.append("<p>No appointments yet.</p>");
        for (Appointment a : svc.getAppointments()) {
            c.append("<div class='row'><span>").append(esc(a.toString())).append("</span>");
            if (a.getStatus() == Appointment.Status.BOOKED) {
                c.append("<form method='post' action='/cancel'><input type='hidden' name='id' value='")
                 .append(a.getId()).append("'><button class='x'>Cancel</button></form>");
            }
            c.append("</div>");
        }
        c.append("</div>");
        send(ex, 200, layout("/appointments", msg(ex), c.toString()));
    }

    private static void cancel(HttpExchange ex) throws IOException {
        if (!isPost(ex)) { redirect(ex, "/appointments", ""); return; }
        Map<String, String> f = parse(body(ex));
        try {
            boolean ok = svc.cancel(Integer.parseInt(f.getOrDefault("id", "")));
            redirect(ex, "/appointments", ok ? "Appointment cancelled." : "Not found or already cancelled.");
        } catch (NumberFormatException e) {
            redirect(ex, "/appointments", "Invalid appointment id.");
        }
    }

    // ---------- layout ----------

    private static String stat(int n, String label, String href) {
        return "<a class='stat' href='" + href + "'><b>" + n + "</b><span>" + label + "</span></a>";
    }

    private static String layout(String active, String msg, String content) {
        String[][] nav = {{"/", "Home"}, {"/patients", "Patients"}, {"/doctors", "Doctors"},
                {"/book", "Book"}, {"/appointments", "Appointments"}};
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<title>Hospital Appointments</title><style>")
          .append("body{font-family:system-ui,sans-serif;max-width:720px;margin:0 auto;padding:16px;background:#f4f7fa;color:#14212b}")
          .append("nav{display:flex;flex-wrap:wrap;gap:6px;margin-bottom:16px}")
          .append("nav a{padding:8px 12px;border:1px solid #dbe3ea;border-radius:8px;background:#fff;color:#14212b;text-decoration:none}")
          .append("nav a.on{background:#0b7a75;color:#fff;border-color:#0b7a75}")
          .append(".card{background:#fff;border:1px solid #dbe3ea;border-radius:12px;padding:14px;margin-bottom:14px}")
          .append("label{display:block;font-size:.85rem;color:#5d6b78;margin:10px 0 4px}")
          .append("input,select{width:100%;padding:10px;border:1px solid #dbe3ea;border-radius:8px;font:inherit;box-sizing:border-box}")
          .append("button,.btn{display:inline-block;margin-top:12px;padding:10px 14px;border:0;border-radius:8px;background:#0b7a75;color:#fff;font:inherit;cursor:pointer;text-decoration:none}")
          .append("button.x{background:#b3261e;margin:0}")
          .append(".msg{background:#e3f4f2;border-radius:8px;padding:10px;margin-bottom:14px}")
          .append(".row{display:flex;justify-content:space-between;align-items:center;gap:8px;border-top:1px solid #dbe3ea;padding:8px 0}")
          .append(".stats{display:flex;gap:10px;margin-bottom:10px}")
          .append(".stat{flex:1;background:#fff;border:1px solid #dbe3ea;border-radius:12px;padding:14px;text-align:center;text-decoration:none;color:#14212b}")
          .append(".stat b{display:block;font-size:1.8rem}")
          .append("</style></head><body><h1>Hospital Appointments</h1><nav>");
        for (String[] n : nav)
            sb.append("<a href='").append(n[0]).append("'").append(n[0].equals(active) ? " class='on'" : "")
              .append(">").append(n[1]).append("</a>");
        sb.append("</nav>");
        if (!msg.isEmpty()) sb.append("<div class='msg'>").append(esc(msg)).append("</div>");
        sb.append(content).append("</body></html>");
        return sb.toString();
    }

    // ---------- helpers ----------

    private static boolean isPost(HttpExchange ex) {
        return "POST".equals(ex.getRequestMethod());
    }

    private static String msg(HttpExchange ex) {
        return parse(ex.getRequestURI().getRawQuery()).getOrDefault("msg", "");
    }

    private static String body(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parse(String s) {
        Map<String, String> m = new HashMap<>();
        if (s == null || s.isEmpty()) return m;
        for (String pair : s.split("&")) {
            int i = pair.indexOf('=');
            String k = i < 0 ? pair : pair.substring(0, i);
            String v = i < 0 ? "" : pair.substring(i + 1);
            m.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
        }
        return m;
    }

    private static void send(HttpExchange ex, int code, String html) throws IOException {
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    private static void redirect(HttpExchange ex, String path, String msg) throws IOException {
        String loc = msg.isEmpty() ? path : path + "?msg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Location", loc);
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
