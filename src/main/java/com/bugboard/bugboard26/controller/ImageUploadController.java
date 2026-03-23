package com.bugboard.bugboard26.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

/**
 * Controller REST dedicato al caricamento di file immagine.
 * <p>
 * Riceve un file in upload, lo salva nella cartella locale dedicata
 * e restituisce l'URL pubblico con cui può essere raggiunto.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class ImageUploadController {

    private static final String UPLOAD_DIR = "uploads/";

    /**
     * Carica un file immagine sul server e restituisce l'URL di accesso.
     *
     * @param file file inviato dal client
     * @return mappa contenente l'URL pubblico del file caricato
     * @throws Exception se si verifica un errore durante la creazione della cartella
     *                   o durante il salvataggio del file
     */
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