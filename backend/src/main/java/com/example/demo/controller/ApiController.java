package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed_root}")
    private String allowedRoot;

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody DeleteRequest req) {
        List<String> deleted = new ArrayList<>();
        List<String> refused = new ArrayList<>();
        Path root;
        try {
            root = Paths.get(allowedRoot).toRealPath();
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "allowed_root not accessible: " + e.getMessage()));
        }
        if (req == null || req.paths == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "paths required"));
        }
        for (String p : req.paths) {
            if (p == null || p.isBlank()) { refused.add(String.valueOf(p)); continue; }
            try {
                Path candidate = root.resolve(p).normalize();
                if (!candidate.startsWith(root)) { refused.add(p); continue; }
                if (Files.isSymbolicLink(candidate)) { refused.add(p); continue; }
                Path real = candidate.toRealPath(LinkOption.NOFOLLOW_LINKS);
                if (!real.startsWith(root)) { refused.add(p); continue; }
                BasicFileAttributes attrs = Files.readAttributes(real, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (!attrs.isRegularFile()) { refused.add(p); continue; }
                Files.delete(real);
                deleted.add(p);
            } catch (IOException | SecurityException e) {
                refused.add(p);
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deleted", deleted);
        body.put("refused", refused);
        return ResponseEntity.ok(body);
    }

    public static class DeleteRequest {
        public List<String> paths;
    }
}
