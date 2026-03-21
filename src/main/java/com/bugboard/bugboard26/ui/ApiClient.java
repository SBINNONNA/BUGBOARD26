package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8081/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static String token = null;
    // Aggiungi questi campi e metodi
    private static Long   currentProjectId   = null;
    private static String currentProjectName = "";

    public static void setCurrentProject(Long id, String name) {
        currentProjectId   = id;
        currentProjectName = name;
    }
    public static Long   getCurrentProjectId()   { return currentProjectId; }
    public static String getCurrentProjectName() { return currentProjectName; }


    public static void setToken(String t) { token = t; }
    public static String getToken() { return token; }
    public static boolean isLoggedIn() { return token != null; }

    public static String post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
    public static String uploadFile(java.io.File file) throws Exception {
        String boundary  = "----Boundary" + System.currentTimeMillis();
        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());

        // ← rileva il MIME correttamente per jpg, jpeg, png, gif
        String name = file.getName().toLowerCase();
        String mime;
        if      (name.endsWith(".png"))  mime = "image/png";
        else if (name.endsWith(".gif"))  mime = "image/gif";
        else                             mime = "image/jpeg";

        byte[] header = ("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\""
                + file.getName() + "\"\r\n"
                + "Content-Type: " + mime + "\r\n\r\n").getBytes();
        byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes();
        byte[] body   = new byte[header.length + fileBytes.length + footer.length];
        System.arraycopy(header,    0, body, 0,                                header.length);
        System.arraycopy(fileBytes, 0, body, header.length,                    fileBytes.length);
        System.arraycopy(footer,    0, body, header.length + fileBytes.length, footer.length);

        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/upload"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        java.net.http.HttpResponse<String> resp = java.net.http.HttpClient.newHttpClient()
                .send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

        // ← controlla status code: lancia eccezione leggibile se non è 200
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Upload fallito (HTTP " + resp.statusCode()
                    + "): " + resp.body());
        }

        return resp.body(); // {"url":"http://..."}
    }


    public static String postAuth(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }


    public static String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String put(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400)                          // ← aggiunge questo
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        return response.body();
    }

    public static String patch(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400)
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        return response.body();
    }
    public static void delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400)
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
    }


}
