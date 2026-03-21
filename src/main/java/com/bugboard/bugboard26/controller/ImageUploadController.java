package com.bugboard.bugboard26.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ImageUploadController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) throws Exception {

        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String original = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "file";
        String ext      = original.contains(".")
                ? original.substring(original.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID() + ext;  // ← mantiene l'estensione originale
        Files.write(Paths.get(UPLOAD_DIR + filename), file.getBytes());

        Map<String, String> res = new HashMap<>();
        res.put("url", "http://localhost:8081/uploads/" + filename);
        return ResponseEntity.ok(res);
    }

}
